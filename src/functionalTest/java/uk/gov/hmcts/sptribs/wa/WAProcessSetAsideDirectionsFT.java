package uk.gov.hmcts.sptribs.wa;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ST_CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ST_CIC_JURISDICTION;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CASE_BUILT;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CLOSE_THE_CASE;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CREATE_DRAFT_ORDER;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_EDIT_CASE;

@SpringBootTest
@Slf4j
public class WAProcessSetAsideDirectionsFT extends FunctionalTestSuite {
    @Autowired
    private CcdCaseCreator ccdCaseCreator;

    @Autowired
    private TaskManagementService taskManagementService;

    @Autowired
    private RoleAssignmentService roleAssignmentService;

    private static final String TASK_TYPE = "processSetAsideDirections";
    private static final List<String> TASK_ROLES = Arrays.asList("regional-centre-admin", "regional-centre-team-leader", "task-supervisor",
        "hearing-centre-admin", "hearing-centre-team-leader", "ctsc", "ctsc-team-leader");
    private static final int DEFAULT_TIMEOUT_SECONDS = 300;
    private static final int DEFAULT_POLL_INTERVAL_SECONDS = 4;

    private static final String CASEWORKER_CREATE_DRAFT_ORDER_DATA = "classpath:wa/caseworker-create-draft-order-submit-data.json";

    @Test
    @EnabledIfEnvironmentVariable(named = "WA_FUNCTIONAL_TESTS_ENABLED", matches = "true")
    void shouldInitiateProcessSetAsideDirectionsTask() throws IOException {
        final CaseDetails caseDetails = createAndSubmitCitizenCaseAndGetCaseDetails();
        final long id = caseDetails.getId();
        final String newCaseId = String.valueOf(id);
        final Map<String, Object> caseData = caseDetails.getData();

        log.debug("New case created: " + newCaseId);

        ccdCaseCreator.createInitialStartEventAndSubmit(CASEWORKER_EDIT_CASE, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);
        ccdCaseCreator.createInitialStartEventAndSubmit(CASEWORKER_CASE_BUILT, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);
        ccdCaseCreator.createInitialStartEventAndSubmit(
            CASEWORKER_CLOSE_THE_CASE, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);

        caseData.put("cicCaseReferralTypeForWA", "Set aside request");
        caseData.putAll(caseData(CASEWORKER_CREATE_DRAFT_ORDER_DATA));
        checkAndUpdateDraftOrderDocument(caseData);
        ccdCaseCreator.createInitialStartEventAndSubmitAdminEvent(
            CASEWORKER_CREATE_DRAFT_ORDER, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);

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
}
