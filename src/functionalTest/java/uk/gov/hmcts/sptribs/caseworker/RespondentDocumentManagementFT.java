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
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.RESPONDENT_DOCUMENT_MANAGEMENT;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class RespondentDocumentManagementFT extends FunctionalTestSuite {
    public static final String CASE_DOCUMENT_MANAGEMENT_UPLOAD_DOCUMENTS_MID_EVENT_URL
        = "/callbacks/mid-event?page=uploadCaseDocuments";

    private static final String CALLBACK_CASE_DATA = "classpath:request/casedata/ccd-callback-casedata.json";

    private static final String MID_EVENT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-document-management-mid-event.json";
    private static final String MID_EVENT_RESPONSE =
        "classpath:responses/response-caseworker-caseworker-document-management-mid-event.json";

    private static final String ABOUT_TO_SUBMIT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-document-management-about-to-submit.json";
    private static final String ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/response-caseworker-caseworker-document-management-about-to-submit.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldValidateCloseCaseDocumentsInMidEventCallback() throws Exception {
        final Map<String, Object> caseData = caseData(MID_EVENT_REQUEST);

        final Response response = triggerCallback(
            caseData,
            RESPONDENT_DOCUMENT_MANAGEMENT,
            CASE_DOCUMENT_MANAGEMENT_UPLOAD_DOCUMENTS_MID_EVENT_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(MID_EVENT_RESPONSE)));
    }

    @Test
    public void shouldUpdateCategoryToCaseworkerDocumentInAboutToSubmitCallback() throws Exception {
        final Map<String, Object> caseData = caseData(ABOUT_TO_SUBMIT_REQUEST);

        final Response response = triggerCallback(
            caseData,
            RESPONDENT_DOCUMENT_MANAGEMENT,
            ABOUT_TO_SUBMIT_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(ABOUT_TO_SUBMIT_RESPONSE)));
    }

    @Test
    public void shouldSuccessfullySubmitEvent() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_CASE_DATA);

        final Response response = triggerCallback(
            caseData,
            RESPONDENT_DOCUMENT_MANAGEMENT,
            SUBMITTED_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Case Updated");
    }
}
