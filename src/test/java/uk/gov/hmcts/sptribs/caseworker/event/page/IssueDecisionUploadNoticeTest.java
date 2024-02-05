package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.DocumentUtil;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.DOCUMENT_VALIDATION_MESSAGE;


@ExtendWith(MockitoExtension.class)
public class IssueDecisionUploadNoticeTest {

    @InjectMocks
    private IssueDecisionUploadNotice issueDecisionUploadNotice;

    private final Document invalidDocumentType = Document.builder().filename("file.jar").build();

    private final Document validDocumentType = Document.builder().filename("file.pdf").build();

    @Test
    void midEventReturnsErrorForInvalidDocumentType() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CICDocument doc = CICDocument.builder().documentLink(invalidDocumentType).build();
        final CaseData caseData = CaseData.builder()
            .caseIssueDecision(CaseIssueDecision.builder().decisionDocument(doc).build())
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = issueDecisionUploadNotice.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).contains(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void midEventReturnsNoErrorsForValidDocumentType() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CICDocument doc = CICDocument.builder().documentLink(validDocumentType).build();
        final CaseData caseData = CaseData.builder()
            .caseIssueDecision(CaseIssueDecision.builder().decisionDocument(doc).build())
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = issueDecisionUploadNotice.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void midEventValidatesDecisionDocumentFormat() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CICDocument doc = CICDocument.builder().documentLink(validDocumentType).build();
        final CaseData caseData = CaseData.builder()
            .caseIssueDecision(CaseIssueDecision.builder().decisionDocument(doc).build())
            .build();
        caseDetails.setData(caseData);
        try (MockedStatic<DocumentUtil> mockedDocumentUtils = Mockito.mockStatic(DocumentUtil.class)) {
            mockedDocumentUtils.when(() -> DocumentUtil.validateDecisionDocumentFormat(any(CICDocument.class)))
                .thenReturn(Collections.emptyList());

            issueDecisionUploadNotice.midEvent(caseDetails, caseDetails);

            mockedDocumentUtils.verify(() ->  DocumentUtil.validateDecisionDocumentFormat(any(CICDocument.class)), times(1));
        }
    }
}
