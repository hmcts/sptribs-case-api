package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.get2Document;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getDocument;

@ExtendWith(MockitoExtension.class)
public class DocumentRemoveListUtilTest {

    @Test
    void shouldSuccessfullyRemoveDocs() {
        //Given
        final CaseData caseData = caseData();
        CICDocument doc = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        caseData.setCaseIssueFinalDecision(CaseIssueFinalDecision.builder().document(doc).build());
        caseData.setCaseIssueDecision(CaseIssueDecision.builder().decisionDocument(doc).build());

        CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        ListValue<CICDocument> cicDocumentListValue = new ListValue<>();
        cicDocumentListValue.setValue(document);
        Order order = Order.builder().uploadedFile(List.of(cicDocumentListValue)).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);


        CicCase cicCase = CicCase.builder()
            .orderDocumentList(getDocument())
            .orderList(List.of(orderListValue))
            .decisionDocumentList(new ArrayList<>())
            .finalDecisionDocumentList(new ArrayList<>())
            .applicantDocumentsUploaded(getDocument())
            .reinstateDocuments(getDocument())
            .build();
        caseData.setCicCase(cicCase);
        DocumentManagement documentManagement = DocumentManagement.builder().caseworkerCICDocument(getDocument()).build();
        caseData.setAllDocManagement(documentManagement);
        caseData.setListing(Listing.builder().summary(HearingSummary.builder().recFile(getDocument()).build()).build());
        CloseCase closeCase = CloseCase.builder()
            .documents(getDocument())
            .build();
        caseData.setCloseCase(closeCase);
        final CaseData oldData = caseData();
        CloseCase closeCaseOld = CloseCase.builder()
            .documents(get2Document())
            .build();
        oldData.setCloseCase(closeCaseOld);
        CICDocument docOld = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        oldData.setCaseIssueFinalDecision(CaseIssueFinalDecision.builder().document(docOld).build());
        oldData.setCaseIssueDecision(CaseIssueDecision.builder().decisionDocument(docOld).build());

        DocumentManagement documentManagementOld = DocumentManagement.builder().caseworkerCICDocument(get2Document()).build();
        oldData.setAllDocManagement(documentManagementOld);
        CicCase cicCaseOld = CicCase.builder()
            .decisionDocumentList(get2Document())
            .finalDecisionDocumentList(get2Document())
            .applicantDocumentsUploaded(get2Document())
            .reinstateDocuments(get2Document())
            .build();
        oldData.setCicCase(cicCaseOld);
        oldData.setListing(Listing.builder().summary(HearingSummary.builder().recFile(get2Document()).build()).build());
        oldData.setCloseCase(CloseCase.builder().documents(get2Document()).build());

        //When
        CaseData result = DocumentRemoveListUtil.setDocumentsListForRemoval(caseData, oldData);

        //Then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldSuccessfullyRemoveDocsSizeDec() {
        //Given
        final CaseData caseData = caseData();
        CICDocument doc = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        caseData.setCaseIssueFinalDecision(CaseIssueFinalDecision.builder().document(doc).build());
        caseData.setCaseIssueDecision(CaseIssueDecision.builder().decisionDocument(doc).build());

        CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        ListValue<CICDocument> cicDocumentListValue = new ListValue<>();
        cicDocumentListValue.setValue(document);
        Order order = Order.builder().uploadedFile(List.of(cicDocumentListValue)).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);

        CicCase cicCase = CicCase.builder()
            .orderDocumentList(getDocument())
            .orderList(List.of(orderListValue))
            .decisionDocumentList(new ArrayList<>())
            .finalDecisionDocumentList(new ArrayList<>())
            .applicantDocumentsUploaded(new ArrayList<>())
            .reinstateDocuments(new ArrayList<>())
            .build();
        caseData.setCicCase(cicCase);
        caseData.setListing(Listing.builder().summary(HearingSummary.builder().recFile(getDocument()).build()).build());
        DocumentManagement documentManagement = DocumentManagement.builder().caseworkerCICDocument(getDocument()).build();
        caseData.setAllDocManagement(documentManagement);
        CloseCase closeCase = CloseCase.builder()
            .documents(new ArrayList<>())
            .build();
        caseData.setCloseCase(closeCase);
        final CaseData oldData = caseData();
        CloseCase closeCaseOld = CloseCase.builder()
            .documents(get2Document())
            .build();
        oldData.setCloseCase(closeCaseOld);
        CICDocument docOld = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        oldData.setCaseIssueFinalDecision(CaseIssueFinalDecision.builder().document(docOld).build());
        oldData.setCaseIssueDecision(CaseIssueDecision.builder().decisionDocument(docOld).build());

        DocumentManagement documentManagementOld = DocumentManagement.builder().caseworkerCICDocument(get2Document()).build();
        oldData.setAllDocManagement(documentManagementOld);
        CicCase cicCaseOld = CicCase.builder()
            .decisionDocumentList(get2Document())
            .finalDecisionDocumentList(get2Document())
            .applicantDocumentsUploaded(get2Document())
            .reinstateDocuments(get2Document())
            .build();
        oldData.setCicCase(cicCaseOld);
        oldData.setListing(Listing.builder().summary(HearingSummary.builder().recFile(get2Document()).build()).build());
        oldData.setCloseCase(CloseCase.builder().documents(get2Document()).build());

        //When
        CaseData result = DocumentRemoveListUtil.setDocumentsListForRemoval(caseData, oldData);

        //Then
        assertThat(result).isNotNull();
    }
}
