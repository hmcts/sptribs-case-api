package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.get2Document;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getDocument;

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

    @Test
    void shouldRemoveFinalDecision() {
        //Given
        final CaseData caseData = caseData();
        CICDocument doc = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        caseData.setCaseIssueFinalDecision(CaseIssueFinalDecision.builder().document(doc).build());

        CicCase cicCase = CicCase.builder()
            .orderDocumentList(getDocument())
            .decisionDocumentList(new ArrayList<>())
            .finalDecisionDocumentList(new ArrayList<>())
            .applicantDocumentsUploaded(getDocument())
            .build();
        caseData.setCicCase(cicCase);

        final CaseData oldData = caseData();

        CICDocument docOld = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        oldData.setCaseIssueFinalDecision(CaseIssueFinalDecision.builder().document(docOld).build());
        CicCase cicCaseOld = CicCase.builder()
            .decisionDocumentList(get2Document())
            .finalDecisionDocumentList(get2Document())
            .applicantDocumentsUploaded(get2Document())
            .reinstateDocuments(get2Document())
            .build();
        oldData.setCicCase(cicCaseOld);

        //When
        DecisionDocumentListUtil.removeDecisionDoc(caseData, oldData);

    }

    @Test
    void shouldRemoveDecisionDoc() {
        //Given
        final CaseData caseData = caseData();
        CICDocument doc = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        caseData.setCaseIssueDecision(CaseIssueDecision.builder().decisionDocument(doc).build());

        CicCase cicCase = CicCase.builder()
            .orderDocumentList(getDocument())
            .decisionDocumentList(new ArrayList<>())
            .finalDecisionDocumentList(new ArrayList<>())
            .applicantDocumentsUploaded(getDocument())
            .build();
        caseData.setCicCase(cicCase);

        final CaseData oldData = caseData();

        CICDocument docOld = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        oldData.setCaseIssueDecision(CaseIssueDecision.builder().decisionDocument(docOld).build());

        CicCase cicCaseOld = CicCase.builder()
            .decisionDocumentList(get2Document())
            .finalDecisionDocumentList(get2Document())
            .applicantDocumentsUploaded(get2Document())
            .reinstateDocuments(get2Document())
            .build();
        oldData.setCicCase(cicCaseOld);

        //When
        DecisionDocumentListUtil.removeDecisionDoc(caseData, oldData);

    }
}
