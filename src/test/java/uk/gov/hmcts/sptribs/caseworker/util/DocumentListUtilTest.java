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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

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
        assertThat(result.getListItems().getFirst().getLabel()).isEqualTo("L - Linked docs--name.pdf");
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

    @Test
    void shouldGetAllCaseDocumentsExcludingInitialCicaUploadWithFurtherUploadedDocuments() {
        //Given
        final CaseData caseData = CaseData.builder().build();

        // Set up further uploaded documents (these should be included)
        List<ListValue<CaseworkerCICDocument>> furtherDocs = new ArrayList<>();
        CaseworkerCICDocument furtherDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("further-url").binaryUrl("further-url").filename("further-doc.pdf").build())
            .documentEmailContent("further email content")
            .build();
        ListValue<CaseworkerCICDocument> furtherDocListValue = new ListValue<>();
        furtherDocListValue.setValue(furtherDoc);
        furtherDocs.add(furtherDocListValue);
        caseData.setFurtherUploadedDocuments(furtherDocs);

        // Set up initial CICA documents (these should be excluded)
        List<ListValue<CaseworkerCICDocument>> initialDocs = new ArrayList<>();
        CaseworkerCICDocument initialDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.APPLICATION_FORM)
            .documentLink(Document.builder().url("initial-url").binaryUrl("initial-url").filename("initial-doc.pdf").build())
            .documentEmailContent("initial email content")
            .build();
        ListValue<CaseworkerCICDocument> initialDocListValue = new ListValue<>();
        initialDocListValue.setValue(initialDoc);
        initialDocs.add(initialDocListValue);
        caseData.setInitialCicaDocuments(initialDocs);

        // Set up other documents that should be included
        List<ListValue<CaseworkerCICDocument>> applicantDocs = new ArrayList<>();
        CaseworkerCICDocument applicantDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.APPLICATION_FORM)
            .documentLink(Document.builder().url("applicant-url").binaryUrl("applicant-url").filename("applicant-doc.pdf").build())
            .documentEmailContent("applicant email content")
            .build();
        ListValue<CaseworkerCICDocument> applicantDocListValue = new ListValue<>();
        applicantDocListValue.setValue(applicantDoc);
        applicantDocs.add(applicantDocListValue);

        CicCase cicCase = CicCase.builder()
            .applicantDocumentsUploaded(applicantDocs)
            .build();
        caseData.setCicCase(cicCase);

        //When
        List<ListValue<CaseworkerCICDocument>> result = DocumentListUtil.getAllCaseDocumentsExcludingInitialCicaUpload(caseData);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2); // Should include further docs and applicant docs, but exclude initial docs

        // Verify further uploaded document is included
        assertThat(result.stream().anyMatch(doc ->
            doc.getValue().getDocumentLink().getFilename().equals("further-doc.pdf"))).isTrue();

        // Verify applicant document is included
        assertThat(result.stream().anyMatch(doc ->
            doc.getValue().getDocumentLink().getFilename().equals("applicant-doc.pdf"))).isTrue();

        // Verify initial CICA document is excluded
        assertThat(result.stream().noneMatch(doc ->
            doc.getValue().getDocumentLink().getFilename().equals("initial-doc.pdf"))).isTrue();
    }

    @Test
    void shouldGetAllCaseDocumentsExcludingInitialCicaUploadWithNullFurtherUploadedDocuments() {
        //Given
        final CaseData caseData = CaseData.builder().build();

        // Set further uploaded documents to null
        caseData.setFurtherUploadedDocuments(null);

        // Set up initial CICA documents (these should be excluded)
        List<ListValue<CaseworkerCICDocument>> initialDocs = new ArrayList<>();
        CaseworkerCICDocument initialDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.APPLICATION_FORM)
            .documentLink(Document.builder().url("initial-url").binaryUrl("initial-url").filename("initial-doc.pdf").build())
            .documentEmailContent("initial email content")
            .build();
        ListValue<CaseworkerCICDocument> initialDocListValue = new ListValue<>();
        initialDocListValue.setValue(initialDoc);
        initialDocs.add(initialDocListValue);
        caseData.setInitialCicaDocuments(initialDocs);

        // Set up other documents that should be included
        List<ListValue<CaseworkerCICDocument>> applicantDocs = new ArrayList<>();
        CaseworkerCICDocument applicantDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.APPLICATION_FORM)
            .documentLink(Document.builder().url("applicant-url").binaryUrl("applicant-url").filename("applicant-doc.pdf").build())
            .documentEmailContent("applicant email content")
            .build();
        ListValue<CaseworkerCICDocument> applicantDocListValue = new ListValue<>();
        applicantDocListValue.setValue(applicantDoc);
        applicantDocs.add(applicantDocListValue);

        CicCase cicCase = CicCase.builder()
            .applicantDocumentsUploaded(applicantDocs)
            .build();
        caseData.setCicCase(cicCase);

        //When
        List<ListValue<CaseworkerCICDocument>> result = DocumentListUtil.getAllCaseDocumentsExcludingInitialCicaUpload(caseData);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1); // Should include only applicant docs, exclude initial docs

        // Verify applicant document is included
        assertThat(result.stream().anyMatch(doc ->
            doc.getValue().getDocumentLink().getFilename().equals("applicant-doc.pdf"))).isTrue();

        // Verify initial CICA document is excluded
        assertThat(result.stream().noneMatch(doc ->
            doc.getValue().getDocumentLink().getFilename().equals("initial-doc.pdf"))).isTrue();
    }

    @Test
    void shouldGetAllCaseDocumentsExcludingInitialCicaUploadWithEmptyFurtherUploadedDocuments() {
        //Given
        final CaseData caseData = CaseData.builder().build();

        // Set further uploaded documents to empty list
        caseData.setFurtherUploadedDocuments(new ArrayList<>());

        // Set up initial CICA documents (these should be excluded)
        List<ListValue<CaseworkerCICDocument>> initialDocs = new ArrayList<>();
        CaseworkerCICDocument initialDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.APPLICATION_FORM)
            .documentLink(Document.builder().url("initial-url").binaryUrl("initial-url").filename("initial-doc.pdf").build())
            .documentEmailContent("initial email content")
            .build();
        ListValue<CaseworkerCICDocument> initialDocListValue = new ListValue<>();
        initialDocListValue.setValue(initialDoc);
        initialDocs.add(initialDocListValue);
        caseData.setInitialCicaDocuments(initialDocs);

        // Set up other documents that should be included
        List<ListValue<CaseworkerCICDocument>> applicantDocs = new ArrayList<>();
        CaseworkerCICDocument applicantDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.APPLICATION_FORM)
            .documentLink(Document.builder().url("applicant-url").binaryUrl("applicant-url").filename("applicant-doc.pdf").build())
            .documentEmailContent("applicant email content")
            .build();
        ListValue<CaseworkerCICDocument> applicantDocListValue = new ListValue<>();
        applicantDocListValue.setValue(applicantDoc);
        applicantDocs.add(applicantDocListValue);

        CicCase cicCase = CicCase.builder()
            .applicantDocumentsUploaded(applicantDocs)
            .build();
        caseData.setCicCase(cicCase);

        //When
        List<ListValue<CaseworkerCICDocument>> result = DocumentListUtil.getAllCaseDocumentsExcludingInitialCicaUpload(caseData);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1); // Should include only applicant docs, exclude initial docs

        // Verify applicant document is included
        assertThat(result.stream().anyMatch(doc ->
            doc.getValue().getDocumentLink().getFilename().equals("applicant-doc.pdf"))).isTrue();

        // Verify initial CICA document is excluded
        assertThat(result.stream().noneMatch(doc ->
            doc.getValue().getDocumentLink().getFilename().equals("initial-doc.pdf"))).isTrue();
    }

    @Test
    void shouldGetAllCaseDocumentsExcludingInitialCicaUploadWithMultipleFurtherUploadedDocuments() {
        //Given
        final CaseData caseData = CaseData.builder().build();

        // Set up multiple further uploaded documents
        List<ListValue<CaseworkerCICDocument>> furtherDocs = new ArrayList<>();

        CaseworkerCICDocument furtherDoc1 = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("further-url-1").binaryUrl("further-url-1").filename("further-doc-1.pdf").build())
            .documentEmailContent("further email content 1")
            .build();
        ListValue<CaseworkerCICDocument> furtherDoc1ListValue = new ListValue<>();
        furtherDoc1ListValue.setValue(furtherDoc1);
        furtherDocs.add(furtherDoc1ListValue);

        CaseworkerCICDocument furtherDoc2 = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.APPLICATION_FORM)
            .documentLink(Document.builder().url("further-url-2").binaryUrl("further-url-2").filename("further-doc-2.pdf").build())
            .documentEmailContent("further email content 2")
            .build();
        ListValue<CaseworkerCICDocument> furtherDoc2ListValue = new ListValue<>();
        furtherDoc2ListValue.setValue(furtherDoc2);
        furtherDocs.add(furtherDoc2ListValue);

        caseData.setFurtherUploadedDocuments(furtherDocs);

        // Set up initial CICA documents (these should be excluded)
        List<ListValue<CaseworkerCICDocument>> initialDocs = new ArrayList<>();
        CaseworkerCICDocument initialDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.APPLICATION_FORM)
            .documentLink(Document.builder().url("initial-url").binaryUrl("initial-url").filename("initial-doc.pdf").build())
            .documentEmailContent("initial email content")
            .build();
        ListValue<CaseworkerCICDocument> initialDocListValue = new ListValue<>();
        initialDocListValue.setValue(initialDoc);
        initialDocs.add(initialDocListValue);
        caseData.setInitialCicaDocuments(initialDocs);

        // Set up other documents that should be included
        List<ListValue<CaseworkerCICDocument>> applicantDocs = new ArrayList<>();
        CaseworkerCICDocument applicantDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.APPLICATION_FORM)
            .documentLink(Document.builder().url("applicant-url").binaryUrl("applicant-url").filename("applicant-doc.pdf").build())
            .documentEmailContent("applicant email content")
            .build();
        ListValue<CaseworkerCICDocument> applicantDocListValue = new ListValue<>();
        applicantDocListValue.setValue(applicantDoc);
        applicantDocs.add(applicantDocListValue);

        CicCase cicCase = CicCase.builder()
            .applicantDocumentsUploaded(applicantDocs)
            .build();
        caseData.setCicCase(cicCase);

        //When
        List<ListValue<CaseworkerCICDocument>> result = DocumentListUtil.getAllCaseDocumentsExcludingInitialCicaUpload(caseData);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3); // Should include 2 further docs and 1 applicant doc, but exclude initial docs

        // Verify both further uploaded documents are included
        assertThat(result.stream().anyMatch(doc ->
            doc.getValue().getDocumentLink().getFilename().equals("further-doc-1.pdf"))).isTrue();
        assertThat(result.stream().anyMatch(doc ->
            doc.getValue().getDocumentLink().getFilename().equals("further-doc-2.pdf"))).isTrue();

        // Verify applicant document is included
        assertThat(result.stream().anyMatch(doc ->
            doc.getValue().getDocumentLink().getFilename().equals("applicant-doc.pdf"))).isTrue();

        // Verify initial CICA document is excluded
        assertThat(result.stream().noneMatch(doc ->
            doc.getValue().getDocumentLink().getFilename().equals("initial-doc.pdf"))).isTrue();
    }

    @Test
    void shouldGetAllCaseDocumentsExcludingInitialCicaUploadWithNoDocuments() {
        //Given
        final CaseData caseData = CaseData.builder().build();

        // Set all document lists to null/empty
        caseData.setFurtherUploadedDocuments(null);
        caseData.setInitialCicaDocuments(null);

        CicCase cicCase = CicCase.builder().build();
        caseData.setCicCase(cicCase);

        //When
        List<ListValue<CaseworkerCICDocument>> result = DocumentListUtil.getAllCaseDocumentsExcludingInitialCicaUpload(caseData);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty(); // Should return empty list when no documents exist
    }

    @Test
    void shouldExtractDocumentIds() {
        final Document document = Document.builder()
            .filename("test file")
            .url("test.url/documentId")
            .binaryUrl("test.url/documentId/binary")
            .build();
        final CaseworkerCICDocument cicDocument = CaseworkerCICDocument.builder()
            .date(LocalDate.of(2025, 12, 11))
            .documentCategory(DocumentType.APPLICATION_FOR_AN_EXTENSION_OF_TIME)
            .documentEmailContent("description")
            .documentLink(document)
            .build();
        final List<ListValue<CaseworkerCICDocument>> applicantDocuments =
            List.of(ListValue.<CaseworkerCICDocument>builder().value(cicDocument).build());

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(CaseData.builder()
                .cicCase(CicCase.builder()
                    .fullName("Test Name")
                    .caseNumber(TEST_CASE_ID.toString())
                    .build())
                .build())
            .build();
        final CaseData data = caseDetails.getData();
        final CicCase cicCase = data.getCicCase();
        cicCase.setApplicantDocumentsUploaded(applicantDocuments);

        DynamicMultiSelectList dynamicMultiSelectList = DocumentListUtil.prepareDocumentList(data, "test.url");
        dynamicMultiSelectList.setValue(dynamicMultiSelectList.getListItems());

        List<String> documentIds = DocumentListUtil.extractDocumentIds(dynamicMultiSelectList.getListItems());
        assertThat(documentIds).contains("documentId").hasSize(1);
    }

    @Test
    void shouldReturnEmptyListIfNoIdFound() {
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder().build();
        List<String> documentIds = DocumentListUtil.extractDocumentIds(dynamicMultiSelectList.getListItems());
        assertThat(documentIds).isEmpty();
    }
}
