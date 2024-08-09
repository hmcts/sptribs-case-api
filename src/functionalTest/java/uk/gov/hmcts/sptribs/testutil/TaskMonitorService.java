package uk.gov.hmcts.sptribs.testutil;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@Service
@Slf4j
public class TaskMonitorService {

    @Autowired
    private IdamTokenGenerator idamTokenGenerator;

    @Autowired
    private ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Value("${wa_task_monitor_api.url}")
    private String taskMonitorUrl;

    public static final int DEFAULT_TIMEOUT_SECONDS = 300;
    public static final int DEFAULT_POLL_INTERVAL_SECONDS = 4;

    public void triggerInitiationJob() {
        Map<String, Map<String, String>> requestBody = Map.of("job_details", Map.of("name", "INITIATION"));
        initiateJob(requestBody);
    }

    public void triggerTerminationJob() {
        Map<String, Map<String, String>> requestBody = Map.of("job_details", Map.of("name", "TERMINATION"));
        initiateJob(requestBody);
    }

    private void initiateJob(Map<String, Map<String, String>> requestBody) {
        await()
            .ignoreException(AssertionError.class)
            .pollInterval(DEFAULT_POLL_INTERVAL_SECONDS, SECONDS)
            .atMost(DEFAULT_TIMEOUT_SECONDS, SECONDS)
            .until(
                () -> {
                    Response result = given()
                        .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate())
                        .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystemUser())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(requestBody)
                        .when()
                        .post(taskMonitorUrl + "/monitor/tasks/jobs");

                    result.then().assertThat()
                        .statusCode(200)
                        .contentType(APPLICATION_JSON_VALUE);
                    return true;
                });
    }
}
