package uk.gov.hmcts.sptribs.controllers;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@SpringBootTest
public class CicaCaseControllerFT extends FunctionalTestSuite {

    @Test
    public void shouldReturnOkWhenAppealExistsAndCitizenHasAccess() {
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
    }
}
