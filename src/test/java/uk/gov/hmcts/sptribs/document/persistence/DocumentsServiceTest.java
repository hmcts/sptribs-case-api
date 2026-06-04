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
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.service.CaseDocumentTypesCache;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;

import java.util.List;

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

    @Mock
    private CaseDocumentTypesCache caseDocumentTypesCache;

    private static final DocumentType HOSPITAL_RECORDS = DocumentType.HOSPITAL_RECORDS;
    private static final DocumentType DSS_SUPPORTING = DocumentType.DSS_SUPPORTING;

    private static final DocumentEntity EXPECTED_TEST_DOCUMENT_ENTITY_NON_DRAFT = DocumentEntity.builder()
        .caseReferenceNumber(TEST_CASE_ID)
            .documentUrl("example.com/test-document.pdf")
            .documentFilename("test-document.pdf")
            .documentBinaryUrl("example.com/test-document.pdf/binary")
            .categoryId(HOSPITAL_RECORDS.getCategory())
            .documentTypeId(2L)
            .isDraft(false)
            .build();

    private static final DocumentEntity EXPECTED_TEST_DOCUMENT_ENTITY_DRAFT = DocumentEntity.builder()
        .caseReferenceNumber(TEST_CASE_ID)
        .documentUrl("example.com/test-document.pdf")
        .documentFilename("test-document.pdf")
        .documentBinaryUrl("example.com/test-document.pdf/binary")
        .categoryId(HOSPITAL_RECORDS.getCategory())
        .documentTypeId(2L)
        .isDraft(true)
        .build();

    private static final DocumentEntity EXPECTED_TEST_DOCUMENT_ENTITY_NON_DRAFT_SENT_VIA_CONTACT_PARTIES = DocumentEntity.builder()
        .caseReferenceNumber(TEST_CASE_ID)
        .documentUrl("example.com/test-document.pdf")
        .documentFilename("test-document.pdf")
        .documentBinaryUrl("example.com/test-document.pdf/binary")
        .categoryId(DSS_SUPPORTING.getCategory())
        .documentTypeId(1L)
        .isDraft(false)
        .sentToApplicantViaContactParties(true)
        .build();

    private static final Document TEST_DOCUMENT_APPLICATION = Document.builder()
        .url("example.com/test-document.pdf")
        .filename("test-document.pdf")
        .binaryUrl("example.com/test-document.pdf/binary")
        .categoryId(DSS_SUPPORTING.getCategory())
        .build();

    private static final Document TEST_DOCUMENT_EVIDENCE = Document.builder()
        .url("example.com/test-document.pdf")
        .filename("test-document.pdf")
        .binaryUrl("example.com/test-document.pdf/binary")
        .categoryId(HOSPITAL_RECORDS.getCategory())
        .build();

    @Test
    public void shouldBuildAndSaveNewNonDraftDocumentEntity() {
        when(caseDocumentTypesCache.getId(HOSPITAL_RECORDS.getCaseDocumentType())).thenReturn(2L);

        documentsService.buildAndSaveNewDocumentEntity(TEST_DOCUMENT_EVIDENCE, TEST_CASE_ID, false, false);

        verify(documentsRepository, times(1)).save(EXPECTED_TEST_DOCUMENT_ENTITY_NON_DRAFT);
    }

    @Test
    public void shouldBuildAndSaveNewDraftDocumentEntity() {

        when(caseDocumentTypesCache.getId(HOSPITAL_RECORDS.getCaseDocumentType())).thenReturn(2L);

        documentsService.buildAndSaveNewDocumentEntity(TEST_DOCUMENT_EVIDENCE, TEST_CASE_ID, true, false);

        verify(documentsRepository, times(1)).save(EXPECTED_TEST_DOCUMENT_ENTITY_DRAFT);
    }

    @Test
    public void shouldThrowRuntimeExceptionWhenDataAccessExceptionCaughtInBuildAndSaveNewDraftDocumentEntity() {
        when(documentsRepository.save(EXPECTED_TEST_DOCUMENT_ENTITY_DRAFT))
            .thenThrow(new DataAccessResourceFailureException("DB error"));
        when(caseDocumentTypesCache.getId(HOSPITAL_RECORDS.getCaseDocumentType())).thenReturn(2L);

        assertThatThrownBy(() -> documentsService.buildAndSaveNewDocumentEntity(TEST_DOCUMENT_EVIDENCE, TEST_CASE_ID, false,
            false))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error saving document entity to database")
            .hasCauseInstanceOf(DataAccessException.class);
    }

    @Test
    public void shouldSetSentToApplicantViaContactPartiesToTrue() {

        documentsService.setSentToApplicantViaContactPartiesToTrue(List.of(TEST_DOCUMENT_APPLICATION.getBinaryUrl()));

        verify(documentsRepository, times(1)).setSentToApplicantViaContactPartiesToTrueByDocumentBinaryUrl(
            List.of(EXPECTED_TEST_DOCUMENT_ENTITY_NON_DRAFT_SENT_VIA_CONTACT_PARTIES.getDocumentBinaryUrl())
        );
    }

    @Test
    public void shouldThrowRuntimeExceptionWhenDataAccessExceptionCaughtInSetSentToApplicantViaContactPartiesToTrue() {
        doThrow(new DataAccessResourceFailureException("DB error"))
            .when(documentsRepository).setSentToApplicantViaContactPartiesToTrueByDocumentBinaryUrl(
                List.of(TEST_DOCUMENT_APPLICATION.getBinaryUrl()));

        assertThatThrownBy(() -> documentsService.setSentToApplicantViaContactPartiesToTrue(
            List.of(TEST_DOCUMENT_APPLICATION.getBinaryUrl())))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error updating sent_to_applicant_via_contact_parties to true")
            .hasCauseInstanceOf(DataAccessException.class);
    }

    @Test
    public void shouldSetIsDraftToFalse() {

        documentsService.setIsDraftToFalse(TEST_DOCUMENT_EVIDENCE.getBinaryUrl());

        verify(documentsRepository, times(1)).setIsDraftToFalseByDocumentBinaryUrl(
            EXPECTED_TEST_DOCUMENT_ENTITY_NON_DRAFT.getDocumentBinaryUrl()
        );
    }

    @Test
    public void shouldThrowRuntimeExceptionWhenDataAccessExceptionCaughtInSetIsDraftToFalse() {
        doThrow(new DataAccessResourceFailureException("DB error"))
            .when(documentsRepository).setIsDraftToFalseByDocumentBinaryUrl(TEST_DOCUMENT_APPLICATION.getBinaryUrl());

        assertThatThrownBy(() -> documentsService.setIsDraftToFalse(TEST_DOCUMENT_APPLICATION.getBinaryUrl()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error updating is_draft to false")
            .hasCauseInstanceOf(DataAccessException.class);
    }
}
