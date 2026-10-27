package uk.gov.hmcts.sptribs.controllers;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CACHE_CONTROL;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@SpringBootTest
public class CicaCaseControllerFT extends FunctionalTestSuite {

    @Value("${postgres.host}")
    private String postgresHost;

    @Value("${postgres.port}")
    private String postgresPort;

    @Value("${postgres.name}")
    private String postgresName;

    @Value("${postgres.username}")
    private String postgresUsername;

    @Value("${postgres.password}")
    private String postgresPassword;

    @Test
    public void shouldReturnOkWithoutCaseDataWhenAppealExistsAndCitizenHasAccess() {
        final Long appealIdForAppealThatExists = createCaseInCcd(true).getId();

        final Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateCcdDataToken())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
            .when()
            .get("/cases/cica/" + appealIdForAppealThatExists + "/access");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThat(response.getBody().asString()).isEmpty();
        assertThat(response.getHeader(CACHE_CONTROL)).isEqualTo("no-store");
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"Draft", "DSS_Draft"})
    public void shouldAllowCitizenAccessToDraftAppeal(State draftState) throws SQLException {
        Long appealId = createCaseInCcd(true).getId();
        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", postgresHost, postgresPort, postgresName);

        try (Connection connection = DriverManager.getConnection(jdbcUrl, postgresUsername, postgresPassword);
             PreparedStatement statement = connection.prepareStatement("UPDATE ccd.case_data SET state = ? WHERE reference = ?")) {
            statement.setString(1, draftState.getName());
            statement.setLong(2, appealId);
            assertThat(statement.executeUpdate()).isEqualTo(1);
        }

        Response response = RestAssured.given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateCcdDataToken())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
            .when()
            .get("/cases/cica/" + appealId + "/access");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThat(response.getBody().asString()).isEmpty();
    }

    @Test
    public void shouldReturnForbiddenWhenAppealExistsAndCitizenDoesNotHaveAccess() {
        final Long appealIdForAppealWithoutMatchingEmail = createCaseInCcd(false).getId();

        final Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateCcdDataToken())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
            .when()
            .get("/cases/cica/" + appealIdForAppealWithoutMatchingEmail + "/access");

        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN.value());
    }

    @Test
    public void shouldReturnNotFoundWhenAppealDoesNotExist() {
        final long appealIdForAppealThatDoesNotExist = 1234567890123456L;

        final Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateCcdDataToken())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
            .when()
            .get("/cases/cica/" + appealIdForAppealThatDoesNotExist + "/access");

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND.value());
        assertThat(response.getBody().asString()).isEmpty();
    }

    @Test
    public void shouldRejectAccessCheckWithoutServiceAuthorisation() {
        Response response = RestAssured.given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
            .when()
            .get("/cases/cica/1234567890123456/access");

        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN.value());
    }

    @Test
    public void shouldVerifySubjectWithoutReturningCaseDetails() throws SQLException {
        String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();
        Long caseId = createCaseWithSubject();

        Response response = verifySubject(caseId, citizenToken, " Subject Name ", "2000-01-01");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThat(response.getHeader(CACHE_CONTROL)).isEqualTo("no-store");
        assertThat(response.getBody().asString()).isEqualTo("{\"verified\":true}");
    }

    @Test
    public void shouldNotRevealWhichSubjectDetailDoesNotMatch() throws SQLException {
        String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();
        Long caseId = createCaseWithSubject();

        Response incorrectName = verifySubject(caseId, citizenToken, "subject name", "2000-01-01");
        Response incorrectDate = verifySubject(caseId, citizenToken, "Subject Name", "2000-01-02");

        assertThat(incorrectName.getStatusCode()).isEqualTo(OK.value());
        assertThat(incorrectName.getBody().asString()).isEqualTo("{\"verified\":false}");
        assertThat(incorrectDate.getStatusCode()).isEqualTo(OK.value());
        assertThat(incorrectDate.getBody().asString()).isEqualTo(incorrectName.getBody().asString());
    }

    @Test
    public void shouldNotVerifyWhenStoredSubjectDetailsAreMissing() {
        String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();
        Long caseId = createCaseInCcd(true).getId();

        Response response = verifySubject(caseId, citizenToken, "Subject Name", "2000-01-01");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThat(response.getBody().asString()).isEqualTo("{\"verified\":false}");
    }

    @Test
    public void shouldNotVerifySubjectForUnassociatedCitizen() {
        String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();
        Long caseId = createCaseInCcd(false).getId();

        Response response = verifySubject(caseId, citizenToken, "Subject Name", "2000-01-01");

        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN.value());
        assertThat(response.getBody().asString()).isEmpty();
    }

    @Test
    public void shouldNotDiscloseWhetherCaseExistsDuringSubjectVerification() {
        String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();

        Response response = verifySubject(1234567890123456L, citizenToken, "Subject Name", "2000-01-01");

        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN.value());
        assertThat(response.getBody().asString()).isEmpty();
    }

    @Test
    public void shouldRejectMalformedSubjectDate() {
        String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();

        Response response = verifySubject(1234567890123456L, citizenToken, "Subject Name", "not-a-date");

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST.value());
    }

    private Long createCaseWithSubject() throws SQLException {
        Long caseId = createCaseInCcd(true).getId();
        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", postgresHost, postgresPort, postgresName);
        String sql = "UPDATE ccd.case_data SET data = jsonb_set("
            + "jsonb_set(data, '{cicCaseFullName}', to_jsonb(CAST(? AS text)), true), "
            + "'{cicCaseDateOfBirth}', to_jsonb(CAST(? AS text)), true) WHERE reference = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, postgresUsername, postgresPassword);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "Subject Name");
            statement.setString(2, "2000-01-01");
            statement.setLong(3, caseId);
            assertThat(statement.executeUpdate()).isEqualTo(1);
        }
        return caseId;
    }

    private Response verifySubject(Long caseId, String citizenToken, String subjectName, String dateOfBirth) {
        return RestAssured.given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateCcdDataToken())
            .header(AUTHORIZATION, citizenToken)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(Map.of("subject_full_name", subjectName, "subject_date_of_birth", dateOfBirth))
            .when()
            .post("/cases/cica/" + caseId + "/subject-verifications");
    }
}
