package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_REMOVE_STAY;

@SpringBootTest
public class CaseworkerRemoveStayFT extends FunctionalTestSuite {

    private static final String CALLBACK_ABOUT_TO_START =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-remove-stay-about-to-start.json";
    private static final String CALLBACK_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-remove-stay-callback-request.json";
    private static final String CALLBACK_SUBMITTED_UNHAPPY =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-remove-stay-submitted-unhappy.json";

    private static final String STATE = "$.state";
    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldReturnPositiveResponseWhenAboutToStartCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_ABOUT_TO_START);

        final Response response = triggerCallback(caseData, CASEWORKER_REMOVE_STAY, ABOUT_TO_START_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldChangeStateWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_REMOVE_STAY, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(STATE)
            .isString()
            .contains("CaseManagement");
    }

    @Test
    public void shouldReturnHappyResponseWhenValidSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_REMOVE_STAY, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Stay Removed from Case \n## A notification has been sent to: Subject");
    }

    @Test
    public void shouldReturnUnhappyResponseWhenInvalidSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_SUBMITTED_UNHAPPY);

        final Response response = triggerCallback(caseData, CASEWORKER_REMOVE_STAY, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Remove case stay notification failed \n## Please resend the notification");
    }
}
