package uk.gov.hmcts.sptribs.controllers;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@SpringBootTest
public class CicaRoleAssignmentFT extends FunctionalTestSuite {

    private static final String CREATOR_ROLE = "[CREATOR]";
    private static final String CASE_POSTCODE = "SW1A 1AA";

    @Autowired
    private CaseAssignmentApi caseAssignmentApi;

    @Test
    public void shouldNotAssignCreatorRoleToCitizenAfterListingDocuments() {
        String caseId = createCaseInCcd(true).getId().toString();
        String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();
        String citizenId = idamTokenGenerator.getUserInfoFor(citizenToken).getUid();

        assertThat(caseRolesForCitizen(caseId, citizenId)).isEmpty();

        Response response = getDocuments(caseId, citizenToken, CASE_POSTCODE);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        // Listing is read-only; creator roles are assigned through role assignment or document download.
        assertThat(caseRolesForCitizen(caseId, citizenId))
            .as("Listing documents must not assign case roles to the citizen")
            .isEmpty();
    }

    @Test
    public void shouldNotAssignCreatorRoleWhenPostcodeDoesNotMatch() {
        String caseId = createCaseInCcd(true).getId().toString();
        String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();
        String citizenId = idamTokenGenerator.getUserInfoFor(citizenToken).getUid();

        Response response = getDocuments(caseId, citizenToken, "INVALID");

        assertThat(response.getStatusCode()).isEqualTo(UNAUTHORIZED.value());
        assertThat(caseRolesForCitizen(caseId, citizenId))
            .noneMatch(role -> CREATOR_ROLE.equals(role.getCaseRole()));
    }

    @Test
    public void shouldAssignCreatorRoleToCaseMemberWithoutDuplicatingIt() {
        String caseId = createCaseInCcd(true).getId().toString();
        String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();
        String citizenId = idamTokenGenerator.getUserInfoFor(citizenToken).getUid();

        assertThat(caseRolesForCitizen(caseId, citizenId)).isEmpty();

        Response response = assignCreatorRole(caseId, citizenToken);

        assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT.value());
        assertThat(caseRolesForCitizen(caseId, citizenId))
            .filteredOn(role -> CREATOR_ROLE.equals(role.getCaseRole()))
            .hasSize(1);

        Response repeatedResponse = assignCreatorRole(caseId, citizenToken);

        assertThat(repeatedResponse.getStatusCode()).isEqualTo(NO_CONTENT.value());
        assertThat(caseRolesForCitizen(caseId, citizenId))
            .filteredOn(role -> CREATOR_ROLE.equals(role.getCaseRole()))
            .hasSize(1);
    }

    @Test
    public void shouldNotAssignCreatorRoleToCitizenWhoDoesNotBelongToCase() {
        String caseId = createCaseInCcd(false).getId().toString();
        String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();
        String citizenId = idamTokenGenerator.getUserInfoFor(citizenToken).getUid();

        Response response = assignCreatorRole(caseId, citizenToken);

        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN.value());
        assertThat(caseRolesForCitizen(caseId, citizenId))
            .isEmpty();
    }

    @Test
    public void shouldNotAssignCreatorRoleForNonexistentCase() {
        String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();

        Response response = assignCreatorRole("1234567890123456", citizenToken);

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND.value());
    }

    private Response getDocuments(String caseId, String citizenToken, String postcode) {
        return given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateCcdDataToken())
            .header(AUTHORIZATION, citizenToken)
            .header("X-Postcode", postcode)
            .when()
            .get("/cases/CIC/" + caseId + "/documents");
    }

    private Response assignCreatorRole(String caseId, String citizenToken) {
        return given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateCcdDataToken())
            .header(AUTHORIZATION, citizenToken)
            .when()
            .post("/cases/cica/" + caseId + "/role-assignments");
    }

    private List<CaseAssignmentUserRole> caseRolesForCitizen(String caseId, String citizenId) {
        return caseAssignmentApi.getUserRoles(
            idamTokenGenerator.generateIdamTokenForSystemUser(),
            serviceAuthenticationGenerator.generate(),
            caseId,
            citizenId
        ).getCaseAssignmentUserRoles();
    }
}
