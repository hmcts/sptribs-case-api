package uk.gov.hmcts.sptribs.testutil;

import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@Service
public class RoleAssignmentService {

    @Autowired
    private IdamTokenGenerator idamTokenGenerator;

    @Autowired
    private ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Value("${role_assignment_mapping_api.url}")
    private String roleAssignmentUrl;

    @Value("${idam.waseniorcaseworker.uid}")
    private String waSeniorCaseworkerUid;

    public void createRoleAssignmentsForWaSeniorCaseworker() {
        createRoleAssignments(this.waSeniorCaseworkerUid);
    }

    public void createRoleAssignments(String userId) {
        Map<String, Object> requestBody = Map.of(
            "userIds", singletonList(userId)
        );

        Response result = given()
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateAccessManagementToken())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystemUser())
            .queryParam("userType", "CASEWORKER")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(requestBody)
            .when()
            .post(roleAssignmentUrl + "/am/testing-support/createOrgMapping");

        result.then().assertThat()
            .statusCode(200);
    }
}
