package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
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
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        String documentId = UUID.randomUUID().toString();
        final Document document = Document.builder()
            .filename("test file")
            .url("test.url/" + documentId)
            .binaryUrl("test.url/" + documentId + "/binary")
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
        assertThat(documentIds).contains(documentId).hasSize(1);
    }

    @Test
    void shouldExtractDocumentIdsFromPreviewUrl() {
        String documentId = UUID.randomUUID().toString();
        String label = "[file.docx L - Linked docs](http://exui.net/media-viewer?document_url="
            + "http%3A%2F%2Fdoc-store%2Fdocuments%2F" + documentId + ")";

        DynamicListElement element = DynamicListElement.builder()
            .label(label)
            .code(UUID.randomUUID())
            .build();

        List<String> documentIds = DocumentListUtil.extractDocumentIds(List.of(element));
        assertThat(documentIds).contains(documentId).hasSize(1);
    }

    @Test
    void shouldReturnEmptyListIfNoIdFound() {
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder().build();
        List<String> documentIds = DocumentListUtil.extractDocumentIds(dynamicMultiSelectList.getListItems());
        assertThat(documentIds).isEmpty();
    }

    @Test
    void shouldReturnEmptyDocumentIdsForNullElements() {
        List<String> documentIds = DocumentListUtil.extractDocumentIds(null);

        assertThat(documentIds).isEmpty();
    }

    @Test
    void shouldExtractDocumentIdFromLabelWhenUrlDoesNotContainUuid() {
        String documentId = UUID.randomUUID().toString();
        DynamicListElement element = DynamicListElement.builder()
            .label("[file.pdf](http://example/no-id) linked-id " + documentId)
            .code(UUID.randomUUID())
            .build();

        assertThat(DocumentListUtil.extractDocumentId(element)).contains(documentId);
    }

    @Test
    void shouldReturnEmptyDocumentIdWhenElementOrLabelIsInvalid() {
        DynamicListElement noLabel = DynamicListElement.builder().code(UUID.randomUUID()).build();
        DynamicListElement invalidLabel = DynamicListElement.builder()
            .label("file.pdf without markdown link")
            .code(UUID.randomUUID())
            .build();

        assertThat(DocumentListUtil.extractDocumentId(null)).isEmpty();
        assertThat(DocumentListUtil.extractDocumentId(noLabel)).isEmpty();
        assertThat(DocumentListUtil.extractDocumentId(invalidLabel)).isEmpty();
    }

    @Test
    void shouldBuildMediaViewerUrl() {
        String documentId = UUID.randomUUID().toString();

        String mediaViewerUrl = DocumentListUtil.buildMediaViewerUrl(
            "https://xui.example.net/",
            "https://doc-store.example.net",
            documentId
        );

        assertThat(mediaViewerUrl)
            .isEqualTo("https://xui.example.net/media-viewer?document_url="
                + "https%3A%2F%2Fdoc-store.example.net%2Fdocuments%2F" + documentId);
    }

    @Test
    void shouldPrepareContactPartiesDocumentListForPreviewWithAllowedDocumentsOnly() {
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();

        CaseworkerCICDocument pdfDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder()
                .url("https://doc-store/documents/11111111-1111-1111-1111-111111111111")
                .binaryUrl("https://doc-store/documents/11111111-1111-1111-1111-111111111111/binary")
                .filename("allowed.pdf")
                .build())
            .build();

        CaseworkerCICDocument mp4Doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder()
                .url("https://doc-store/documents/22222222-2222-2222-2222-222222222222")
                .binaryUrl("https://doc-store/documents/22222222-2222-2222-2222-222222222222/binary")
                .filename("blocked.mp4")
                .build())
            .build();

        ListValue<CaseworkerCICDocument> pdfListValue = new ListValue<>();
        pdfListValue.setValue(pdfDoc);
        listValueList.add(pdfListValue);
        ListValue<CaseworkerCICDocument> mp4ListValue = new ListValue<>();
        mp4ListValue.setValue(mp4Doc);
        listValueList.add(mp4ListValue);

        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder().reinstateDocuments(listValueList).build())
            .build();

        DynamicMultiSelectList result = DocumentListUtil.prepareContactPartiesDocumentListForPreview(
            caseData,
            "https://xui.example.net",
            "https://doc-store.example.net"
        );

        assertThat(result.getListItems()).hasSize(1);
        assertThat(result.getListItems().getFirst().getLabel()).contains("allowed.pdf");
        assertThat(result.getListItems().getFirst().getLabel()).contains("media-viewer?document_url=");
    }

    @Test
    void shouldAddToExistingDocumentList() {
        CaseworkerCICDocument firstDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url-1").binaryUrl("url-1").filename("first.pdf").build())
            .build();
        CaseworkerCICDocument secondDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url-2").binaryUrl("url-2").filename("second.pdf").build())
            .build();

        List<ListValue<CaseworkerCICDocument>> existing = new ArrayList<>();
        ListValue<CaseworkerCICDocument> existingListValue = new ListValue<>();
        existingListValue.setValue(firstDoc);
        existing.add(existingListValue);

        List<ListValue<CaseworkerCICDocument>> toAdd = new ArrayList<>();
        ListValue<CaseworkerCICDocument> toAddListValue = new ListValue<>();
        toAddListValue.setValue(secondDoc);
        toAdd.add(toAddListValue);

        List<ListValue<CaseworkerCICDocument>> merged = DocumentListUtil.addToExistingDocumentList(existing, toAdd);
        List<ListValue<CaseworkerCICDocument>> fromEmpty = DocumentListUtil.addToExistingDocumentList(new ArrayList<>(), toAdd);

        assertThat(merged).hasSize(2);
        assertThat(fromEmpty).isEqualTo(toAdd);
    }

    @Test
    void shouldGetCaseDocumentById() {
        String matchingId = UUID.randomUUID().toString();
        CaseworkerCICDocument matchingDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder()
                .url("https://doc-store/documents/" + matchingId)
                .binaryUrl("https://doc-store/documents/" + matchingId + "/binary")
                .filename("match.pdf")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .reinstateDocuments(List.of(ListValue.<CaseworkerCICDocument>builder().value(matchingDoc).build()))
                .build())
            .build();

        assertThat(DocumentListUtil.getCaseDocumentById(matchingId, caseData)).contains(matchingDoc);
        assertThat(DocumentListUtil.getCaseDocumentById(UUID.randomUUID().toString(), caseData)).isEmpty();
    }

    @Test
    void shouldGetContactPartiesCaseDocuments() {
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument pdfDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name.pdf").build())
            .build();
        CaseworkerCICDocument mp4Doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name.mp4").build())
            .build();

        ListValue<CaseworkerCICDocument> pdfListValue = new ListValue<>();
        pdfListValue.setValue(pdfDoc);
        listValueList.add(pdfListValue);
        ListValue<CaseworkerCICDocument> mp4ListValue = new ListValue<>();
        mp4ListValue.setValue(mp4Doc);
        listValueList.add(mp4ListValue);

        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder().reinstateDocuments(listValueList).build())
            .build();

        List<ListValue<CaseworkerCICDocument>> result = DocumentListUtil.getContactPartiesCaseDocuments(caseData);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getValue().getDocumentLink().getFilename()).isEqualTo("name.pdf");
    }

    @Test
    void shouldPrepareCicDocumentListWithAllDocuments() {
        List<ListValue<CaseworkerCICDocument>> applicantDocs = new ArrayList<>();
        CaseworkerCICDocument applicantDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url-a").binaryUrl("url-a").filename("a.pdf").build())
            .build();
        ListValue<CaseworkerCICDocument> applicantListValue = new ListValue<>();
        applicantListValue.setValue(applicantDoc);
        applicantDocs.add(applicantListValue);

        List<ListValue<CaseworkerCICDocument>> reinstateDocs = new ArrayList<>();
        CaseworkerCICDocument reinstateDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url-r").binaryUrl("url-r").filename("r.pdf").build())
            .build();
        ListValue<CaseworkerCICDocument> reinstateListValue = new ListValue<>();
        reinstateListValue.setValue(reinstateDoc);
        reinstateDocs.add(reinstateListValue);

        List<ListValue<CaseworkerCICDocument>> docMgmtDocs = new ArrayList<>();
        CaseworkerCICDocument docMgmtDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url-d").binaryUrl("url-d").filename("d.pdf").build())
            .build();
        ListValue<CaseworkerCICDocument> docMgmtListValue = new ListValue<>();
        docMgmtListValue.setValue(docMgmtDoc);
        docMgmtDocs.add(docMgmtListValue);

        List<ListValue<CaseworkerCICDocument>> closeCaseDocs = new ArrayList<>();
        CaseworkerCICDocument closeDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url-c").binaryUrl("url-c").filename("c.pdf").build())
            .build();
        ListValue<CaseworkerCICDocument> closeListValue = new ListValue<>();
        closeListValue.setValue(closeDoc);
        closeCaseDocs.add(closeListValue);

        List<ListValue<CaseworkerCICDocument>> summaryDocs = new ArrayList<>();
        CaseworkerCICDocument summaryDoc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url-h").binaryUrl("url-h").filename("h.pdf").build())
            .build();
        ListValue<CaseworkerCICDocument> summaryListValue = new ListValue<>();
        summaryListValue.setValue(summaryDoc);
        summaryDocs.add(summaryListValue);

        Listing listing = Listing.builder().summary(HearingSummary.builder().recFile(summaryDocs).build()).build();
        ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(listing);

        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .applicantDocumentsUploaded(applicantDocs)
                .reinstateDocuments(reinstateDocs)
                .build())
            .allDocManagement(DocumentManagement.builder().caseworkerCICDocument(docMgmtDocs).build())
            .closeCase(CloseCase.builder().documents(closeCaseDocs).build())
            .hearingList(List.of(listingListValue))
            .build();

        var result = DocumentListUtil.prepareCICDocumentListWithAllDocuments(caseData);

        assertThat(result.getListItems()).hasSize(5);
    }

    @Test
    void shouldExtractDocumentsFromListValues() {
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name.pdf").build())
            .build();

        ListValue<CaseworkerCICDocument> listValue = new ListValue<>();
        listValue.setValue(doc);

        assertThat(DocumentListUtil.extractDocumentsFromListValues(List.of(listValue))).containsExactly(doc);
        assertThat(DocumentListUtil.extractDocumentsFromListValues(new ArrayList<>())).isEmpty();
        assertThat(DocumentListUtil.extractDocumentsFromListValues(null)).isEmpty();
    }


    @Test
    void givenCicDoc_whenRemoveFurtherUploadedDocument_thenRemoveIfExists() {
        //given

        final CaseData caseData = CaseData.builder().build();
        List<ListValue<CaseworkerCICDocument>> furtherUploadedDocsList = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name").build())
            .build();
        ListValue<CaseworkerCICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(doc);
        furtherUploadedDocsList.add(documentListValue);

        caseData.setFurtherUploadedDocuments(furtherUploadedDocsList);

        //when
        DocumentListUtil.removeFurtherUploadedDocument(caseData, documentListValue);


        //then
        assertThat(caseData.getFurtherUploadedDocuments()).isEmpty();

    }

    @Test
    void givenCicDoc_whenRemoveFurtherUploadedDocument_thenNoMatchAndDontRemove() {
        //given

        final CaseData caseData = CaseData.builder().build();
        List<ListValue<CaseworkerCICDocument>> furtherUploadedDocsList = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name").build())
            .build();
        ListValue<CaseworkerCICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(doc);
        furtherUploadedDocsList.add(documentListValue);

        caseData.setFurtherUploadedDocuments(furtherUploadedDocsList);

        CaseworkerCICDocument differentDocument = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url123").binaryUrl("url123").filename("name123").build())
            .build();

        ListValue<CaseworkerCICDocument> differentDocumentListValue = new ListValue<>();
        differentDocumentListValue.setValue(differentDocument);

        //when
        DocumentListUtil.removeFurtherUploadedDocument(caseData, differentDocumentListValue);


        //then
        assertThat(caseData.getFurtherUploadedDocuments()).isEqualTo(List.of(documentListValue));

    }

    @Test
    void givenCicDocAndNullOrders_whenRemoveFurtherUploadedDocument_thenDoNothing() {
        //given
        final CaseData caseData = CaseData.builder().build();

        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name").build())
            .build();
        ListValue<CaseworkerCICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(doc);

        //when
        DocumentListUtil.removeFurtherUploadedDocument(caseData, documentListValue);


        //then
        assertThat(caseData.getFurtherUploadedDocuments()).isNull();
    }

    @Test
    void shouldRemoveInitialCaseDocuments_whenDocumentMatches() {
        final CaseData caseData = CaseData.builder().build();

        List<ListValue<CaseworkerCICDocument>> initialDocs = TestDataHelper.get2Document();
        caseData.setInitialCicaDocuments(initialDocs);

        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                .documentCategory(DocumentType.LINKED_DOCS)
                .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build())
                .build();

        ListValue<CaseworkerCICDocument> docToRemove =
            ListValue.<CaseworkerCICDocument>builder().value(doc).build();

        DocumentListUtil.removeInitialCaseDocuments(caseData, docToRemove);

        assertThat(caseData.getInitialCicaDocuments()).hasSize(1);

    }

    @Test
    void shouldNotRemoveInitialCaseDocuments_whenNoDocumentMatches() {
        final CaseData caseData = CaseData.builder().build();

        List<ListValue<CaseworkerCICDocument>> initialDocs = TestDataHelper.get2Document();
        caseData.setInitialCicaDocuments(initialDocs);

        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                .documentCategory(DocumentType.LINKED_DOCS)
                .documentLink(Document.builder().url("url2").binaryUrl("url2").filename("name2").build())
                .build();

        ListValue<CaseworkerCICDocument> docToRemove =
                ListValue.<CaseworkerCICDocument>builder().value(doc).build();

        DocumentListUtil.removeInitialCaseDocuments(caseData, docToRemove);

        assertThat(caseData.getInitialCicaDocuments()).hasSize(2);

    }

    @Test
    void shouldNotRemoveInitialCaseDocuments_whenEmpty() {
        final CaseData caseData = CaseData.builder().build();
        caseData.setInitialCicaDocuments(new ArrayList<>());

        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                .documentCategory(DocumentType.LINKED_DOCS)
                .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build())
                .build();

        ListValue<CaseworkerCICDocument> docToRemove =
                ListValue.<CaseworkerCICDocument>builder().value(doc).build();

        DocumentListUtil.removeInitialCaseDocuments(caseData, docToRemove);

        assertThat(caseData.getInitialCicaDocuments()).isNotNull();
        assertThat(caseData.getInitialCicaDocuments()).isEmpty();

    }

    @Test
    void shouldNotRemoveInitialCaseDocuments_whenNull() {
        final CaseData caseData = CaseData.builder().build();

        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                .documentCategory(DocumentType.LINKED_DOCS)
                .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build())
                .build();

        ListValue<CaseworkerCICDocument> docToRemove =
                ListValue.<CaseworkerCICDocument>builder().value(doc).build();

        DocumentListUtil.removeInitialCaseDocuments(caseData, docToRemove);

        assertThat(caseData.getInitialCicaDocuments()).isNull();

    }
}
