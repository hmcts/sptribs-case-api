package uk.gov.hmcts.sptribs.dmn.service;

import io.restassured.http.Headers;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.dmn.domain.entities.task.Expectation;
import uk.gov.hmcts.sptribs.dmn.domain.entities.task.TestScenario;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
@Slf4j
public class TaskManagementService {

    private static final String CLAIM_ENDPOINT_PATH = "/task/{task-id}/claim";
    private static final String ASSIGN_ENDPOINT_PATH = "/task/{task-id}/assign";
    private static final String COMPLETE_ENDPOINT_PATH = "/task/{task-id}/complete";

    @Autowired
    protected AuthorizationHeadersProvider authorizationHeadersProvider;

    @Autowired
    private MapValueExpander mapValueExpander;
    @Autowired
    private DeserializeValuesUtil deserializeValuesUtil;
    @Autowired
    private TaskMonitorService taskMonitorService;

    @Value("${wa_task_management_api.url}")
    private String taskManagementUrl;

    public String search(Expectation expectation,
                         List caseIds,
                         TestScenario scenario,
                         Headers authorizationHeaders) {

        Map<String, Object> searchParameter = Map.of(
            "key", "caseId",
            "operator", "IN",
            "values", caseIds
        );

        Set<Map<String, Object>> searchMap = new HashSet<>();
        searchMap.add(searchParameter);

        Map<String, Set<Map<String, Object>>> requestBody = Map.of("search_parameters", searchMap);

        //Also trigger (CRON) Jobs programmatically
        taskMonitorService.triggerInitiationJob(authorizationHeaders);
        taskMonitorService.triggerTerminationJob(authorizationHeaders);
        taskMonitorService.triggerReconfigurationJob(authorizationHeaders);

        int expectedStatus = expectation.getStatus();
        int expectedTasks = expectation.getNumberOfTasksAvailable();

        Response result = given()
            .headers(authorizationHeaders)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(requestBody)
            .when()
            .post(taskManagementUrl + "/task/extended-search");

        result.then().assertThat()
            .statusCode(expectedStatus)
            .contentType(APPLICATION_JSON_VALUE)
            .body("tasks.size()", is(expectedTasks));

        String actualResponseBody = result.then()
            .extract()
            .body().asString();

        log.info("Response body: " + actualResponseBody);

        return actualResponseBody;
    }

    public String retrieveTaskRolePermissions(Map<String, Object> roleDataMap,
                                              String taskId,
                                              int expectedStatus,
                                              Headers authorizationHeaders) {

        Response result = given()
            .headers(authorizationHeaders)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get(taskManagementUrl + "/task/" + taskId + "/roles");

        int expectedRoles = MapValueExtractor.extractOrDefault(
            roleDataMap, "numberOfRolesAvailable", 4);

        result.then().assertThat()
            .statusCode(expectedStatus)
            .contentType(APPLICATION_JSON_VALUE)
            .body("roles.size()", is(expectedRoles));

        String actualResponseBody = result.then()
            .extract()
            .body().asString();

        log.info("Response body: " + actualResponseBody);

        return actualResponseBody;
    }

    public void claimTask(TestScenario scenario, Headers authorizationHeaders, UserInfo userInfo) {
        if (scenario.getTaskIds().isEmpty()) {
            log.error("Task id list for claim is empty. Test will now fail");
        } else {
            scenario.getTaskIds().forEach(taskId -> {
                Response result = given()
                    .headers(authorizationHeaders)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .when()
                    .post(taskManagementUrl + CLAIM_ENDPOINT_PATH, taskId);

                result.then().assertThat()
                    .statusCode(HttpStatus.NO_CONTENT.value());
                log.info("Claim task request, task id: {}, assignee id: {}, response code: {}",
                    taskId, userInfo.getUid(), result.getStatusCode());
            });

            scenario.setAssigneeId(userInfo.getUid());
        }
    }

    public void assignTask(TestScenario scenario, Headers authorizationHeaders, UserInfo assignee) {

        if (scenario.getTaskIds().isEmpty()) {
            log.error("Task id list for assign is empty. Test will now fail");
        } else {
            scenario.getTaskIds().forEach(taskId -> {
                Response result = given()
                    .headers(authorizationHeaders)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body("{\"user_id\": \"" + assignee.getUid() + "\" }")
                    .when()
                    .post(taskManagementUrl + ASSIGN_ENDPOINT_PATH, taskId);

                result.then().assertThat()
                    .statusCode(HttpStatus.NO_CONTENT.value());
                log.info("Assign task request, task id: {}, assignee id: {}, response code: {}",
                    taskId, assignee.getUid(), result.getStatusCode());
            });

            scenario.setAssigneeId(assignee.getUid());
        }
    }

    public void completeTask(TestScenario scenario, Headers authorizationHeaders, UserInfo userInfo) {
        if (scenario.getTaskIds().isEmpty()) {
            log.error("Task id list for complete is empty. Test will now fail");
        } else {
            scenario.getTaskIds().forEach(taskId -> {
                Response result = given()
                    .headers(authorizationHeaders)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .when()
                    .post(taskManagementUrl + COMPLETE_ENDPOINT_PATH, taskId);

                result.then().assertThat()
                    .statusCode(HttpStatus.NO_CONTENT.value());
                log.info("Complete task request, task id: {}, requester id: {}, response code: {}",
                    taskId, userInfo.getUid(), result.getStatusCode());
            });
        }
    }

}
