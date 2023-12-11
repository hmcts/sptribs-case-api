package uk.gov.hmcts.sptribs.caseworker;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

public class CaseworkerCaancelHearingFT extends uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata.json";

    @org.junit.jupiter.api.Test
    public void shouldSuccessfullySaveDraftOrder() throws java.io.IOException {
        // Given
        final java.util.Map<String, Object> caseData = caseData(REQUEST);

        // When
        io.restassured.response.Response
            response =
            triggerCallback(caseData, uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CANCEL_HEARING, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        // Then
        // response.then().statusCode(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-caseworker-contact-parties-about-to-submit.json"
            )));
    }
}
