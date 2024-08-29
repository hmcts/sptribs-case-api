package uk.gov.hmcts.sptribs.wa;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.CcdCaseCreator;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;
import uk.gov.hmcts.sptribs.testutil.TaskManagementService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class WATaskRegisterNewCaseFT extends FunctionalTestSuite {

    @Autowired
    private CcdCaseCreator ccdCaseCreator;

    @Autowired
    private TaskManagementService taskManagementService;

    private static final String TASK_ID = "registerNewCase";
    private static final int DEFAULT_TIMEOUT_SECONDS = 300;
    private static final int DEFAULT_POLL_INTERVAL_SECONDS = 4;

    @Test
    public void should() throws IOException, InterruptedException {
        String newCaseId = String.valueOf(createAndSubmitTestCaseAndGetCaseReference());

        System.out.println(newCaseId);

        await()
            .pollInterval(DEFAULT_POLL_INTERVAL_SECONDS, SECONDS)
            .atMost(DEFAULT_TIMEOUT_SECONDS, SECONDS)
            .until(
                () -> {
                    Response searchByCaseIdResponseBody =
                        taskManagementService.search(newCaseId, List.of(TASK_ID), 1, 200);

                    System.out.println(searchByCaseIdResponseBody.asString());

                    if (searchByCaseIdResponseBody.asString().isBlank()) {
                        return false;
                    }

                    final List<Map<String, Object>> tasks = searchByCaseIdResponseBody.getBody().path("tasks");
                    final String taskId = searchByCaseIdResponseBody.getBody().path("tasks[0].type");

                    assertNotNull(tasks);
                    assertThat(tasks).isNotEmpty();
                    assertThat(taskId).isEqualTo(TASK_ID);

                    String retrieveTaskRolePermissionsResponseBody =
                        taskManagementService.retrieveTaskRolePermissions(taskId, 4, 200);
                    System.out.println(retrieveTaskRolePermissionsResponseBody);
                    // TODO: verify roles returned against JSON
                    return true;
                });
    }
}
