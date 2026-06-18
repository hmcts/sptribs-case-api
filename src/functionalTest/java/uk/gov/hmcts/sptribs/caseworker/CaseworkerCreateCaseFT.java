package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.List;
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

    private static final String BAD_REQUEST = "classpath:request/casedata/ccd-callback-casedata-create-case-submitted-unhappy.json";
    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-create-case-about-to-submit.json";
    private static final String SUBMITTED_REQUEST = "classpath:request/casedata/ccd-callback-casedata-create-case-submitted.json";

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
        final Map<String, Object> caseData = caseData(SUBMITTED_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_CASE_EVENT_ID, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        final String confirmationHeader = JsonPath.from(response.asString()).getString("confirmation_header");
        assertThat(confirmationHeader)
            .as("confirmation header should contain the generated case reference")
            .contains("# Case Created", "## Case reference number:")
            .matches("(?s).*##\\s*[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}.*");

        long testCaseRef = Long.parseLong(caseData.get("hyphenatedCaseRef").toString().replace("-", ""));

        List<DocumentEntity> documentEntities = caseDocumentsFTDataManager.getDocumentEntities(testCaseRef);
        assertThat(documentEntities).hasSize(1);

        DocumentEntity firstDocumentEntity = documentEntities.getFirst();

        assertThat(firstDocumentEntity.getId()).isNotNull();
        assertThat(firstDocumentEntity.getCaseReferenceNumber()).isEqualTo(Long.parseLong(caseData.get("hyphenatedCaseRef")
            .toString().replace("-", "")));
        assertThat(firstDocumentEntity.getDocumentTypeName()).isEqualTo(DocumentType.APPLICATION_FORM.name());
        assertThat(firstDocumentEntity.getCaseDocumentTypeId()).isEqualTo(1L);
        assertThat(firstDocumentEntity.getSavedAt()).isNotNull();
        assertThat(firstDocumentEntity.isDraft()).isFalse();
        assertThat(firstDocumentEntity.isSentToApplicantViaContactParties()).isFalse();
        assertThat(firstDocumentEntity.getDocumentUrl()).isNotNull();
        assertThat(firstDocumentEntity.getDocumentFilename()).isNotNull();
        assertThat(firstDocumentEntity.getDocumentBinaryUrl()).isNotNull();
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
