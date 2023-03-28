package uk.gov.hmcts.sptribs.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class DocumentListServiceTest {

    @InjectMocks
    private DocumentListService documentListService;

    @Test
    void shouldGenerateDocList() {
        //Given

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.CARE_DOCUMENTS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name").build())
            .build();
        ListValue<CaseworkerCICDocument> list = new ListValue<>();
        list.setValue(doc);
        listValueList.add(list);
        CicCase cicCase = CicCase.builder()
            .reinstateDocuments(listValueList)
            .build();
        final CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(cicCase);
        details.setData(caseData);
        //When
        DynamicList result = documentListService.prepareDocumentList(caseData);

        //Then
        assertThat(result).isNotNull();

    }

    @Test
    void shouldGenerateDocListApplicant() {
        //Given

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.CARE_DOCUMENTS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name").build())
            .build();
        ListValue<CaseworkerCICDocument> list = new ListValue<>();
        list.setValue(doc);
        listValueList.add(list);
        CicCase cicCase = CicCase.builder()
            .applicantDocumentsUploaded(listValueList)
            .build();
        final CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(cicCase);
        details.setData(caseData);
        //When
        DynamicList result = documentListService.prepareDocumentList(caseData);

        //Then
        assertThat(result).isNotNull();

    }

    @Test
    void shouldGenerateDocListClose() {
        //Given

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.CARE_DOCUMENTS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name").build())
            .build();
        ListValue<CaseworkerCICDocument> list = new ListValue<>();
        list.setValue(doc);
        listValueList.add(list);
        final CaseData caseData = CaseData.builder().build();
        caseData.setCloseCase(CloseCase.builder().documents(listValueList).build());
        details.setData(caseData);
        //When
        DynamicList result = documentListService.prepareDocumentList(caseData);

        //Then
        assertThat(result).isNotNull();

    }


    @Test
    void shouldGenerateDocListDocManagement() {
        //Given

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.CARE_DOCUMENTS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name").build())
            .build();
        ListValue<CaseworkerCICDocument> list = new ListValue<>();
        list.setValue(doc);
        listValueList.add(list);
        final CaseData caseData = CaseData.builder().build();
        caseData.setDocManagement(DocumentManagement.builder().caseworkerCICDocument(listValueList).build());
        details.setData(caseData);
        //When
        DynamicList result = documentListService.prepareDocumentList(caseData);

        //Then
        assertThat(result).isNotNull();

    }

    @Test
    void shouldGenerateDocListHearingSummary() {
        //Given

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.CARE_DOCUMENTS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name").build())
            .build();
        ListValue<CaseworkerCICDocument> list = new ListValue<>();
        list.setValue(doc);
        listValueList.add(list);
        final CaseData caseData = CaseData.builder().build();
        caseData.setListing(Listing.builder().summary(HearingSummary.builder().recFile(listValueList).build()).build());
        details.setData(caseData);
        //When
        DynamicList result = documentListService.prepareDocumentList(caseData);

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
        List<CaseworkerCICDocument> result = documentListService.getDecisionDocs(caseData);

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
        List<CaseworkerCICDocument> result = documentListService.getDecisionDocs(caseData);

        //Then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldGenerateDecisionDocAll() {
        //Given
        CaseIssueDecision decision = CaseIssueDecision.builder()
            .issueDecisionDraft(Document.builder().filename("name").binaryUrl("d").build())
            .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.setCaseIssueDecision(decision);

        //When
        List<ListValue<CaseworkerCICDocument>> result = documentListService.getAllDecisionDocuments(caseData);

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
        List<CaseworkerCICDocument> result = documentListService.getFinalDecisionDocs(caseData);

        //Then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldGenerateFinalDecisionDocList() {
        //Given
        CaseIssueFinalDecision decision = CaseIssueFinalDecision.builder()
            .document(CICDocument.builder().documentLink(Document.builder().filename("name").binaryUrl("d").build()).build())
            .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.setCaseIssueFinalDecision(decision);

        //When
        List<ListValue<CaseworkerCICDocument>> result = documentListService.getAllFinalDecisionDocuments(caseData);

        //Then
        assertThat(result).isNotNull();
    }


    @Test
    void shouldGenerateFinalDecisionDocTemp() {
        //Given
        CaseIssueFinalDecision decision = CaseIssueFinalDecision.builder()
            .finalDecisionDraft(Document.builder().filename("name").binaryUrl("d").build())
            .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.setCaseIssueFinalDecision(decision);

        //When
        List<CaseworkerCICDocument> result = documentListService.getFinalDecisionDocs(caseData);

        //Then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldGenerateOrderDocTemp() {
        //Given

        Order order = Order.builder().draftOrder(DraftOrderCIC.builder()
            .templateGeneratedDocument(Document.builder().filename("name").build()).build()).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);
        CicCase cicCase = CicCase.builder().orderList(List.of(orderListValue)).build();

        //When
        List<CaseworkerCICDocument> result = documentListService.getOrderDocuments(cicCase);

        //Then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldGenerateOrderAll() {
        //Given
        Order order = Order.builder().draftOrder(DraftOrderCIC.builder()
            .templateGeneratedDocument(Document.builder().build()).build()).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);
        CicCase cicCase = CicCase.builder().orderList(List.of(orderListValue)).build();

        //When
        List<ListValue<CaseworkerCICDocument>> result = documentListService.getAllOrderDocuments(cicCase);

        //Then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldGenerateOrderDocUpload() {
        //Given

        CICDocument document = CICDocument.builder().documentLink(Document.builder().filename("name").build()).build();
        ListValue<CICDocument> cicDocumentListValue = new ListValue<>();
        cicDocumentListValue.setValue(document);
        Order order = Order.builder().uploadedFile(List.of(cicDocumentListValue)).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);
        CicCase cicCase = CicCase.builder().orderList(List.of(orderListValue)).build();

        //When
        List<CaseworkerCICDocument> result = documentListService.getOrderDocuments(cicCase);

        //Then
        assertThat(result).isNotNull();
    }

}
