package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.DOCUMENT_VALIDATION_MESSAGE;

@ExtendWith(MockitoExtension.class)
public class IssueDecisionUploadNoticeTest {

    @InjectMocks
    private IssueDecisionUploadNotice issueDecisionUploadNotice;

    @Test
    void shouldValidateUploadedDocument() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CICDocument doc = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("file.txt").build()).build();
        final CaseData caseData = CaseData.builder()
            .caseIssueDecision(CaseIssueDecision.builder().decisionDocument(doc).build())
            .caseIssueFinalDecision(CaseIssueFinalDecision.builder().document(doc).build())
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = issueDecisionUploadNotice.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors().contains(DOCUMENT_VALIDATION_MESSAGE)).isTrue();
    }
}
