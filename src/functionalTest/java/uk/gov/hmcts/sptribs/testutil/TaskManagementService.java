package uk.gov.hmcts.sptribs.testutil;

import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@Service
public class TaskManagementService {

    @Autowired
    private TaskMonitorService taskMonitorService;

    @Autowired
    private IdamTokenGenerator idamTokenGenerator;

    @Autowired
    private ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Value("${wa_task_management_api.url}")
    private String taskManagementUrl;

    public Response search(String caseId,
                         List<String> expectedTaskList,
                         int expectedTasks,
                         int expectedStatus) throws InterruptedException {

        //Also trigger (CRON) Jobs programmatically
        taskMonitorService.triggerInitiationJob();
        taskMonitorService.triggerTerminationJob();
        taskMonitorService.triggerReconfigurationJob();

        // Wait 120 seconds to allow time for task to be created in database
        Thread.sleep(120000);

        Map<String, Object> searchParameter = Map.of(
            "key", "caseId",
            "operator", "IN",
            "values", singletonList(caseId)
        );

        Map<String, Object> searchParameter2 = Map.of(
            "key", "task_type",
            "operator", "IN",
            "values", expectedTaskList
        );

        Map<String, Set<Map<String, Object>>> requestBody = Map.of("search_parameters", Set.of(searchParameter, searchParameter2));
        Response result = given()
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTaskManagementToken())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForWASeniorCaseworker())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(requestBody)
            .when()
            .post(taskManagementUrl + "/task/extended-search");

        result.then().assertThat()
            .statusCode(expectedStatus)
            .contentType(APPLICATION_JSON_VALUE)
            .body("tasks.size()", is(expectedTasks));

        return result;
    }

    public Response retrieveTaskRolePermissions(String taskId,
                                              int expectedNumberOfRoles,
                                              int expectedStatus) {

        Response result = given()
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForWASeniorCaseworker())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get(taskManagementUrl + "/task/" + taskId + "/roles");

        result.then().assertThat()
            .statusCode(expectedStatus)
            .contentType(APPLICATION_JSON_VALUE)
            .body("roles.size()", is(expectedNumberOfRoles));

        return result;
    }
}
