package uk.gov.hmcts.sptribs.controllers;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.io.IOException;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseManagementControllerFT extends FunctionalTestSuite {

    private static final String CREATE_CASE_RESPONSE =
        "classpath:responses/response-citizen-create-case.json";
    private static final String UPDATE_CASE_RESPONSE =
        "classpath:responses/response-citizen-update-case.json";
    private static final String DSS_UPDATE_CASE_RESPONSE =
        "classpath:responses/response-citizen-dss-update-case.json";

    @Test
    @Ignore
    public void shouldCreateCaseWhenEndpointTriggered() throws IOException {

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
            .body(getDssCaseData())
            .when()
            .post("/case/dss-orchestration/create");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CREATE_CASE_RESPONSE)));
    }

    @Test
    @Ignore
    public void shouldUpdateCaseWhenEndpointTriggered() throws IOException {

        final long caseReference = createTestCaseAndGetCaseReference();

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
            .param(EVENT_PARAM, UPDATE)
            .body(getDssCaseDataUpdated())
            .when()
            .put("/case/dss-orchestration/" + caseReference +  "/update");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(UPDATE_CASE_RESPONSE)));
    }

    @Test
    @Ignore
    public void shouldSubmitCaseWhenEndpointTriggered() throws IOException {

        final long caseReference = createTestCaseAndGetCaseReference();

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
            .param(EVENT_PARAM, SUBMIT)
            .body(getDssCaseDataUpdated())
            .when()
            .put("/case/dss-orchestration/" + caseReference +  "/update");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(UPDATE_CASE_RESPONSE)));
    }

    @Test
    @Ignore
    public void shouldTriggerDssUpdateCaseWhenEndpointTriggered() throws IOException {

        final long caseReference = createAndSubmitTestCaseAndGetCaseReference();

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
            .param(EVENT_PARAM, UPDATE_CASE)
            .body(getDssCaseDataUpdated())
            .when()
            .put("/case/dss-orchestration/" + caseReference +  "/update");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(DSS_UPDATE_CASE_RESPONSE)));
    }

    @Test
    @Ignore
    public void shouldFetchCaseDetailsWhenEndpointTriggered() {

        final long caseReference = createTestCaseAndGetCaseReference();

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
            .when()
            .get("/case/dss-orchestration/fetchCaseDetails/" + caseReference);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
