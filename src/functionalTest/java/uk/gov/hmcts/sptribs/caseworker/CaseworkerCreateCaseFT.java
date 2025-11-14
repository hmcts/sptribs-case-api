package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.path.json.JsonPath;
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
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerCreateCaseFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-caseworker-submit-case.json";
    private static final String BAD_REQUEST = "classpath:request/casedata/ccd-callback-casedata-caseworker-submit-case-failure.json";
    private static final String RESPONSE = "classpath:responses/response-caseworker-submit-case.json";

    private static final String CASEWORKER_CREATE_CASE_EVENT_ID = "caseworker-create-case";
    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldSuccessfullySubmitCaseWhenAboutToSubmitCallbackIsTriggered() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_CASE_EVENT_ID, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldGetConfirmationWhenValidSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_CASE_EVENT_ID, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        final String confirmationHeader = JsonPath.from(response.asString()).getString("confirmation_header");
        assertThat(confirmationHeader)
            .as("confirmation header should contain the generated case reference")
            .contains("# Case Created", "## Case reference number:")
            .matches("(?s).*##\\s*[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}.*");
    }

    @Test
    public void shouldGetFailureWhenInvalidSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(BAD_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_CASE_EVENT_ID, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Create case notification failed \\n## Please resend the notification");
    }

}
