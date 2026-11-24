package uk.gov.hmcts.sptribs.controllers;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;
import uk.gov.hmcts.sptribs.testutil.data.CaseDocumentsFTDataManager;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

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

    @Value("${feature.role-assignment.enabled}")
    private boolean roleAssignmentEnabled;

    @Test
    public void shouldNotAssignCreatorRoleToCitizenAfterListingDocuments() {
        String caseId = createCaseInCcd(true).getId().toString();
        String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();
        String citizenId = idamTokenGenerator.getUserInfoFor(citizenToken).getUid();

        assertThat(caseRolesForCitizen(caseId, citizenId, citizenToken)).isEmpty();

        Response response = getDocuments(caseId, citizenToken, CASE_POSTCODE);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        // Listing is read-only; creator roles are assigned through role assignment or document download.
        assertThat(caseRolesForCitizen(caseId, citizenId, citizenToken))
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
        assertThat(caseRolesForCitizen(caseId, citizenId, citizenToken))
            .noneMatch(role -> CREATOR_ROLE.equals(role.getCaseRole()));
    }

    @Test
    public void shouldAssignCreatorRoleOnlyWhenEnabledWithoutDuplicatingIt() {
        String caseId = createCaseInCcd(true).getId().toString();
        String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();
        String citizenId = idamTokenGenerator.getUserInfoFor(citizenToken).getUid();

        assertThat(caseRolesForCitizen(caseId, citizenId, citizenToken)).isEmpty();

        Response response = assignCreatorRole(caseId, citizenToken);

        assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT.value());
        assertThat(caseRolesForCitizen(caseId, citizenId, citizenToken))
            .filteredOn(role -> CREATOR_ROLE.equals(role.getCaseRole()))
            .hasSize(roleAssignmentEnabled ? 1 : 0);

        Response repeatedResponse = assignCreatorRole(caseId, citizenToken);

        assertThat(repeatedResponse.getStatusCode()).isEqualTo(NO_CONTENT.value());
        assertThat(caseRolesForCitizen(caseId, citizenId, citizenToken))
            .filteredOn(role -> CREATOR_ROLE.equals(role.getCaseRole()))
            .hasSize(roleAssignmentEnabled ? 1 : 0);
    }

    @Test
    public void shouldNotAssignCreatorRoleToCitizenWhoDoesNotBelongToCase() {
        String caseId = createCaseInCcd(false).getId().toString();
        String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();
        String citizenId = idamTokenGenerator.getUserInfoFor(citizenToken).getUid();

        Response response = assignCreatorRole(caseId, citizenToken);

        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN.value());
        assertThat(caseRolesForCitizen(caseId, citizenId, citizenToken))
            .isEmpty();
    }

    @Test
    @DisabledIf(expression = "${feature.role-assignment.enabled}", loadContext = true)
    public void shouldDownloadSystemOwnedDocumentWithoutAssigningCreatorRole() throws Exception {
        long caseId = createCaseInCcd(true).getId();
        String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();
        String citizenId = idamTokenGenerator.getUserInfoFor(citizenToken).getUid();
        ClassPathResource pdf = new ClassPathResource("data/sample_file.pdf");
        var document = uploadTestDocument(pdf, "test-download.pdf").getDocuments().getFirst();
        String url = document.links.self.href;
        String documentId = url.substring(url.lastIndexOf('/') + 1);
        CaseDocumentsFTDataManager.saveTestDocumentEntity(caseId, url, document.originalDocumentName,
            "APPLICATION_FORM", "2", Timestamp.from(Instant.now()));
        assertThat(caseRolesForCitizen(Long.toString(caseId), citizenId, citizenToken)).isEmpty();

        Response response = given().relaxedHTTPSValidation().baseUri(testUrl)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateCcdDataToken())
            .header(AUTHORIZATION, citizenToken)
            .header("X-Postcode", CASE_POSTCODE)
            .when().get("/cases/CIC/" + caseId + "/documents/" + documentId + "/download");

        assertThat(response.statusCode()).isEqualTo(OK.value());
        assertThat(response.asByteArray()).isEqualTo(pdf.getContentAsByteArray());
        assertThat(caseRolesForCitizen(Long.toString(caseId), citizenId, citizenToken)).isEmpty();
    }

    @Test
    public void shouldRejectDownloadOfDocumentNotOnCaseWithoutGrantingRoles() {
        String caseId = createCaseInCcd(true).getId().toString();
        String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();
        String citizenId = idamTokenGenerator.getUserInfoFor(citizenToken).getUid();

        given().relaxedHTTPSValidation().baseUri(testUrl)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateCcdDataToken())
            .header(AUTHORIZATION, citizenToken)
            .header("X-Postcode", CASE_POSTCODE)
            .when().get("/cases/CIC/" + caseId + "/documents/" + UUID.randomUUID() + "/download")
            .then().statusCode(NOT_FOUND.value());

        assertThat(caseRolesForCitizen(caseId, citizenId, citizenToken)).isEmpty();
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

    private List<CaseAssignmentUserRole> caseRolesForCitizen(String caseId, String citizenId, String citizenToken) {
        return caseAssignmentApi.getUserRoles(
            citizenToken,
            serviceAuthenticationGenerator.generate(),
            caseId,
            citizenId
        ).getCaseAssignmentUserRoles();
    }
}
