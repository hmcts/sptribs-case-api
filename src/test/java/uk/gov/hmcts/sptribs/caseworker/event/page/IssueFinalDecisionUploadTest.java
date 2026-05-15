package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.DocumentConstants;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class IssueFinalDecisionUploadTest {
    @InjectMocks
    private IssueFinalDecisionUpload issueFinalDecisionUpload;

    @Test
    void shouldReturnErrorsInvalidDocumentUploaded() {
        //Given

        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().binaryUrl("url").url("url").filename("file.sql").build())
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
        assertThat(response.getErrors()).contains(DocumentConstants.DOCUMENT_VALIDATION_MESSAGE);
    }
}