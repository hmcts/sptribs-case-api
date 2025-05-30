package uk.gov.hmcts.sptribs.wa;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.CcdCaseCreator;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;
import uk.gov.hmcts.sptribs.testutil.RoleAssignmentService;
import uk.gov.hmcts.sptribs.testutil.TaskManagementService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemTriggerCompleteHearingOutcome.SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ST_CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ST_CIC_JURISDICTION;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CANCEL_HEARING;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CASE_BUILT;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_EDIT_CASE;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_POSTPONE_HEARING;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_RECORD_LISTING;

@SpringBootTest
@Slf4j
public class WACompleteHearingOutcomeFT extends FunctionalTestSuite {

    @Autowired
    private CcdCaseCreator ccdCaseCreator;

    @Autowired
    private TaskManagementService taskManagementService;

    @Autowired
    private RoleAssignmentService roleAssignmentService;

    private static final String TASK_TYPE = "completeHearingOutcome";
    private static final List<String> TASK_ROLES = Arrays.asList("regional-centre-admin", "regional-centre-team-leader", "task-supervisor",
        "hearing-centre-admin", "hearing-centre-team-leader", "ctsc", "ctsc-team-leader");
    private static final int DEFAULT_TIMEOUT_SECONDS = 300;
    private static final int DEFAULT_POLL_INTERVAL_SECONDS = 4;

    private static final String CASEWORKER_RECORD_LISTING_DATA = "classpath:wa/caseworker-record-listing-submit-data.json";
    private static final String CASEWORKER_CANCEL_HEARING_DATA = "classpath:wa/caseworker-cancel-hearing-submit-data.json";
    private static final String CASEWORKER_POSTPONE_HEARING_DATA = "classpath:wa/caseworker-postpone-hearing-submit-data.json";

    @Test
    @EnabledIfEnvironmentVariable(named = "WA_FUNCTIONAL_TESTS_ENABLED", matches = "true")
    void shouldInitiateCompleteHearingOutcomeTask() throws IOException {
        final Response response = createAndSubmitTestCaseAndGetResponse();
        final long id = response.getBody().path("id");
        final String newCaseId = String.valueOf(id);
        final Map<String, Object> caseData = response.getBody().path("caseData");

        log.debug("New case created: " + newCaseId);

        ccdCaseCreator.createInitialStartEventAndSubmit(CASEWORKER_EDIT_CASE, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);
        ccdCaseCreator.createInitialStartEventAndSubmit(CASEWORKER_CASE_BUILT, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);

        caseData.putAll(caseData(CASEWORKER_RECORD_LISTING_DATA));
        ccdCaseCreator.createInitialStartEventAndSubmit(
            CASEWORKER_RECORD_LISTING, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);

        ccdCaseCreator.createInitialStartEventAndSubmitSystemEvent(
            SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);

        await()
            .pollInterval(DEFAULT_POLL_INTERVAL_SECONDS, SECONDS)
            .atMost(DEFAULT_TIMEOUT_SECONDS, SECONDS)
            .until(
                () -> {
                    roleAssignmentService.createRoleAssignmentsForWaSeniorCaseworker();

                    Response searchByCaseIdResponseBody =
                        taskManagementService.search(newCaseId, List.of(TASK_TYPE), 1, 200);

                    if (searchByCaseIdResponseBody.asString().isBlank()) {
                        return false;
                    }

                    final List<Map<String, Object>> tasks = searchByCaseIdResponseBody.getBody().path("tasks");
                    final String taskId = searchByCaseIdResponseBody.getBody().path("tasks[0].id");
                    final String taskType = searchByCaseIdResponseBody.getBody().path("tasks[0].type");

                    assertNotNull(tasks);
                    assertThat(tasks).isNotEmpty();
                    assertThat(taskType).isEqualTo(TASK_TYPE);

                    Response retrieveTaskRolePermissionsResponseBody =
                        taskManagementService.retrieveTaskRolePermissions(taskId, 7, 200);

                    if (retrieveTaskRolePermissionsResponseBody.asString().isBlank()) {
                        return false;
                    }

                    final List<Map<String, Object>> roles = retrieveTaskRolePermissionsResponseBody.getBody().path("roles");

                    assertNotNull(roles);
                    assertThat(roles).isNotEmpty();

                    for (Map<String, Object> role : roles) {
                        String roleName = role.get("role_name").toString();
                        assertThat(roleName).isIn(TASK_ROLES);
                    }

                    return true;
                });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "WA_FUNCTIONAL_TESTS_ENABLED", matches = "true")
    void shouldCancelCompleteHearingOutcomeTaskWithCaseworkerPostponeHearing() throws IOException {
        final Response response = createAndSubmitTestCaseAndGetResponse();
        final long id = response.getBody().path("id");
        final String newCaseId = String.valueOf(id);
        final Map<String, Object> caseData = response.getBody().path("caseData");

        log.debug("New case created: " + newCaseId);

        ccdCaseCreator.createInitialStartEventAndSubmit(CASEWORKER_EDIT_CASE, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);
        ccdCaseCreator.createInitialStartEventAndSubmit(CASEWORKER_CASE_BUILT, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);

        caseData.putAll(caseData(CASEWORKER_RECORD_LISTING_DATA));
        ccdCaseCreator.createInitialStartEventAndSubmit(
            CASEWORKER_RECORD_LISTING, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);

        ccdCaseCreator.createInitialStartEventAndSubmitSystemEvent(
            SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);

        await()
            .pollInterval(DEFAULT_POLL_INTERVAL_SECONDS, SECONDS)
            .atMost(DEFAULT_TIMEOUT_SECONDS, SECONDS)
            .until(
                () -> {
                    roleAssignmentService.createRoleAssignmentsForWaSeniorCaseworker();

                    Response searchByCaseIdResponseBody =
                        taskManagementService.search(newCaseId, List.of(TASK_TYPE), 1, 200);

                    if (searchByCaseIdResponseBody.asString().isBlank()) {
                        return false;
                    }

                    List<Map<String, Object>> tasks = searchByCaseIdResponseBody.getBody().path("tasks");
                    String taskType = searchByCaseIdResponseBody.getBody().path("tasks[0].type");

                    assertNotNull(tasks);
                    assertThat(tasks).isNotEmpty();
                    assertThat(taskType).isEqualTo(TASK_TYPE);

                    String taskState = searchByCaseIdResponseBody.getBody().path("tasks[0].task_state");
                    assertThat(taskState).isEqualTo("unassigned");

                    caseData.putAll(caseData(CASEWORKER_POSTPONE_HEARING_DATA));
                    ccdCaseCreator.createInitialStartEventAndSubmit(
                        CASEWORKER_POSTPONE_HEARING, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);

                    searchByCaseIdResponseBody =
                        taskManagementService.search(newCaseId, List.of(TASK_TYPE), 1, 200);

                    if (searchByCaseIdResponseBody.asString().isBlank()) {
                        return false;
                    }

                    tasks = searchByCaseIdResponseBody.getBody().path("tasks");
                    taskType = searchByCaseIdResponseBody.getBody().path("tasks[0].type");

                    assertNotNull(tasks);
                    assertThat(tasks).isNotEmpty();
                    assertThat(taskType).isEqualTo(TASK_TYPE);

                    taskState = searchByCaseIdResponseBody.getBody().path("tasks[0].task_state");
                    assertThat(taskState).isEqualTo("terminated");

                    return true;
                });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "WA_FUNCTIONAL_TESTS_ENABLED", matches = "true")
    void shouldCancelCompleteHearingOutcomeTaskWithCaseworkerCancelHearing() throws IOException {
        final Response response = createAndSubmitTestCaseAndGetResponse();
        final long id = response.getBody().path("id");
        final String newCaseId = String.valueOf(id);
        final Map<String, Object> caseData = response.getBody().path("caseData");

        log.debug("New case created: " + newCaseId);

        ccdCaseCreator.createInitialStartEventAndSubmit(CASEWORKER_EDIT_CASE, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);
        ccdCaseCreator.createInitialStartEventAndSubmit(CASEWORKER_CASE_BUILT, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);

        caseData.putAll(caseData(CASEWORKER_RECORD_LISTING_DATA));
        ccdCaseCreator.createInitialStartEventAndSubmit(
            CASEWORKER_RECORD_LISTING, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);

        ccdCaseCreator.createInitialStartEventAndSubmitSystemEvent(
            SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);

        await()
            .pollInterval(DEFAULT_POLL_INTERVAL_SECONDS, SECONDS)
            .atMost(DEFAULT_TIMEOUT_SECONDS, SECONDS)
            .until(
                () -> {
                    roleAssignmentService.createRoleAssignmentsForWaSeniorCaseworker();

                    Response searchByCaseIdResponseBody =
                        taskManagementService.search(newCaseId, List.of(TASK_TYPE), 1, 200);

                    if (searchByCaseIdResponseBody.asString().isBlank()) {
                        return false;
                    }

                    List<Map<String, Object>> tasks = searchByCaseIdResponseBody.getBody().path("tasks");
                    String taskType = searchByCaseIdResponseBody.getBody().path("tasks[0].type");

                    assertNotNull(tasks);
                    assertThat(tasks).isNotEmpty();
                    assertThat(taskType).isEqualTo(TASK_TYPE);

                    String taskState = searchByCaseIdResponseBody.getBody().path("tasks[0].task_state");
                    assertThat(taskState).isEqualTo("unassigned");

                    caseData.putAll(caseData(CASEWORKER_CANCEL_HEARING_DATA));
                    ccdCaseCreator.createInitialStartEventAndSubmit(
                        CASEWORKER_CANCEL_HEARING, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);

                    searchByCaseIdResponseBody =
                        taskManagementService.search(newCaseId, List.of(TASK_TYPE), 1, 200);

                    if (searchByCaseIdResponseBody.asString().isBlank()) {
                        return false;
                    }

                    tasks = searchByCaseIdResponseBody.getBody().path("tasks");
                    taskType = searchByCaseIdResponseBody.getBody().path("tasks[0].type");

                    assertNotNull(tasks);
                    assertThat(tasks).isNotEmpty();
                    assertThat(taskType).isEqualTo(TASK_TYPE);

                    taskState = searchByCaseIdResponseBody.getBody().path("tasks[0].task_state");
                    assertThat(taskState).isEqualTo("terminated");

                    return true;
                });
    }
}
