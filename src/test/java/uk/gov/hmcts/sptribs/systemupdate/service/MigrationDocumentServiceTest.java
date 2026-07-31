package uk.gov.hmcts.sptribs.systemupdate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.service.CaseDocumentTypesCache;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class MigrationDocumentServiceTest {

    @InjectMocks
    private MigrationDocumentService documentsService;

    @Mock
    private DocumentsRepository documentsRepository;

    @Mock
    private CaseDocumentTypesCache caseDocumentTypesCache;

    private static final DocumentType HOSPITAL_RECORDS = DocumentType.HOSPITAL_RECORDS;

    private static final LocalDateTime dateTime = LocalDateTime.now();

    @Test
    void shouldBuildAndSaveNewCaseworkerDocumentEntity() {
        Document evidenceDocument = buildDocument(HOSPITAL_RECORDS.getCategory());
        DocumentEntity evidenceDocumentEntity = buildDocumentEntity(HOSPITAL_RECORDS.name(), 2L, OffsetDateTime.now());

        when(caseDocumentTypesCache.getId(CaseDocumentType.DOCUMENT_MANAGEMENT)).thenReturn(2L);

        documentsService.buildAndSaveNewDocumentEntityWithDocDateTime(evidenceDocument, TEST_CASE_ID, HOSPITAL_RECORDS,
            CaseDocumentType.DOCUMENT_MANAGEMENT, dateTime);

        verify(documentsRepository, times(1)).save(evidenceDocumentEntity);
    }

    @Test
    void shouldBuildAndSaveNewBundleDocument() {
        Document bundleDocument = buildDocument(null);
        DocumentEntity bundleDocumentEntity = buildDocumentEntity(null, 9L, OffsetDateTime.now());

        when(caseDocumentTypesCache.getId(CaseDocumentType.BUNDLE)).thenReturn(9L);

        documentsService.buildAndSaveNewDocumentEntityWithDocDateTime(bundleDocument, TEST_CASE_ID, null,
            CaseDocumentType.BUNDLE, dateTime);

        verify(documentsRepository, times(1)).save(bundleDocumentEntity);
    }

    @Test
    void shouldBuildAndSaveNewDraftOrderDocumentEntity() {
        Document draftEvidenceDocument = buildDocument(HOSPITAL_RECORDS.getCategory());
        DocumentEntity draftEvidenceDocumentEntity = buildDocumentEntity(HOSPITAL_RECORDS.name(), 4L, OffsetDateTime.now());

        when(caseDocumentTypesCache.getId(CaseDocumentType.DRAFT_ORDER)).thenReturn(4L);

        documentsService.buildAndSaveNewDocumentEntityWithDocDateTime(draftEvidenceDocument, TEST_CASE_ID, HOSPITAL_RECORDS,
            CaseDocumentType.DRAFT_ORDER, dateTime);

        verify(documentsRepository, times(1)).save(draftEvidenceDocumentEntity);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenDataAccessExceptionCaughtInBuildAndSaveNewDraftDocumentEntity() {
        Document draftEvidenceDocument = buildDocument(HOSPITAL_RECORDS.getCategory());
        DocumentEntity draftEvidenceDocumentEntity = buildDocumentEntity(HOSPITAL_RECORDS.name(), 4L,
            OffsetDateTime.now());

        when(documentsRepository.save(draftEvidenceDocumentEntity)).thenThrow(new DataAccessResourceFailureException("DB error"));
        when(caseDocumentTypesCache.getId(CaseDocumentType.DRAFT_ORDER)).thenReturn(4L);

        assertThatThrownBy(
            () -> documentsService.buildAndSaveNewDocumentEntityWithDocDateTime(draftEvidenceDocument, TEST_CASE_ID, HOSPITAL_RECORDS,
                CaseDocumentType.DRAFT_ORDER, dateTime)).isInstanceOf(
            RuntimeException.class).hasMessageContaining("Error saving document entity to database").hasCauseInstanceOf(
            DataAccessException.class);
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
