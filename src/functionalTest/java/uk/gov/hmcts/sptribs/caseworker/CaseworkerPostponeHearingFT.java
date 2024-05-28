package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.caseworker.model.PostponeReason;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.ArrayList;
import java.util.List;
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

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    private static final String REQUEST_ABOUT_TO_START =
        "classpath:request/casedata/ccd-callback-casedata-general.json";
    private static final String REQUEST_ABOUT_TO_SUBMIT =
        "classpath:request/casedata/ccd-callback-casedata-postpone-hearing-about-to-submit.json";
    private static final String REQUEST_SUBMITTED = "classpath:request/casedata/ccd-callback-casedata-postpone-hearing-submitted.json";

    private static final String RESPONSE_ABOUT_TO_START =
        "classpath:responses/response-caseworker-postpone-hearing-about-to-start.json";
    private static final String RESPONSE_ABOUT_TO_SUBMIT =
        "classpath:responses/response-caseworker-postpone-hearing-about-to-submit.json";

    private static final String TRACE_HEADER = "$.trace";

    @Test
    public void shouldConfirmHearingPostponedNotificationIsSentToSubjectForAllGivenReasonsWhenSubmittedIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_SUBMITTED);

        for(PostponeReason postponeReason : PostponeReason.values()) {
            caseData.put("postponeReason", postponeReason.getReason());

            final Response response = triggerCallback(caseData, CASEWORKER_POSTPONE_HEARING, SUBMITTED_URL);

            assertThat(response.getStatusCode()).isEqualTo(OK.value());
            assertThatJson(response.asString())
                .inPath(CONFIRMATION_HEADER)
                .isString()
                .contains("# Hearing Postponed \n## The hearing has been postponed, the case has been updated \n"
                    + "## A notification has been sent to: Subject");
        }
    }

    @Test
    public void shouldInitialiseTheHearingListAndSetTheCurrentEventWhenAboutToStartCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_ABOUT_TO_START);

        final Response response = triggerCallback(caseData, CASEWORKER_POSTPONE_HEARING, ABOUT_TO_START_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_ARRAY_ORDER)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE_ABOUT_TO_START)));
    }

    @Test
    public void shouldReceiveErrorIfInvalidReasonIsSubmitted() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_SUBMITTED);

        ArrayList<String> invalidReasons = new ArrayList<>(List.of(
            "invalid reason", "APPELLANT_IS_OUT_OF_COUNTRY"));

        for(String invalidReason : invalidReasons) {
            caseData.put("postponeReason", invalidReason);

            final Response response = triggerCallback(caseData, CASEWORKER_POSTPONE_HEARING, SUBMITTED_URL);

            assertThatJson(response.asString())
                .inPath(TRACE_HEADER)
                .isString()
                .contains("Cannot deserialize value of type `uk.gov.hmcts.sptribs.caseworker.model.PostponeReason` from String \"" +
                    invalidReason + "\": not one of the values accepted for Enum class:");
        }

        ArrayList<String> emptyReasons = new ArrayList<>(List.of("", " "));

        for(String emptyReason : emptyReasons) {
            caseData.put("postponeReason", emptyReason);

            final Response response = triggerCallback(caseData, CASEWORKER_POSTPONE_HEARING, SUBMITTED_URL);

            assertThatJson(response.asString())
                .inPath(TRACE_HEADER)
                .isString()
                .contains("Cannot coerce empty String");
        }

    }

    @Test
    public void shouldResetCurrentEventAndSetStatusesAndSetPostponeDateWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_ABOUT_TO_SUBMIT);

        for(PostponeReason postponeReason : PostponeReason.values()) {
            caseData.put("postponeReason", postponeReason.getReason());

            final Response response = triggerCallback(caseData, CASEWORKER_POSTPONE_HEARING, ABOUT_TO_SUBMIT_URL);

            JSONObject responseAboutToSubmit = new JSONObject(expectedResponse(RESPONSE_ABOUT_TO_SUBMIT));
            JSONObject responseAboutToSubmitData = new JSONObject((responseAboutToSubmit.get("data")).toString());
            responseAboutToSubmitData.put("postponeReason", postponeReason.getReason());
            responseAboutToSubmit.put("data", responseAboutToSubmitData);

            assertThat(response.getStatusCode()).isEqualTo(OK.value());
            assertThatJson(response.asString())
                .when(IGNORING_ARRAY_ORDER)
                .when(IGNORING_EXTRA_FIELDS)
                .isEqualTo(responseAboutToSubmit);
        }
    }
}
