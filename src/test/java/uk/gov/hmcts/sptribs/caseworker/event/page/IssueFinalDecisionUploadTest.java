package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.DocumentConstants;
import uk.gov.hmcts.sptribs.document.model.OrderDecisionUploadCICDocument;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class IssueFinalDecisionUploadTest {
    @InjectMocks
    private IssueFinalDecisionUpload issueFinalDecisionUpload;

    @ParameterizedTest
    @ValueSource(strings = {"file.uml", "file.docx"})
    void midEventShouldReturnErrorsInvalidDocumentUploaded(String filename) {
        //Given
        final OrderDecisionUploadCICDocument document = OrderDecisionUploadCICDocument.builder()
            .documentLink(Document.builder().binaryUrl("url").url("url").filename(filename).build())
            .documentEmailContent("content")
            .build();
        final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder().document(document).build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .caseIssueFinalDecision(caseIssueFinalDecision)
            .build();
        caseDetails.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = issueFinalDecisionUpload.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getErrors()).contains(DocumentConstants.DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void midEventShouldReturnNoErrorsPdfUpload() {
        //Given
        final OrderDecisionUploadCICDocument document = OrderDecisionUploadCICDocument.builder()
            .documentLink(Document.builder().binaryUrl("url").url("url").filename("file.pdf").build())
            .documentEmailContent("content")
            .build();
        final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder().document(document).build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .caseIssueFinalDecision(caseIssueFinalDecision)
            .build();
        caseDetails.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = issueFinalDecisionUpload.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).hasSize(0);
    }
}
