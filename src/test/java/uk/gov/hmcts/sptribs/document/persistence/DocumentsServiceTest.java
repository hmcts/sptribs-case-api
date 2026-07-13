package uk.gov.hmcts.sptribs.document.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentDashboardModel;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.service.CaseDocumentTypesCache;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.buildCaseworkerCicDocumentListValue;

@ExtendWith(MockitoExtension.class)
public class DocumentsServiceTest {

    @InjectMocks
    private DocumentsService documentsService;

    @Mock
    private DocumentsRepository documentsRepository;

    @Mock
    private CaseDocumentTypesCache caseDocumentTypesCache;

    private static final DocumentType HOSPITAL_RECORDS = DocumentType.HOSPITAL_RECORDS;
    private static final DocumentType DSS_SUPPORTING = DocumentType.DSS_SUPPORTING;
    private static final DocumentType ORDER_AND_DECISION_DOCUMENT = DocumentType.TRIBUNAL_DIRECTION;

    @Test
    public void shouldBuildAndSaveNewCaseworkerDocumentEntity() {
        Document evidenceDocument = buildDocument(HOSPITAL_RECORDS.getCategory());
        DocumentEntity evidenceDocumentEntity = buildDocumentEntity(HOSPITAL_RECORDS.name(), 2L, OffsetDateTime.now());

        when(caseDocumentTypesCache.getId(CaseDocumentType.DOCUMENT_MANAGEMENT)).thenReturn(2L);

        documentsService.buildAndSaveNewDocumentEntity(evidenceDocument, TEST_CASE_ID, HOSPITAL_RECORDS, CaseDocumentType.DOCUMENT_MANAGEMENT);

        verify(documentsRepository, times(1)).save(evidenceDocumentEntity);
    }

    @Test
    public void shouldBuildAndSaveNewBundleDocument() {
        Document bundleDocument = buildDocument(null);
        DocumentEntity bundleDocumentEntity = buildDocumentEntity(null, 9L, OffsetDateTime.now());

        when(caseDocumentTypesCache.getId(CaseDocumentType.BUNDLE)).thenReturn(9L);

        documentsService.buildAndSaveNewDocumentEntity(bundleDocument, TEST_CASE_ID, null,CaseDocumentType.BUNDLE);

        verify(documentsRepository, times(1)).save(bundleDocumentEntity);
    }

    @Test
    public void shouldBuildAndSaveNewDraftOrderDocumentEntity() {
        Document draftEvidenceDocument = buildDocument(HOSPITAL_RECORDS.getCategory());
        DocumentEntity draftEvidenceDocumentEntity = buildDocumentEntity(HOSPITAL_RECORDS.name(), 4L, OffsetDateTime.now());

        when(caseDocumentTypesCache.getId(CaseDocumentType.DRAFT_ORDER)).thenReturn(4L);

        documentsService.buildAndSaveNewDocumentEntity(draftEvidenceDocument, TEST_CASE_ID, HOSPITAL_RECORDS,
            CaseDocumentType.DRAFT_ORDER);

        verify(documentsRepository, times(1)).save(draftEvidenceDocumentEntity);
    }

    @Test
    public void shouldThrowRuntimeExceptionWhenDataAccessExceptionCaughtInBuildAndSaveNewDraftDocumentEntity() {
        Document draftEvidenceDocument = buildDocument(HOSPITAL_RECORDS.getCategory());
        DocumentEntity draftEvidenceDocumentEntity = buildDocumentEntity(HOSPITAL_RECORDS.name(), 4L,
            OffsetDateTime.now());

        when(documentsRepository.save(draftEvidenceDocumentEntity)).thenThrow(new DataAccessResourceFailureException("DB error"));
        when(caseDocumentTypesCache.getId(CaseDocumentType.DRAFT_ORDER)).thenReturn(4L);

        assertThatThrownBy(
            () -> documentsService.buildAndSaveNewDocumentEntity(draftEvidenceDocument, TEST_CASE_ID, HOSPITAL_RECORDS,
                CaseDocumentType.DRAFT_ORDER)).isInstanceOf(
            RuntimeException.class).hasMessageContaining("Error saving document entity to database").hasCauseInstanceOf(
            DataAccessException.class);
    }

    @Test
    public void shouldSetNewDocumentTypeName() {
        Document applicationDocument = buildDocument(HOSPITAL_RECORDS.getCategory());
        DocumentEntity applicationDocumentEntity = buildDocumentEntity(DSS_SUPPORTING.name(), 1L, OffsetDateTime.now());

        documentsService.setNewDocumentTypeName(applicationDocument.getBinaryUrl(), DSS_SUPPORTING.name());

        verify(documentsRepository, times(1)).setDocumentTypeNameByDocumentBinaryUrl(
            applicationDocumentEntity.getDocumentBinaryUrl(), DSS_SUPPORTING.name());
    }

    @Test
    public void shouldThrowRuntimeExceptionWhenDataAccessExceptionCaughtInSetNewDocumentTypeName() {
        Document applicationDocument = buildDocument(DSS_SUPPORTING.getCategory());

        doThrow(new DataAccessResourceFailureException("DB error")).when(
            documentsRepository).setDocumentTypeNameByDocumentBinaryUrl(
                applicationDocument.getBinaryUrl(),
                DSS_SUPPORTING.name());

        assertThatThrownBy(
            () -> documentsService.setNewDocumentTypeName(applicationDocument.getBinaryUrl(), DSS_SUPPORTING.name()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error updating document type name")
            .hasCauseInstanceOf(DataAccessException.class);
    }

    @Test
    public void shouldUpdateDocumentToNonDraftOrder() {
        Document applicationDocument = buildDocument(HOSPITAL_RECORDS.getCategory());
        DocumentEntity draftEvidenceDocumentEntity = buildDocumentEntity(HOSPITAL_RECORDS.name(), 3L,
            OffsetDateTime.now());

        when(caseDocumentTypesCache.getId(CaseDocumentType.ORDER)).thenReturn(3L);

        documentsService.updateDocumentToNonDraft(applicationDocument.getBinaryUrl());

        verify(documentsRepository, times(1)).updateCaseDocumentTypeIdByDocumentBinaryUrl(
            draftEvidenceDocumentEntity.getDocumentBinaryUrl(), 3L);
    }

    @Test
    public void shouldThrowRuntimeExceptionWhenDataAccessExceptionCaughtInUpdateDocumentToNonDraft() {
        Document applicationDocument = buildDocument(DSS_SUPPORTING.getCategory());
        when(caseDocumentTypesCache.getId(CaseDocumentType.ORDER)).thenReturn(3L);

        doThrow(new DataAccessResourceFailureException("DB error")).when(documentsRepository).updateCaseDocumentTypeIdByDocumentBinaryUrl(
            applicationDocument.getBinaryUrl(), 3L);

        assertThatThrownBy(() -> documentsService.updateDocumentToNonDraft(applicationDocument.getBinaryUrl())).isInstanceOf(
                RuntimeException.class).hasMessageContaining("Error updating case document type from draft order to order")
            .hasCauseInstanceOf(DataAccessException.class);
    }

    @Test
    void shouldGetDocumentsSentViaContactParties() {

        ListValue<CaseworkerCICDocument> caseDocument1 = buildCaseworkerCicDocumentListValue("url-1", "my-env/binary-1/binary", "file-1");
        ListValue<CaseworkerCICDocument> caseDocument2 = buildCaseworkerCicDocumentListValue("url-2", "my-env/binary-2/binary", "file-2");

        DocumentManagement documentManagement = DocumentManagement.builder().caseworkerCICDocument(
            List.of(caseDocument1, caseDocument2)).build();

        CaseData caseData = CaseData.builder().allDocManagement(documentManagement).build();

        Map<String, String> uploadedDocuments = buildUploadedDocuments();

        when(documentsRepository.findIdsByDocumentBinaryUrls(
            List.of("my-env/binary-1/binary", "my-env/binary-2/binary"))).thenReturn(2);

        documentsService.getDocumentsViaSentByContactParties(caseData, uploadedDocuments);

        verify(documentsRepository).findIdsByDocumentBinaryUrls(
            List.of("my-env/binary-1/binary", "my-env/binary-2/binary"));
    }

    @Test
    void shouldGetDocumentsUnderLimitSentViaContactParties() {

        //3 and 4 should not be included as map has reached limit for emails...
        ListValue<CaseworkerCICDocument> caseDocument1 = buildCaseworkerCicDocumentListValue("url-1", "my-env/binary-1/binary", "file-1");
        ListValue<CaseworkerCICDocument> caseDocument2 = buildCaseworkerCicDocumentListValue("url-2", "my-env/binary-2/binary", "file-2");
        ListValue<CaseworkerCICDocument> caseDocument3 = buildCaseworkerCicDocumentListValue("url-3", "my-env/binary-3/binary", "file-3");
        ListValue<CaseworkerCICDocument> caseDocument4 = buildCaseworkerCicDocumentListValue("url-4", "my-env/binary-4/binary", "file-4");

        DocumentManagement documentManagement = DocumentManagement.builder().caseworkerCICDocument(
            List.of(caseDocument1, caseDocument2, caseDocument3, caseDocument4)).build();

        CaseData caseData = CaseData.builder().allDocManagement(documentManagement).build();

        Map<String, String> uploadedDocuments = buildUploadedDocuments();

        when(documentsRepository.findIdsByDocumentBinaryUrls(
            List.of("my-env/binary-1/binary", "my-env/binary-2/binary"))).thenReturn(2);

        documentsService.getDocumentsViaSentByContactParties(caseData, uploadedDocuments);

        verify(documentsRepository).findIdsByDocumentBinaryUrls(
            List.of("my-env/binary-1/binary", "my-env/binary-2/binary"));
    }

    @Test
    void shouldReturnDocumentDashboardModelForCCDRefWithDocsInCorrectPlace() {

        //what happens when order/decision doc is in both orders list and sent out list? for now lets make sure its there once in order?
        //given

        DocumentEntity orderDocument = buildDocumentEntity(ORDER_AND_DECISION_DOCUMENT.name(), 3L, OffsetDateTime.now());

        DocumentEntity bundleDocument = buildDocumentEntity(null, 9L, OffsetDateTime.now());

        DocumentEntity documentSentOutViaContactParties1 = buildDocumentEntity(HOSPITAL_RECORDS.name(), 2L, OffsetDateTime.now());
        DocumentEntity documentSentOutViaContactParties2 = buildDocumentEntity(DSS_SUPPORTING.name(), 1L, OffsetDateTime.now());

        DocumentEntity orderDocumentSentViaContactParties = buildDocumentEntity(ORDER_AND_DECISION_DOCUMENT.name(), 3L,
            OffsetDateTime.now());

        when(caseDocumentTypesCache.getId(CaseDocumentType.ORDER)).thenReturn(3L);
        when(caseDocumentTypesCache.getId(CaseDocumentType.DECISION)).thenReturn(5L);
        when(caseDocumentTypesCache.getId(CaseDocumentType.FINAL_DECISION)).thenReturn(6L);
        when(caseDocumentTypesCache.getId(CaseDocumentType.BUNDLE)).thenReturn(9L);

        when(documentsRepository.findContactPartyDocuments(TEST_CASE_ID, List.of(
            Party.APPLICANT,
            Party.REPRESENTATIVE,
            Party.SUBJECT
        ))).thenReturn();

        when(documentsRepository.findLatestBundleDocument(TEST_CASE_ID, 6L)).thenReturn(Optional.of(bundleDocument));

        when(documentsRepository.findOrderAndDecisionDocuments(TEST_CASE_ID, List.of(3L, 5L, 6L))).thenReturn();


        //when
        DocumentDashboardModel actualResult = documentsService.getDocumentsOnCase(TEST_CASE_ID);

        //then
        assertThat(actualResult).isNotNull();

        assertThat(actualResult.getOrderAndDecisionDocuments()).containsExactlyInAnyOrder(orderDocument,
            orderDocumentSentViaContactParties);

        assertThat(actualResult.getLatestCaseBundleDocument()).isEqualTo(bundleDocument);

        assertThat(actualResult.getContactPartiesDocuments()).containsExactlyInAnyOrder(documentSentOutViaContactParties1,
            documentSentOutViaContactParties2);

    }

    @Test
    void shouldReturnEmptyModelWhenNoConditionsMet() {
        //given
        List<DocumentEntity> documentEntities = new ArrayList<>();

        DocumentEntity applicantDoc = buildDocumentEntity(DSS_SUPPORTING.name(), 1L, false,
            OffsetDateTime.now());

        documentEntities.add(applicantDoc);

        when(documentsRepository.findAllDocumentsByCaseReference(TEST_CASE_ID)).thenReturn(documentEntities);
        when(caseDocumentTypesCache.getId(CaseDocumentType.ORDER)).thenReturn(4L);
        when(caseDocumentTypesCache.getId(CaseDocumentType.BUNDLE)).thenReturn(10L);

        //when
        DocumentDashboardModel actualResult = documentsService.getDocumentsOnCase(TEST_CASE_ID);

        //then
        assertThat(actualResult).isNotNull();

    }

    private Map<String, String> buildUploadedDocuments() {

        return Map.of("CaseDocument1", "binary-1", "CaseDocument2", "binary-2", "DocumentAvailable1", "yes", "DocumentAvailable2", "yes");
    }

    private DocumentEntity buildDocumentEntity(String docTypeName, Long caseDocumentTypeId, OffsetDateTime offsetDateTime) {
        return DocumentEntity.builder()
            .caseReferenceNumber(TEST_CASE_ID)
            .documentUrl("example.com/test-document.pdf")
            .documentFilename("test-document.pdf")
            .documentBinaryUrl("example.com/test-document.pdf/binary")
            .documentTypeName(docTypeName)
            .caseDocumentTypeId(caseDocumentTypeId)
            .savedAt(offsetDateTime)
            .build();
    }

    private Document buildDocument(String categoryId) {
        return Document.builder()
            .url("example.com/test-document.pdf")
            .filename("test-document.pdf")
            .binaryUrl("example.com/test-document.pdf/binary")
            .categoryId(categoryId)
            .build();
    }

}
