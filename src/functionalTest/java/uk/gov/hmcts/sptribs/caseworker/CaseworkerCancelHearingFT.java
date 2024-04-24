package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.caseworker.util.EventConstants;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.HashMap;
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
public class CaseworkerCancelHearingFT extends FunctionalTestSuite {

    private static final String REQUEST_ABOUT_TO_SUBMIT =
        "classpath:request/casedata/ccd-callback-casedata-cancel-hearing-about-to-submit.json";
    private static final String REQUEST_SUBMITTED_ONE_PARTY =
        "classpath:request/casedata/ccd-callback-casedata-cancel-hearing-submitted.json";
    private static final String REQUEST_ALL_PARTIES_NULL = "classpath:request/casedata/ccd-callback-casedata.json";
    private static final String REQUEST_ONE_PARTY_INVALID =
        "classpath:request/casedata/ccd-callback-casedata-cancel-hearing-submitted_invalid_party.json";

    private static final String RESPONSE_ABOUT_TO_SUBMIT = "classpath:responses/response-caseworker-cancel-hearing-about-to-submit.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";
    private static final String ERROR_MESSAGE = "$.errors";

    @Test
    public void shouldCancelHearingWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_ABOUT_TO_SUBMIT);

        final Response response = triggerCallback(caseData, EventConstants.CASEWORKER_CANCEL_HEARING, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE_ABOUT_TO_SUBMIT)));
    }

    @Test
    public void shouldReceiveNoticeWhenOnePartyIsSubmittedIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_SUBMITTED_ONE_PARTY);

        final Response response = triggerCallback(caseData, EventConstants.CASEWORKER_CANCEL_HEARING, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Hearing cancelled \\n## A notification has been sent to: Subject");
    }

    @Test
    public void shouldRecieveErrorIfInvalidRequestIsInvoked() throws Exception {
        JSONObject emptyJsonObject = new JSONObject();
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("emptyString", "");
        caseData.put("emptyJsonObject", emptyJsonObject);

        final Response response = triggerCallback(caseData, EventConstants.CASEWORKER_CANCEL_HEARING, SUBMITTED_URL);

        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Cancel hearing notification failed \\n## Please resend the notification");
    }

    @Test
    public void shouldRaiseErrorIfSubjectRepresentativeRespondentNotifyPartiesAllNull() throws Exception {
        Map<String, Object> caseData = caseData(REQUEST_ALL_PARTIES_NULL);

        Response response = triggerCallback(caseData, EventConstants.CASEWORKER_CANCEL_HEARING, RECORD_NOTIFY_PARTIES_MID_EVENT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(ERROR_MESSAGE)
            .isEqualTo("[\"One recipient must be selected.\"]");
    }

    @Test
    public void shouldRaiseErrorIfOnePartyIsInvalid() throws Exception {
        Map<String, Object> caseData = caseData(REQUEST_ONE_PARTY_INVALID);

        Response response = triggerCallback(caseData, EventConstants.CASEWORKER_CANCEL_HEARING, RECORD_NOTIFY_PARTIES_MID_EVENT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(ERROR_MESSAGE)
            .isEqualTo("[\"One recipient must be selected.\"]");
    }
}
