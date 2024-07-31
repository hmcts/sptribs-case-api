package uk.gov.hmcts.sptribs.dmn.service;

import io.restassured.http.Headers;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionEvaluationLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static net.serenitybdd.rest.SerenityRest.given;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
@Slf4j
public class TaskMonitorService {

    @Autowired
    protected AuthorizationHeadersProvider authorizationHeadersProvider;

    @Autowired
    private MapValueExpander mapValueExpander;

    @Value("${wa_task_monitor_api.url}")
    private String taskMonitorUrl;

    public void triggerInitiationJob(Headers authorizationHeaders) {

        Map<String, Map<String, String>> requestBody = Map.of("job_details", Map.of("name", "INITIATION"));

        initiateJob(requestBody, authorizationHeaders);

    }

    public void triggerTerminationJob(Headers authorizationHeaders) {

        Map<String, Map<String, String>> requestBody = Map.of("job_details", Map.of("name", "TERMINATION"));

        initiateJob(requestBody, authorizationHeaders);
    }

    public void triggerReconfigurationJob(Headers authorizationHeaders) {

        Map<String, Map<String, String>> requestBody = Map.of("job_details", Map.of("name", "RECONFIGURATION"));

        initiateJob(requestBody, authorizationHeaders);
        await().atLeast(DEFAULT_TIMEOUT_SECONDS, SECONDS);
    }

    private void initiateJob(Map<String, Map<String, String>> requestBody, Headers authorizationHeaders) {
        await()
            .ignoreException(AssertionError.class)
            .conditionEvaluationListener(new ConditionEvaluationLogger(log::info))
            .pollInterval(DEFAULT_POLL_INTERVAL_SECONDS, SECONDS)
            .atMost(DEFAULT_TIMEOUT_SECONDS, SECONDS)
            .until(
                () -> {
                    Response result = given()
                        .headers(authorizationHeaders)
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
