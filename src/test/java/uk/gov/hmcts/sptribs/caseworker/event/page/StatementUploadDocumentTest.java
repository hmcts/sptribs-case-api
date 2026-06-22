package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.statement.model.StatementUpload;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class StatementUploadDocumentTest {

    private static final String DOCUMENT_VALIDATION_MESSAGE = "Please upload valid document";

    @InjectMocks
    private StatementUploadDocument statementUploadDocument;

    @Test
    void midEventReturnsNoErrorsWhenDocumentIsValid() {
        CaseDetails<CaseData, State> caseDetails = buildCaseDetails(Document.builder()
            .url("http://dm-store/documents/1")
            .binaryUrl("http://dm-store/documents/1/binary")
            .filename("statement.pdf")
            .build());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            statementUploadDocument.midEvent(caseDetails, new CaseDetails<>());

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void midEventReturnsNoErrorsWhenDocumentIsMissing() {
        CaseDetails<CaseData, State> caseDetails = buildCaseDetails(null);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            statementUploadDocument.midEvent(caseDetails, new CaseDetails<>());

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void midEventReturnsNoErrorsWhenStatementUploadIsMissing() {
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(CaseData.builder().statementUpload(null).build());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            statementUploadDocument.midEvent(caseDetails, new CaseDetails<>());

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void midEventReturnsErrorWhenDocumentUploadIsIncomplete() {
        CaseDetails<CaseData, State> caseDetails = buildCaseDetails(Document.builder()
            .url("http://dm-store/documents/1")
            .filename("statement.pdf")
            .build());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            statementUploadDocument.midEvent(caseDetails, new CaseDetails<>());

        assertThat(response.getErrors()).containsExactly(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void midEventReturnsErrorWhenDocumentFilenameIsBlank() {
        CaseDetails<CaseData, State> caseDetails = buildCaseDetails(Document.builder()
            .url("http://dm-store/documents/1")
            .binaryUrl("http://dm-store/documents/1/binary")
            .filename(" ")
            .build());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            statementUploadDocument.midEvent(caseDetails, new CaseDetails<>());

        assertThat(response.getErrors()).containsExactly(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void midEventReturnsErrorWhenDocumentUrlIsBlank() {
        CaseDetails<CaseData, State> caseDetails = buildCaseDetails(Document.builder()
            .url(" ")
            .binaryUrl("http://dm-store/documents/1/binary")
            .filename("statement.pdf")
            .build());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            statementUploadDocument.midEvent(caseDetails, new CaseDetails<>());

        assertThat(response.getErrors()).containsExactly(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void midEventReturnsErrorWhenDocumentBinaryUrlIsBlank() {
        CaseDetails<CaseData, State> caseDetails = buildCaseDetails(Document.builder()
            .url("http://dm-store/documents/1")
            .binaryUrl(" ")
            .filename("statement.pdf")
            .build());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            statementUploadDocument.midEvent(caseDetails, new CaseDetails<>());

        assertThat(response.getErrors()).containsExactly(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void midEventReturnsErrorWhenDocumentExtensionIsInvalid() {
        CaseDetails<CaseData, State> caseDetails = buildCaseDetails(Document.builder()
            .url("http://dm-store/documents/1")
            .binaryUrl("http://dm-store/documents/1/binary")
            .filename("statement.exe")
            .build());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            statementUploadDocument.midEvent(caseDetails, new CaseDetails<>());

        assertThat(response.getErrors()).containsExactly(DOCUMENT_VALIDATION_MESSAGE);
    }

    private CaseDetails<CaseData, State> buildCaseDetails(Document document) {
        CaseData caseData = CaseData.builder()
            .statementUpload(StatementUpload.builder().statementDocument(document).build())
            .build();
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        return caseDetails;
    }
}
