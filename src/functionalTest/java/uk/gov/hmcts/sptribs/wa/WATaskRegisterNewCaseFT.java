package uk.gov.hmcts.sptribs.wa;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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

@SpringBootTest
@Slf4j
public class WATaskRegisterNewCaseFT extends FunctionalTestSuite {

    @Autowired
    private CcdCaseCreator ccdCaseCreator;

    @Autowired
    private TaskManagementService taskManagementService;

    @Autowired
    private RoleAssignmentService roleAssignmentService;

    private static final String TASK_TYPE = "registerNewCase";
    private static final List<String> TASK_ROLES = Arrays.asList("regional-centre-admin", "regional-centre-team-leader", "task-supervisor");
    private static final int DEFAULT_TIMEOUT_SECONDS = 300;
    private static final int DEFAULT_POLL_INTERVAL_SECONDS = 4;

    @Test
    @Disabled
    public void shouldInitiateRegisterNewCaseTask() throws IOException, InterruptedException {
        String newCaseId = String.valueOf(createAndSubmitTestCaseAndGetCaseReference());

        log.debug("New case created: " + newCaseId);

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
                        taskManagementService.retrieveTaskRolePermissions(taskId, 3, 200);

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
