package uk.gov.hmcts.sptribs.dmn.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.http.Headers;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionEvaluationLogger;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.dmn.domain.entities.task.Expectation;
import uk.gov.hmcts.sptribs.dmn.domain.entities.task.Task;
import uk.gov.hmcts.sptribs.dmn.domain.entities.task.TestScenario;
import uk.gov.hmcts.sptribs.testutil.DeserializeValuesUtil;
import uk.gov.hmcts.sptribs.testutil.MapSerializer;
import uk.gov.hmcts.sptribs.testutil.MapValueExtractor;
import uk.gov.hmcts.sptribs.testutil.TaskDataVerifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@Component
@Slf4j
public class TaskMgmApiRetrieverService implements TaskRetrieverService {

    private final TaskManagementService taskManagementService;
    private final DeserializeValuesUtil deserializeValuesUtil;
    private final List<TaskDataVerifier> verifiers;

    public TaskMgmApiRetrieverService(TaskManagementService taskManagementService,
                                      DeserializeValuesUtil deserializeValuesUtil,
                                      List<TaskDataVerifier> verifiers) {
        this.taskManagementService = taskManagementService;
        this.deserializeValuesUtil = deserializeValuesUtil;
        this.verifiers = verifiers;
    }

    @SneakyThrows
    @Override
    public void retrieveTask(Expectation expectation, TestScenario scenario, String caseId,
                             Headers authorizationHeaders) {
        retrieveTask(expectation, scenario, singletonList(caseId), authorizationHeaders);
    }

    @SneakyThrows
    public void retrieveTask(Expectation expectation, TestScenario scenario,
                             List<String> caseIds, Headers authorizationHeaders) {
        // clauseValues -> expectations

        Map<String, String> taskTemplatesByFilename =
            StringResourceLoader.load(
                "/templates/" + scenario.getJurisdiction().toLowerCase(Locale.ENGLISH) + "/task/*.json"
            );

        Map<String, String> additionalValues;
        if (scenario.getAssignedCaseIdMap() != null && scenario.getAssignedCaseIdMap().size() > 1) {
            additionalValues = scenario.getAssignedCaseIdMap();
        } else {
            additionalValues = new HashMap<>() {
                {
                    put("caseId", caseIds.get(0));
                }
            };
        }

        if (scenario.getAssigneeId() != null) {
            additionalValues.put("assigneeId", scenario.getAssigneeId());
        }

        // TODO - work out how this should be handled
        Map<String, Object> deserializedClauseValues =
            deserializeValuesUtil.expandMapValues(clauseValues, additionalValues);

        AtomicBoolean isTestPassed = new AtomicBoolean(false);

        await()
            .ignoreException(AssertionError.class)
            .conditionEvaluationListener(new ConditionEvaluationLogger(log::info))
            .pollInterval(DEFAULT_POLL_INTERVAL_SECONDS, SECONDS)
            .atMost(DEFAULT_TIMEOUT_SECONDS, SECONDS)
            .until(
                () -> {

                    String searchByCaseIdResponseBody = taskManagementService.search(
                        expectation,
                        caseIds,
                        scenario,
                        authorizationHeaders
                    );

                    // TODO - fix this method
                    String expectedResponseBody = buildTaskExpectationResponseBody(
                        expectation,
                        taskTemplatesByFilename,
                        additionalValues
                    );

                    log.info("Expectation {}", expectedResponseBody);
                    if (searchByCaseIdResponseBody.isBlank()) {
                        log.error("Find my case ID response is empty. Test will now fail");
                        return false;
                    }

                    // TODO - make sure this is correct
                    Map<String, Object> actualResponse = MapSerializer.deserialize(
                        MapSerializer.sortCollectionElement(
                            searchByCaseIdResponseBody,
                            "tasks",
                            taskTitleComparator()
                        ));
                    Map<String, Object> expectedResponse = MapSerializer.deserialize(expectedResponseBody);

                    verifiers.forEach(verifier ->
                        verifier.verify(
                            scenario,
                            expectedResponse,
                            actualResponse
                        )
                    );

                    List<Map<String, Object>> tasks = MapValueExtractor.extract(actualResponse, "tasks");

                    if (tasks == null || tasks.isEmpty()) {
                        log.error("Task list is empty. Test will now fail");
                        return false;
                    }

                    tasks.forEach(t -> scenario.addTaskId(MapValueExtractor.extract(t, "id")));
                    AtomicReference<Map<String, Object>> actualRoleResponse = new AtomicReference<>(emptyMap());
                    AtomicReference<Map<String, Object>> expectedRoleResponse = new AtomicReference<>(emptyMap());

                    Map<String, Object> scenarioMap = deserializeValuesUtil.expandMapValues(
                        deserializedClauseValues,
                        additionalValues
                    );

                    AtomicInteger index = new AtomicInteger(0);
                    tasks.forEach(task -> {
                        try {
                            String taskId = MapValueExtractor.extract(task, "id");

                            List<Map<String, Object>> taskDataList = MapValueExtractor.extract(
                                scenarioMap,
                                "taskData.replacements.tasks"
                            );

                            if (taskDataList == null || taskDataList.isEmpty()) {
                                log.info("taskDataList is null or empty");
                                return;
                            }

                            Map<String, Object> taskData = taskDataList.get(index.get());

                            List<Map<String, Object>> metaDataList = MapValueExtractor.extract(
                                taskData,
                                "test_meta_data"
                            );

                            if (metaDataList == null || metaDataList.isEmpty()) {
                                log.info("metaDataList is null or empty");
                                return;
                            }

                            String roleDataKey = metaDataList.stream()
                                .filter(md -> md.containsKey("key"))
                                .map(md -> md.get("value").toString())
                                .findFirst()
                                .orElse(null);

                            Map<String, Object> roleDataMap = filterRoleData(clauseValues, roleDataKey);

                            //skip role assignment validation if no role data provided in scenario
                            if (roleDataMap.isEmpty()) {
                                isTestPassed.set(true);
                                return;
                            }

                            int expectedStatus = expectation.getStatus();

                            String retrieveTaskRolePermissionsResponseBody =
                                taskManagementService.retrieveTaskRolePermissions(
                                    roleDataMap,
                                    taskId,
                                    expectedStatus,
                                    authorizationHeaders
                                );

                            if (retrieveTaskRolePermissionsResponseBody.isBlank()) {
                                log.error("Task role permissions response is empty. Test will now fail");
                                isTestPassed.set(false);
                            }

                            String rolesExpectationResponseBody = buildRolesExpectationResponseBody(
                                deserializedClauseValues,
                                additionalValues,
                                roleDataKey
                            );

                            log.info("expected roles: {}", rolesExpectationResponseBody);
                            actualRoleResponse.set(MapSerializer.deserialize(
                                retrieveTaskRolePermissionsResponseBody));
                            expectedRoleResponse.set(MapSerializer.deserialize(
                                rolesExpectationResponseBody));

                            verifiers.forEach(verifier ->
                                verifier.verify(
                                    scenario,
                                    expectedRoleResponse.get(),
                                    actualRoleResponse.get()
                                )
                            );

                            index.getAndIncrement();

                        } catch (Exception e) {
                            isTestPassed.set(false);
                            Logger.say(SCENARIO_FAILED, scenario.getDescription());
                            throw new RuntimeException(e);
                        }

                    });

                    isTestPassed.set(true);
                    return true;
                });

        if (!isTestPassed.get()) {
            Logger.say(SCENARIO_FAILED, scenario.getDescription());
        }
    }

    private Comparator<JsonNode> taskTitleComparator() {
        return (j1, j2) -> {
            String title1 = j1.findValue("task_title").asText();
            String title2 = j2.findValue("task_title").asText();
            return title1.compareTo(title2);
        };
    }

    private String buildTaskExpectationResponseBody(Expectation expectation,
                                                    Map<String, String> taskTemplatesByFilename,
                                                    Map<String, String> additionalValues) throws IOException {

        Task taskData = expectation.getTaskData();

        String templateFilename = taskData.getTemplate();

        Map<String, Object> taskDataExpectation = deserializeValuesUtil.deserializeStringWithExpandedValues(
            taskTemplatesByFilename.get(templateFilename),
            additionalValues
        );

        // TODO - how to configure replacements - might need another class
        Map<String, Object> taskDataDataReplacements = MapValueExtractor.extract(taskData, "replacements");
        Map<String, Object> taskDataDataReplacementsWithoutMetaData = removeMetaDataFromDataMap(
            taskDataDataReplacements);
        Map<String, Object> taskDataExpectationWithoutMetaData = removeMetaDataFromDataMap(taskDataExpectation);

        if (taskDataDataReplacements != null) {
            MapMerger.merge(taskDataExpectationWithoutMetaData, taskDataDataReplacementsWithoutMetaData);
        }

        return MapSerializer.serialize(taskDataExpectationWithoutMetaData);

    }

    private Map<String, Object> removeMetaDataFromDataMap(Map<String, Object> dataMap) {

        List<Map<String, Object>> tasks = new ArrayList<>(
            requireNonNull(MapValueExtractor.extract(dataMap, "tasks")));

        Map<String, Object> tempDataMap = new HashMap<>(dataMap);
        tempDataMap.remove("tasks");
        List<Map<String, Object>> taskWithoutMetaDataList = new ArrayList<>();

        tasks.forEach(task -> {
            Map<String, Object> taskWithoutMetaData = new HashMap<>(task);
            taskWithoutMetaData.entrySet().removeIf(entry -> entry.getKey().equals("test_meta_data"));
            taskWithoutMetaDataList.add(taskWithoutMetaData);
        });

        tempDataMap.put("tasks", taskWithoutMetaDataList);

        return tempDataMap;
    }

    private String buildRolesExpectationResponseBody(Map<String, Object> clauseValues,
                                                     Map<String, String> additionalValues,
                                                     String roleDataKey) throws IOException {

        Map<String, Object> scenario = deserializeValuesUtil.expandMapValues(clauseValues, additionalValues);
        Map<String, Object> roleDataMap = filterRoleData(scenario, roleDataKey);
        HashMap<String, Object> roleMap = new HashMap<>(roleDataMap);

        List<String> toBeRemovedKeys = Arrays.asList("key", "numberOfRolesAvailable");
        roleMap.entrySet().removeIf(entry -> toBeRemovedKeys.contains(entry.getKey()));
        return MapSerializer.serialize(roleMap);
    }

    protected Map<String, Object> filterRoleData(Map<String, Object> clauseValues, String key) {

        List<Map<String, Object>> roleData =
            MapValueExtractor.extractOrDefault(
                clauseValues,
                "roleData",
                new ArrayList<>()
            );

        return roleData.stream()
            .filter((Map<String, Object> rd) -> rd.get("key").equals(key))
            .findFirst()
            .orElse(emptyMap());
    }

}
