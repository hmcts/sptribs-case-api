package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class DecisionDocumentListUtilTest {


    @Test
    void shouldGenerateFinalDecisionDocTemp() {
        //Given
        CaseIssueFinalDecision decision = CaseIssueFinalDecision.builder()
            .finalDecisionDraft(Document.builder().filename("name").binaryUrl("d").build())
            .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.setCaseIssueFinalDecision(decision);

        //When
        List<CaseworkerCICDocument> result = DecisionDocumentListUtil.getFinalDecisionDocs(caseData);

        //Then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldGenerateFinalDecisionDoc() {
        //Given
        CaseIssueFinalDecision decision = CaseIssueFinalDecision.builder()
            .document(CICDocument.builder().documentLink(Document.builder().filename("name").binaryUrl("d").build()).build())
            .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.setCaseIssueFinalDecision(decision);

        //When
        List<CaseworkerCICDocument> result = DecisionDocumentListUtil.getFinalDecisionDocs(caseData);

        //Then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldGenerateDecisionDoc() {
        //Given
        CaseIssueDecision decision = CaseIssueDecision.builder()
            .decisionDocument(CICDocument.builder().documentLink(Document.builder().filename("name").binaryUrl("d").build()).build())
            .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.setCaseIssueDecision(decision);

        //When
        List<CaseworkerCICDocument> result = DecisionDocumentListUtil.getDecisionDocs(caseData);

        //Then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldGenerateDecisionDocTemp() {
        //Given
        CaseIssueDecision decision = CaseIssueDecision.builder()
            .issueDecisionDraft(Document.builder().filename("name").binaryUrl("d").build())
            .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.setCaseIssueDecision(decision);

        //When
        List<CaseworkerCICDocument> result = DecisionDocumentListUtil.getDecisionDocs(caseData);

        //Then
        assertThat(result).isNotNull();
    }
}
