package uk.gov.hmcts.sptribs.wa;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
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
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;

@SpringBootTest
public class WATaskRegisterNewCaseFT extends FunctionalTestSuite {

    @Autowired
    private CcdCaseCreator ccdCaseCreator;

    @Autowired
    private TaskManagementService taskManagementService;

    private static final String TASK_ID = "issueCaseToRespondent";

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-caseworker-submit-case.json";

    public static final int DEFAULT_TIMEOUT_SECONDS = 300;
    public static final int DEFAULT_POLL_INTERVAL_SECONDS = 4;

    @Test
    @EnabledIfEnvironmentVariable(named = "", matches = "") //TODO: enable/disable based on whether WA PR
    public void should() throws IOException {
        final Map<String, Object> caseData = caseData(REQUEST);

        // TODO: get user which is set up in AAT with Role assignments? Senior tribunal caseworker perhaps?

        String newCaseId = String.valueOf(createAndSubmitTestCaseAndGetCaseReference());

        System.out.println(newCaseId);

        await()
            .pollInterval(DEFAULT_POLL_INTERVAL_SECONDS, SECONDS)
            .atMost(DEFAULT_TIMEOUT_SECONDS, SECONDS)
            .until(
                () -> {
                    Response searchByCaseIdResponseBody =
                        taskManagementService.search(newCaseId, List.of("issueCaseToRespondent"), 1, 200);

                    if (searchByCaseIdResponseBody.asString().isBlank()) {
                        return false;
                    }

                    // TODO: verify tasks returned against JSON
                    //  get "tasks" out of response body and compare it to expected
                    final List<Map<String, Object>> tasks = searchByCaseIdResponseBody.getBody().path("tasks");
                    final String taskId = searchByCaseIdResponseBody.getBody().path("tasks[0].id");

                    assertNotNull(tasks);
                    assertThat(tasks).isNotEmpty();
                    assertThat(taskId).isEqualTo(TASK_ID);

                    // TODO: do we need a AM refresh to ensure we can fetch task role permissions?
                    String retrieveTaskRolePermissionsResponseBody =
                        taskManagementService.retrieveTaskRolePermissions(taskId, 4, 200);
                    // TODO: verify roles returned against JSON
                    return true;
                });
    }
}
