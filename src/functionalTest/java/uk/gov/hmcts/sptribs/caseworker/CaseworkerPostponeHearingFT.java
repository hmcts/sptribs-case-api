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
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_POSTPONE_HEARING;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerPostponeHearingFT extends FunctionalTestSuite {

    private static final String REQUEST_ABOUT_TO_START =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-postpone-hearing-about-to-start.json";
    private static final String CALLBACK_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-postpone-hearing-callback-request.json";
    private static final String REQUEST_SUBMITTED =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-postpone-hearing-submitted.json";

    private static final String RESPONSE_ABOUT_TO_START =
        "classpath:responses/response-caseworker-postpone-hearing-about-to-start.json";
    private static final String RESPONSE_ABOUT_TO_SUBMIT =
        "classpath:responses/response-caseworker-postpone-hearing-about-to-submit.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldInitialiseDynamicHearingListAndSetCurrentEventWhenAboutToStartCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_ABOUT_TO_START);

        final Response response = triggerCallback(caseData, CASEWORKER_POSTPONE_HEARING, ABOUT_TO_START_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE_ABOUT_TO_START)));
    }

    @Test
    public void shouldSuccessfullySetHearingStatusCurrentEventAndPostponeDateWhenABoutToSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_POSTPONE_HEARING, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE_ABOUT_TO_SUBMIT)));
    }

    @Test
    public void shouldBeUnsuccessfulWhenBadSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_POSTPONE_HEARING, SUBMITTED_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Postpone hearing notification failed \n## Please resend the notification");
    }

    @Test
    public void shouldBeSuccessfulWhenGoodSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_SUBMITTED);

        final Response response = triggerCallback(caseData, CASEWORKER_POSTPONE_HEARING, SUBMITTED_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("""
                # Hearing Postponed\s
                ## The hearing has been postponed, the case has been updated \

                ## A notification has been sent to: Subject""");
    }
}
