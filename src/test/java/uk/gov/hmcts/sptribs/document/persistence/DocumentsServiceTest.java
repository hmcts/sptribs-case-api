package uk.gov.hmcts.sptribs.document.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.services.DocumentsService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
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

    private static final DocumentEntity EXPECTED_TEST_DOCUMENT_ENTITY_NON_DRAFT_NOT_SENT_VIA_CONTACT_PARTIES = DocumentEntity.builder()
        .caseReferenceNumber(TEST_CASE_ID)
        .documentUrl("example.com/test-document.pdf")
        .documentFilename("test-document.pdf")
        .documentBinaryUrl("example.com/test-document.pdf/binary")
        .categoryId("testCategory")
        .isDraft(false)
        .sentToApplicantViaContactParties(false)
        .build();

    private static final DocumentEntity EXPECTED_TEST_DOCUMENT_ENTITY_NON_DRAFT_SENT_VIA_CONTACT_PARTIES = DocumentEntity.builder()
        .caseReferenceNumber(TEST_CASE_ID)
        .documentUrl("example.com/test-document.pdf")
        .documentFilename("test-document.pdf")
        .documentBinaryUrl("example.com/test-document.pdf/binary")
        .categoryId("testCategory")
        .isDraft(false)
        .sentToApplicantViaContactParties(true)
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

        verify(documentsRepository, times(1)).save(EXPECTED_TEST_DOCUMENT_ENTITY_NON_DRAFT);
    }

    @Test
    public void shouldBuildAndSaveNewDraftDocumentEntity() {
        documentsService.buildAndSaveNewDocumentEntity(TEST_DOCUMENT, TEST_CASE_ID, true);

        verify(documentsRepository, times(1)).save(EXPECTED_TEST_DOCUMENT_ENTITY_DRAFT);
    }

    @Test
    public void shouldThrowRuntimeExceptionWhenDataAccessExceptionCaughtInBuildAndSaveNewDraftDocumentEntity() {
        when(documentsRepository.save(EXPECTED_TEST_DOCUMENT_ENTITY_DRAFT))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        assertThatThrownBy(() -> documentsService.buildAndSaveNewDocumentEntity(TEST_DOCUMENT, TEST_CASE_ID, false))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error saving document entity to database")
            .hasCauseInstanceOf(DataAccessException.class);
    }

    @Test
    public void shouldSetSentToApplicantViaContactPartiesToTrue() {

        documentsService.setSentToApplicantViaContactPartiesToTrue(TEST_DOCUMENT.getBinaryUrl());

        verify(documentsRepository, times(1)).setSentToApplicantViaContactPartiesToTrueByDocumentBinaryUrl(
            EXPECTED_TEST_DOCUMENT_ENTITY_NON_DRAFT_SENT_VIA_CONTACT_PARTIES.getDocumentBinaryUrl()
        );
    }

    @Test
    public void shouldThrowRuntimeExceptionWhenDataAccessExceptionCaughtInSetSentToApplicantViaContactPartiesToTrue() {
        doThrow(new DataAccessResourceFailureException("DB error"))
            .when(documentsRepository).setSentToApplicantViaContactPartiesToTrueByDocumentBinaryUrl(TEST_DOCUMENT.getBinaryUrl());

        assertThatThrownBy(() -> documentsService.setSentToApplicantViaContactPartiesToTrue(TEST_DOCUMENT.getBinaryUrl()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error updating sent_to_applicant_via_contact_parties to true")
            .hasCauseInstanceOf(DataAccessException.class);
    }
}
