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
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_ISSUE_CASE;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerIssueCaseFT extends FunctionalTestSuite {

    private static final String CALLBACK_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-issue-case-about-to-start.json";
    private static final String ABOUT_TO_SUBMIT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-issue-case-about-to-submit.json";

    private static final String ABOUT_TO_START_RESPONSE =
        "classpath:responses/response-caseworker-issue-case-about-to-start.json";
    private static final String ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/response-caseworker-issue-case-about-to-submit.json";

    private static final String SUBMITTED_FAILURE_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-issue-case-submitted.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldInitialiseDocumentListWhenAboutToStartCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_CASE, ABOUT_TO_START_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(ABOUT_TO_START_RESPONSE)));
    }

    @Test
    public void shouldBeSuccessfulWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(ABOUT_TO_SUBMIT_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_CASE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(ABOUT_TO_SUBMIT_RESPONSE)));
    }

    @Test
    public void shouldBeSuccessfulWhenSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_CASE, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Case issued \n##  This case has now been issued. \n## A notification has been sent to");
    }

    @Test
    public void shouldReturnFailureMessageWhenEmailCouldNotSendWhenSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(SUBMITTED_FAILURE_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_ISSUE_CASE, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .isEqualTo("""
                # Issue case notification failed\s
                ## A notification could not be sent to: Subject\s
                ## Please resend the notification.""");
    }
}
