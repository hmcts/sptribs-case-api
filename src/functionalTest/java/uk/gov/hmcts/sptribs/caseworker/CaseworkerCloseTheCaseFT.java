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
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CLOSE_THE_CASE;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerCloseTheCaseFT extends FunctionalTestSuite {

    public static final String CLOSE_CASE_UPLOAD_DOCUMENTS_MID_EVENT_URL =
        "/callbacks/mid-event?page=closeCaseUploadDocuments";

    private static final String CALLBACK_CASE_DATA = "classpath:request/casedata/ccd-callback-casedata.json";

    private static final String MID_EVENT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-close-case-mid-event.json";
    private static final String MID_EVENT_RESPONSE =
        "classpath:responses/response-caseworker-close-case-mid-event.json";

    private static final String ABOUT_TO_SUBMIT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-close-case-about-to-submit.json";
    private static final String ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/response-caseworker-close-case-about-to-submit.json";

    private static final String SUBMITTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-close-case-submitted.json";
    private static final String SUBMITTED_REQUEST_WITHOUT_EMAIL =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-close-case-without-email-submitted.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldValidateCloseCaseDocumentsInMidEventCallback() throws Exception {
        final Map<String, Object> caseData = caseData(MID_EVENT_REQUEST);

        final Response response = triggerCallback(
            caseData,
            CASEWORKER_CLOSE_THE_CASE,
            CLOSE_CASE_UPLOAD_DOCUMENTS_MID_EVENT_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(MID_EVENT_RESPONSE)));
    }

    @Test
    public void shouldNotReturnErrorsIfCloseCaseDocumentsListEmptyInMidEvent() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_CASE_DATA);

        final Response response = triggerCallback(
            caseData,
            CASEWORKER_CLOSE_THE_CASE,
            CLOSE_CASE_UPLOAD_DOCUMENTS_MID_EVENT_URL
        );

        assertThatJson(response.asString())
            .inPath("$.errors")
            .isArray()
            .isEmpty();
    }

    @Test
    public void shouldPopulateJudgesListInAboutToStartCallback() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_CASE_DATA);

        final Response response = triggerCallback(caseData, CASEWORKER_CLOSE_THE_CASE, ABOUT_TO_START_URL);
        final String responseAsString = response.asString();

        assertThatJson(responseAsString)
            .inPath("$.data.judgeList")
            .isArray()
            .isNotEmpty();

        assertThatJson(responseAsString)
            .inPath("$.data.closeRejectionName.list_items")
            .isArray()
            .isNotEmpty();

        assertThatJson(responseAsString)
            .inPath("$.data.closeStrikeOutName.list_items")
            .isArray()
            .isNotEmpty();
    }

    @Test
    public void shouldUpdateCategoryToCaseworkerDocumentInAboutToSubmitCallback() throws Exception {
        final Map<String, Object> caseData = caseData(ABOUT_TO_SUBMIT_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_CLOSE_THE_CASE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(ABOUT_TO_SUBMIT_RESPONSE)));
    }

    @Test
    public void shouldSendEmailSuccessfullyInSubmittedCallback() throws Exception {
        final Map<String, Object> caseData = caseData(SUBMITTED_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_CLOSE_THE_CASE, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo(
                "# Case closed \\n"
                    + "## A notification has been sent to: Subject \\n"
                    + "## Use 'Reinstate case' if this case needs to be reopened in the future."
            );
    }

    @Test
    public void shouldErrorSubmittedCallback() throws Exception {
        final Map<String, Object> caseData = caseData(SUBMITTED_REQUEST_WITHOUT_EMAIL);

        final Response response = triggerCallback(caseData, CASEWORKER_CLOSE_THE_CASE, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Case close notification failed \\n## Please resend the notification");
    }
}
