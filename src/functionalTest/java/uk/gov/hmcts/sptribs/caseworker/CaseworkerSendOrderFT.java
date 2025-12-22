package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_SEND_ORDER;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerSendOrderFT extends FunctionalTestSuite {

    private static final String GENERAL_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-general.json";
    private static final String CALLBACK_REQUEST = "classpath:request/casedata/ccd-callback-casedata-send-order-callback-request.json";
    private static final String REQUEST_UNHAPPY_ABOUT_TO_SUBMIT =
        "classpath:request/casedata/ccd-callback-casedata-send-order-unhappy-about-to-submit.json";

    private static final String RESPONSE = "classpath:responses/response-caseworker-send-order-about-to-submit.json";
    private static final String ABOUT_TO_START_RESPONSE = "classpath:responses/response-caseworker-send-order-about-to-start.json";
    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldSetCurrentEventInAboutToStartCallback() throws Exception {
        final Map<String, Object> caseData = caseData(GENERAL_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_SEND_ORDER, ABOUT_TO_START_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
                .when(IGNORING_EXTRA_FIELDS)
                .isEqualTo(json(expectedResponse(ABOUT_TO_START_RESPONSE)));
    }

    @Test
    public void shouldRetainSelectedOrderDataWhenAboutToSubmitCallbackIsTriggered() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_SEND_ORDER, ABOUT_TO_SUBMIT_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldReturnHappyResponseWhenValidSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_SEND_ORDER, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Order sent \n## A notification has been sent to: Representative");
    }

    @Test
    public void shouldReturnUnhappyResponseWhenInvalidSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_UNHAPPY_ABOUT_TO_SUBMIT);

        final Response response = triggerCallback(caseData, CASEWORKER_SEND_ORDER, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Send order notification failed \n## Please resend the order");
    }
}
