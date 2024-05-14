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
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_EDIT_RECORD_LISTING;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.EDIT_RECORD_LISTING_MID_EVENT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerEditRecordListingFT extends FunctionalTestSuite {

    private static final String REQUEST_ABOUT_TO_START = "classpath:request/casedata/ccd-callback-casedata-general.json";
    private static final String REQUEST_MID_EVENT =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-edit-record-listing-mid-event.json";
    private static final String CALLBACK_REQUEST_HAPPY =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-edit-record-listing-about-to-submit.json";
    private static final String CALLBACK_REQUEST_UNHAPPY =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-edit-record-listing-no-parties-about-to-submit.json";

    private static final String RESPONSE_ABOUT_TO_START =
        "classpath:responses/response-caseworker-edit-record-listing-about-to-start.json";
    private static final String RESPONSE_MID_EVENT =
        "classpath:responses/response-caseworker-edit-record-listing-mid-event.json";
    private static final String RESPONSE_ABOUT_TO_SUBMIT =
        "classpath:responses/response-caseworker-edit-record-listing-about-to-submit.json";

    private static final String ERROR_MESSAGE = "$.errors";
    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldSetHearingAndRegionFieldsWhenAboutToStartCallBackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_ABOUT_TO_START);

        final Response response = triggerCallback(caseData, CASEWORKER_EDIT_RECORD_LISTING, ABOUT_TO_START_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_ARRAY_ORDER)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE_ABOUT_TO_START)));
    }

    @Test
    public void shouldSetHearingVenueFieldsWhenMidEventCallBackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_MID_EVENT);

        final Response response = triggerCallback(caseData, CASEWORKER_EDIT_RECORD_LISTING, EDIT_RECORD_LISTING_MID_EVENT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_ARRAY_ORDER)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE_MID_EVENT)));
    }

    @Test
    public void shouldSetAdditionalHearingDateAndStateWhenAboutToSubmitCallBackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST_HAPPY);

        final Response response = triggerCallback(caseData, CASEWORKER_EDIT_RECORD_LISTING, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_ARRAY_ORDER)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE_ABOUT_TO_SUBMIT)));
    }

    @Test
    public void shouldContainErrorWhenNoPartiesEnteredWhenAboutToSubmitCallBackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST_UNHAPPY);

        final Response response = triggerCallback(caseData, CASEWORKER_EDIT_RECORD_LISTING, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(ERROR_MESSAGE)
            .isArray()
            .contains("One party must be selected.");
    }

    @Test
    public void shouldTriggerSuccessfulResponseIfSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST_HAPPY);

        final Response response = triggerCallback(caseData, CASEWORKER_EDIT_RECORD_LISTING, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("""
                # Listing record updated\s
                ##  If any changes are made to this hearing,  \
                remember to make those changes in this listing record.\s
                ## A notification has been sent to: Subject""");
    }

    @Test
    public void shouldTriggerUnsuccessfulResponseIfBadSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST_UNHAPPY);

        final Response response = triggerCallback(caseData, CASEWORKER_EDIT_RECORD_LISTING, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Update listing notification failed \n## Please resend the notification");
    }
}
