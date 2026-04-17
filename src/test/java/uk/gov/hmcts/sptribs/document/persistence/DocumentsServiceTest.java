package uk.gov.hmcts.sptribs.document.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessException;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.common.repositories.exception.CaseEventRepositoryException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class DocumentsServiceTest {
    @InjectMocks
    private DocumentsService documentsService;

    @Mock
    private DocumentsRepository documentsRepository;

    private static final DocumentEntity EXPECTED_TEST_DOCUMENT_ENTITY_NON_DRAFT = DocumentEntity.builder()
        .caseReferenceNumber(TEST_CASE_ID)
            .documentUrl("example.com/test-document.pdf")
            .documentFilename("test-document.pdf")
            .documentBinaryUrl("example.com/test-document.pdf/binary")
            .categoryId("testCategory")
            .isDraft(false)
            .build();

    private static final DocumentEntity EXPECTED_TEST_DOCUMENT_ENTITY_DRAFT = DocumentEntity.builder()
        .caseReferenceNumber(TEST_CASE_ID)
        .documentUrl("example.com/test-document.pdf")
        .documentFilename("test-document.pdf")
        .documentBinaryUrl("example.com/test-document.pdf/binary")
        .categoryId("testCategory")
        .isDraft(true)
        .build();

    private static final Document TEST_DOCUMENT = Document.builder()
        .url("example.com/test-document.pdf")
        .filename("test-document.pdf")
        .binaryUrl("example.com/test-document.pdf/binary")
        .categoryId("testCategory")
        .build();

    @Test
    public void shouldBuildAndSaveNewNonDraftDocumentEntity() {
        documentsService.buildAndSaveNewDocumentEntity(TEST_DOCUMENT, TEST_CASE_ID, false);

        verify(documentsRepository, times(1))
            .findAllByDocumentBinaryUrl(EXPECTED_TEST_DOCUMENT_ENTITY_NON_DRAFT.getDocumentBinaryUrl());
        verify(documentsRepository, times(1)).save(EXPECTED_TEST_DOCUMENT_ENTITY_NON_DRAFT);
    }

    @Test
    public void shouldBuildAndSaveNewDraftDocumentEntity() {
        documentsService.buildAndSaveNewDocumentEntity(TEST_DOCUMENT, TEST_CASE_ID, true);

        verify(documentsRepository, times(1))
            .findAllByDocumentBinaryUrl(EXPECTED_TEST_DOCUMENT_ENTITY_NON_DRAFT.getDocumentBinaryUrl());
        verify(documentsRepository, times(1)).save(EXPECTED_TEST_DOCUMENT_ENTITY_DRAFT);
    }

    @Test
    public void shouldNotBuildAndSaveDuplicateDocumentEntity() {
        when(documentsRepository.findAllByDocumentBinaryUrl(TEST_DOCUMENT.getBinaryUrl()))
            .thenReturn(java.util.List.of(EXPECTED_TEST_DOCUMENT_ENTITY_NON_DRAFT));

        documentsService.buildAndSaveNewDocumentEntity(TEST_DOCUMENT, TEST_CASE_ID, false);

        verify(documentsRepository, times(1))
            .findAllByDocumentBinaryUrl(EXPECTED_TEST_DOCUMENT_ENTITY_NON_DRAFT.getDocumentBinaryUrl());
        verify(documentsRepository, times(0)).save(EXPECTED_TEST_DOCUMENT_ENTITY_NON_DRAFT);
    }

    @Test
    public void shouldThrowRuntimeExceptionWhenDataAccessExceptionCaughtInBuildAndSaveNewDraftDocumentEntity() {
        when(documentsRepository.findAllByDocumentBinaryUrl(TEST_DOCUMENT.getBinaryUrl()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        assertThatThrownBy(() -> documentsService.buildAndSaveNewDocumentEntity(TEST_DOCUMENT, TEST_CASE_ID, false))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error saving document entity to database")
            .hasCauseInstanceOf(DataAccessException.class);
    }
}
