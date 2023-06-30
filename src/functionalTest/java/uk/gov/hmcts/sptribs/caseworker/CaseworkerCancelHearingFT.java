package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.caseworker.util.EventConstants;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.RECORD_NOTIFY_PARTIES_MID_EVENT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
@Ignore
public class CaseworkerCancelHearingFT extends FunctionalTestSuite {

    private static final String REQUEST_SUBMIT = "classpath:request/casedata/ccd-callback-casedata-cancel-hearing-about-to-submit.json";
    private static final String REQUEST_SUBMITTED = "classpath:request/casedata/ccd-callback-casedata-cancel-hearing-submitted.json";
    private static final String REQUEST_MID_EVENT = "classpath:request/casedata/ccd-callback-casedata.json";

    @Test
    public void shouldCancelHearingWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_SUBMIT);

        final Response response = triggerCallback(caseData, EventConstants.CASEWORKER_CANCEL_HEARING, ABOUT_TO_SUBMIT_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        // notes.date value is compared using ${json-unit.any-string}
        // assertion will fail if the above value is missing
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-caseworker-cancel-hearing-about-to-submit.json"
            )));
    }

    @Test
    public void shouldReceiveNoticeWhenSubmittedIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_SUBMITTED);

        final Response response = triggerCallback(caseData, EventConstants.CASEWORKER_CANCEL_HEARING, SUBMITTED_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        // notes.date value is compared using ${json-unit.any-string}
        // assertion will fail if the above value is missing
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-caseworker-cancel-hearing-submitted.json"
            )));
    }

    @Test
    public void shouldRaiseErrorIfSubjectRepresentativeRespondentNotifyPartiesAllNull() throws Exception {
        Map<String, Object> caseData = caseData(REQUEST_MID_EVENT);

        Response response = triggerCallback(caseData, EventConstants.CASEWORKER_CANCEL_HEARING, RECORD_NOTIFY_PARTIES_MID_EVENT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-caseworker-cancel-hearing-record-notify-parties-mid-event.json"
            )));
    }
}
