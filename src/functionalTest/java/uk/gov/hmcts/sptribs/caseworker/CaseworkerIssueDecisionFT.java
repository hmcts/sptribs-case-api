package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_ISSUE_DECISION;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ISSUE_DECISION_MID_EVENT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerIssueDecisionFT extends FunctionalTestSuite {

    private static final String REQUEST_ABOUT_TO_START = "classpath:request/casedata/ccd-callback-casedata-general.json";
    private static final String CALLBACK_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-issue-decision-callback-request.json";
    private static final String REQUEST_SUBMITTED_UNHAPPY_PATH =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-issue-decision-bad-submitted.json";

    private static final String RESPONSE_MID_EVENT = "classpath:responses/response-caseworker-issue-decision-mid-event.json";

    private static final String DECISION_SIGNATURE = "$.data.decisionSignature";
    private static final String STATE = "$.state";
    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldAddDecisionSignatureFieldWhenAboutToStartCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_ABOUT_TO_START);
        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_DECISION, ABOUT_TO_START_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(DECISION_SIGNATURE)
            .isString()
            .isEmpty();
    }

    @Test
    public void shouldAddValidDecisionSignatureAndCreateDraftDocumentWhenMidEventCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_DECISION, ISSUE_DECISION_MID_EVENT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE_MID_EVENT)));
    }

    @Test
    public void shouldChangeStateWhenAboutToSubmitEventCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(STATE)
            .isString()
            .isEqualTo("CaseManagement");
    }

    @Test
    public void shouldSuccessfullySendNotificationWhenSubmittedEventCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_DECISION, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .isEqualTo("# Decision notice issued \n## A notification has been sent to");
    }

    @Test
    public void shouldUnsuccessfullySendNotificationWhenBadSubmittedEventCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_SUBMITTED_UNHAPPY_PATH);
        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_DECISION, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .isEqualTo("# Issue a decision notification failed \n## Please resend the notification");
    }
}
