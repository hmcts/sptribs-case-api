package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.util.DocumentManagementUtil.EMPTY_DOCUMENT;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class DecisionDocumentListUtilTest {

    @Test
    void shouldGenerateFinalDecisionDocFromDraft() {
        //Given
        final Document document = Document.builder().filename("name").binaryUrl("d").build();
        final CaseIssueFinalDecision decision = CaseIssueFinalDecision.builder()
            .finalDecisionDraft(document)
            .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.setCaseIssueFinalDecision(decision);

        //When
        final List<CaseworkerCICDocument> result = DecisionDocumentListUtil.getFinalDecisionDocs(caseData);

        //Then
        final CaseworkerCICDocument expectedCaseworkerCICDocument = CaseworkerCICDocument.builder()
            .documentLink(document)
            .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
            .build();
        assertThat(result).hasSize(1).contains(expectedCaseworkerCICDocument);
    }

    @Test
    void shouldGenerateFinalDecisionDocFromCICDocument() {
        //Given
        final Document document = Document.builder().filename("name").binaryUrl("d").build();
        final CICDocument cicDocument = CICDocument.builder()
            .documentLink(document)
            .documentEmailContent("Test content")
            .build();
        final CaseIssueFinalDecision decision = CaseIssueFinalDecision.builder()
            .document(cicDocument)
            .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.setCaseIssueFinalDecision(decision);

        //When
        final List<CaseworkerCICDocument> result = DecisionDocumentListUtil.getFinalDecisionDocs(caseData);

        //Then
        final CaseworkerCICDocument expectedCaseworkerCICDocument = CaseworkerCICDocument.builder()
            .documentEmailContent("Test content")
            .documentLink(document)
            .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
            .build();
        assertThat(result).hasSize(1).contains(expectedCaseworkerCICDocument);
    }

    @Test
    void shouldGenerateDecisionDocFromCICDocument() {
        //Given
        final Document document = Document.builder().filename("name").binaryUrl("d").build();
        final CICDocument cicDocument = CICDocument.builder()
            .documentLink(document)
            .documentEmailContent("Test content")
            .build();
        final CaseIssueDecision decision = CaseIssueDecision.builder()
            .decisionDocument(cicDocument)
            .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.setCaseIssueDecision(decision);

        //When
        final List<CaseworkerCICDocument> result = DecisionDocumentListUtil.getDecisionDocs(caseData);

        //Then
        final CaseworkerCICDocument expectedCaseworkerCICDocument = CaseworkerCICDocument.builder()
            .documentEmailContent("Test content")
            .documentLink(document)
            .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
            .build();
        assertThat(result).hasSize(1).contains(expectedCaseworkerCICDocument);
    }

    @Test
    void shouldGenerateDecisionDocFromDraft() {
        //Given
        final Document document = Document.builder().filename("name").binaryUrl("d").build();
        final CaseIssueDecision decision = CaseIssueDecision.builder()
            .issueDecisionDraft(document)
            .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.setCaseIssueDecision(decision);

        //When
        final List<CaseworkerCICDocument> result = DecisionDocumentListUtil.getDecisionDocs(caseData);

        //Then
        final CaseworkerCICDocument expectedCaseworkerCICDocument = CaseworkerCICDocument.builder()
            .documentLink(document)
            .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
            .build();
        assertThat(result).hasSize(1).contains(expectedCaseworkerCICDocument);
    }

    @Test
    void shouldSetFinalDecisionDocumentsForRemovalDraftDocument() {
        //Given
        final CaseData caseData = caseData();

        final CicCase cicCase = CicCase.builder()
            .finalDecisionDocumentList(new ArrayList<>())
            .build();
        caseData.setCicCase(cicCase);


        final CaseData oldData = caseData();
        final Document document = Document.builder().url("url1").binaryUrl("url1").filename("name1").build();
        oldData.setCaseIssueFinalDecision(CaseIssueFinalDecision.builder().finalDecisionDraft(document).build());

        final CicCase cicCaseOld = CicCase.builder().build();
        oldData.setCicCase(cicCaseOld);

        //When
        DecisionDocumentListUtil.addFinalDecisionDocumentsForRemoval(caseData, oldData);

        //Then
        final CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
                .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                .documentLink(document).build();
        final ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue = new ListValue<>();
        caseworkerCICDocumentListValue.setId("1");
        caseworkerCICDocumentListValue.setValue(caseworkerCICDocument);
        assertThat(caseData.getCicCase().getRemovedDocumentList()).hasSize(1).contains(caseworkerCICDocumentListValue);
    }

    @Test
    void shouldSetFinalDecisionDocumentsForRemovalCICDocument() {
        //Given
        final CaseData caseData = caseData();

        final CicCase cicCase = CicCase.builder()
                .finalDecisionDocumentList(new ArrayList<>())
                .build();
        caseData.setCicCase(cicCase);


        final CaseData oldData = caseData();
        final Document document = Document.builder().url("url1").binaryUrl("url1").filename("name1").build();
        final CICDocument docOld = CICDocument.builder().documentLink(document).build();
        oldData.setCaseIssueFinalDecision(CaseIssueFinalDecision.builder().document(docOld).build());

        final CicCase cicCaseOld = CicCase.builder().build();
        oldData.setCicCase(cicCaseOld);

        //When
        DecisionDocumentListUtil.addFinalDecisionDocumentsForRemoval(caseData, oldData);

        //Then
        final CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
                .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                .documentLink(document).build();
        final ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue = new ListValue<>();
        caseworkerCICDocumentListValue.setId("1");
        caseworkerCICDocumentListValue.setValue(caseworkerCICDocument);
        assertThat(caseData.getCicCase().getRemovedDocumentList()).hasSize(1).contains(caseworkerCICDocumentListValue);
    }

    @Test
    void shouldRemoveFinalDecisionDraft() {
        //Given
        final Document document = Document.builder().url("url1").binaryUrl("url1").filename("name1").build();
        final CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
                .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                .documentLink(document).build();
        final ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue = new ListValue<>();
        caseworkerCICDocumentListValue.setId("1");
        caseworkerCICDocumentListValue.setValue(caseworkerCICDocument);

        final CaseData caseData = caseData();
        caseData.getCaseIssueFinalDecision().setFinalDecisionDraft(document);
        final CicCase cicCase = CicCase.builder()
                .removedDocumentList(List.of(caseworkerCICDocumentListValue))
                .build();
        caseData.setCicCase(cicCase);

        //When
        DecisionDocumentListUtil.removeFinalDecisionDraftAndCICDocument(caseData, caseworkerCICDocumentListValue);

        //Then
        assertThat(caseData.getCaseIssueFinalDecision().getFinalDecisionDraft()).isNull();
    }

    @Test
    void shouldRemoveFinalDecisionCICDocument() {
        //Given
        final Document document = Document.builder().url("url1").binaryUrl("url1").filename("name1").build();
        final CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
                .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                .documentLink(document).build();
        final ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue = new ListValue<>();
        caseworkerCICDocumentListValue.setId("1");
        caseworkerCICDocumentListValue.setValue(caseworkerCICDocument);

        final CICDocument cicDocument = CICDocument.builder().documentLink(document).build();
        final CaseData caseData = caseData();
        caseData.getCaseIssueFinalDecision().setDocument(cicDocument);
        final CicCase cicCase = CicCase.builder()
                .removedDocumentList(List.of(caseworkerCICDocumentListValue))
                .build();
        caseData.setCicCase(cicCase);

        //When
        DecisionDocumentListUtil.removeFinalDecisionDraftAndCICDocument(caseData, caseworkerCICDocumentListValue);

        //Then
        assertThat(caseData.getCaseIssueFinalDecision().getDocument()).isEqualTo(EMPTY_DOCUMENT);
    }

    @Test
    void shouldSetDecisionDocumentsForRemovalDraftDocument() {
        //Given
        final CaseData caseData = caseData();

        final CicCase cicCase = CicCase.builder()
                .decisionDocumentList(new ArrayList<>())
                .build();
        caseData.setCicCase(cicCase);


        final CaseData oldData = caseData();
        final Document document = Document.builder().url("url1").binaryUrl("url1").filename("name1").build();
        oldData.setCaseIssueDecision(CaseIssueDecision.builder().issueDecisionDraft(document).build());

        final CicCase cicCaseOld = CicCase.builder().build();
        oldData.setCicCase(cicCaseOld);

        //When
        DecisionDocumentListUtil.addDecisionDocumentsForRemoval(caseData, oldData);

        //Then
        final CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
                .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                .documentLink(document).build();
        final ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue = new ListValue<>();
        caseworkerCICDocumentListValue.setId("1");
        caseworkerCICDocumentListValue.setValue(caseworkerCICDocument);
        assertThat(caseData.getCicCase().getRemovedDocumentList()).hasSize(1).contains(caseworkerCICDocumentListValue);
    }

    @Test
    void shouldSetDecisionDocumentsForRemovalCICDocument() {
        //Given
        final CaseData caseData = caseData();

        final CicCase cicCase = CicCase.builder()
                .decisionDocumentList(new ArrayList<>())
                .build();
        caseData.setCicCase(cicCase);


        final CaseData oldData = caseData();
        final Document document = Document.builder().url("url1").binaryUrl("url1").filename("name1").build();
        final CICDocument docOld = CICDocument.builder().documentLink(document).build();
        oldData.setCaseIssueDecision(CaseIssueDecision.builder().decisionDocument(docOld).build());

        final CicCase cicCaseOld = CicCase.builder().build();
        oldData.setCicCase(cicCaseOld);

        //When
        DecisionDocumentListUtil.addDecisionDocumentsForRemoval(caseData, oldData);

        //Then
        final CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
                .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                .documentLink(document).build();
        final ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue = new ListValue<>();
        caseworkerCICDocumentListValue.setId("1");
        caseworkerCICDocumentListValue.setValue(caseworkerCICDocument);
        assertThat(caseData.getCicCase().getRemovedDocumentList()).hasSize(1).contains(caseworkerCICDocumentListValue);
    }

    @Test
    void shouldRemoveDecisionDraft() {
        //Given
        final Document document = Document.builder().url("url1").binaryUrl("url1").filename("name1").build();
        final CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
                .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                .documentLink(document).build();
        final ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue = new ListValue<>();
        caseworkerCICDocumentListValue.setId("1");
        caseworkerCICDocumentListValue.setValue(caseworkerCICDocument);

        final CaseData caseData = caseData();
        caseData.getCaseIssueDecision().setIssueDecisionDraft(document);
        final CicCase cicCase = CicCase.builder()
                .removedDocumentList(List.of(caseworkerCICDocumentListValue))
                .build();
        caseData.setCicCase(cicCase);

        //When
        DecisionDocumentListUtil.removeDecisionDraftAndCICDocument(caseData, caseworkerCICDocumentListValue);

        //Then
        assertThat(caseData.getCaseIssueDecision().getIssueDecisionDraft()).isNull();
    }

    @Test
    void shouldRemoveDecisionCICDocument() {
        //Given
        final Document document = Document.builder().url("url1").binaryUrl("url1").filename("name1").build();
        final CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
                .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                .documentLink(document).build();
        final ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue = new ListValue<>();
        caseworkerCICDocumentListValue.setId("1");
        caseworkerCICDocumentListValue.setValue(caseworkerCICDocument);

        final CICDocument cicDocument = CICDocument.builder().documentLink(document).build();
        final CaseData caseData = caseData();
        caseData.getCaseIssueDecision().setDecisionDocument(cicDocument);
        final CicCase cicCase = CicCase.builder()
                .removedDocumentList(List.of(caseworkerCICDocumentListValue))
                .build();
        caseData.setCicCase(cicCase);

        //When
        DecisionDocumentListUtil.removeDecisionDraftAndCICDocument(caseData, caseworkerCICDocumentListValue);

        //Then
        assertThat(caseData.getCaseIssueDecision().getDecisionDocument()).isEqualTo(EMPTY_DOCUMENT);
    }
}
