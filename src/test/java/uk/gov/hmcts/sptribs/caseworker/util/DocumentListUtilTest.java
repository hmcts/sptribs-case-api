package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
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
public class DocumentListUtilTest {

    @Test
    void shouldGenerateDocList() {
        //Given

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
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
        DynamicMultiSelectList result = DocumentListUtil.prepareDocumentList(caseData, "");

        //Then
        assertThat(result).isNotNull();

    }

    @Test
    void shouldGenerateEmptyContactPartiesDocList() {
        //Given

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument mp3Doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name.mp3").build())
            .build();
        CaseworkerCICDocument mp4Doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name.mp4").build())
            .build();
        ListValue<CaseworkerCICDocument> mp3listValue = new ListValue<>();
        mp3listValue.setValue(mp3Doc);
        listValueList.add(mp3listValue);
        ListValue<CaseworkerCICDocument> mp4listValue = new ListValue<>();
        mp4listValue.setValue(mp4Doc);
        listValueList.add(mp4listValue);
        CicCase cicCase = CicCase.builder()
            .reinstateDocuments(listValueList)
            .build();
        final CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(cicCase);
        details.setData(caseData);
        //When
        DynamicMultiSelectList result = DocumentListUtil.prepareContactPartiesDocumentList(caseData, "");

        //Then
        Assertions.assertTrue(result.getListItems().isEmpty());

    }

    @Test
    void shouldGenerateNonEmptyContactPartiesDocList() {
        //Given

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument pdfDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name.pdf").build())
            .build();
        CaseworkerCICDocument docxDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name.docx").build())
            .build();
        ListValue<CaseworkerCICDocument> pdflistValue = new ListValue<>();
        pdflistValue.setValue(pdfDoc);
        listValueList.add(pdflistValue);
        ListValue<CaseworkerCICDocument> docxlistValue = new ListValue<>();
        docxlistValue.setValue(docxDoc);
        listValueList.add(docxlistValue);
        CicCase cicCase = CicCase.builder()
            .reinstateDocuments(listValueList)
            .build();
        final CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(cicCase);
        details.setData(caseData);
        //When
        DynamicMultiSelectList result = DocumentListUtil.prepareContactPartiesDocumentList(caseData, "");

        //Then
        Assertions.assertEquals(2, result.getListItems().size());

    }

    @Test
    void shouldGenerateSelectedAmendDocList() {
        //Given

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().binaryUrl("url").filename("name").build())
            .build();
        ListValue<CaseworkerCICDocument> list = new ListValue<>();
        list.setValue(doc);
        listValueList.add(list);
        CicCase cicCase = CicCase.builder()
            .reinstateDocuments(listValueList)
            .build();
        DocumentManagement documentManagement = DocumentManagement.builder()
            .build();
        final CaseData caseData = CaseData.builder()
            .newDocManagement(documentManagement)
            .build();
        caseData.setCicCase(cicCase);
        details.setData(caseData);
        //When

        //Then
        //assertThat(result).isNotNull();

    }

    @Test
    void shouldGenerateDocListApplicant() {
        //Given

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
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
        DynamicMultiSelectList result = DocumentListUtil.prepareDocumentList(caseData, "");

        //Then
        assertThat(result).isNotNull();

    }

    @Test
    void shouldGenerateDocListClose() {
        //Given

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name").build())
            .build();
        ListValue<CaseworkerCICDocument> list = new ListValue<>();
        list.setValue(doc);
        listValueList.add(list);
        final CaseData caseData = CaseData.builder().build();
        caseData.setCloseCase(CloseCase.builder().documents(listValueList).build());
        details.setData(caseData);
        //When
        DynamicMultiSelectList result = DocumentListUtil.prepareDocumentList(caseData, "");

        //Then
        assertThat(result).isNotNull();

    }


    @Test
    void shouldGenerateDocListDocManagement() {
        //Given

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name").build())
            .build();
        ListValue<CaseworkerCICDocument> list = new ListValue<>();
        list.setValue(doc);
        listValueList.add(list);
        final CaseData caseData = CaseData.builder().build();
        caseData.setAllDocManagement(DocumentManagement.builder().caseworkerCICDocument(listValueList).build());
        details.setData(caseData);
        //When
        DynamicMultiSelectList result = DocumentListUtil.prepareDocumentList(caseData, "");
        //Then
        assertThat(result).isNotNull();

    }

    @Test
    void shouldGenerateDocListHearingSummary() {
        //Given
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name.pdf").build())
            .build();
        ListValue<CaseworkerCICDocument> list = new ListValue<>();
        list.setValue(doc);
        listValueList.add(list);
        final CaseData caseData = CaseData.builder().build();
        final Listing listing = Listing.builder().build();
        final HearingSummary summary = HearingSummary.builder().recFile(listValueList).build();
        listing.setSummary(summary);

        ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(listing);
        caseData.setHearingList(List.of(listingListValue));
        details.setData(caseData);

        //When
        DynamicMultiSelectList result = DocumentListUtil.prepareDocumentList(caseData);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getListItems().size()).isEqualTo(1);
        assertThat(result.getListItems().get(0).getLabel()).isEqualTo("L - Linked docs--name.pdf");
    }

    @Test
    void shouldNotPopulateDocumentListIfRecFileListIsEmpty() {
        //Given
        final CaseDetails<CaseData, State> details = new CaseDetails<>();

        final CaseData caseData = CaseData.builder().build();
        final Listing listing = Listing.builder().build();
        final HearingSummary summary = HearingSummary.builder().build();
        listing.setSummary(summary);

        ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(listing);
        caseData.setHearingList(List.of(listingListValue));
        details.setData(caseData);

        //When
        DynamicMultiSelectList result = DocumentListUtil.prepareDocumentList(caseData);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getListItems().size()).isEqualTo(0);
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
        List<ListValue<CaseworkerCICDocument>> result = DocumentListUtil.getAllDecisionDocuments(caseData);

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
        List<ListValue<CaseworkerCICDocument>> result = DocumentListUtil.getAllFinalDecisionDocuments(caseData);

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
        List<ListValue<CaseworkerCICDocument>> result = DocumentListUtil.getAllOrderDocuments(cicCase);

        //Then
        assertThat(result).isNotNull();
    }

}
