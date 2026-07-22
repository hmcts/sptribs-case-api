package uk.gov.hmcts.sptribs.document.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.common.repositories.DocumentDownloadStatusesRepository;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.document.model.DocumentDownloadStatusEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentDownloadStatusServiceTest {

    @InjectMocks
    private DocumentDownloadStatusService documentDownloadStatusService;

    @Mock
    private DocumentsRepository documentsRepository;

    @Mock
    private DocumentDownloadStatusesRepository documentDownloadStatusesRepository;

    @Test
    public void shouldRecordNewDocumentDownloadForSubject() {
        String ref = "1234567890123456";
        String docUuid = "uuid-123";

        DocumentEntity docEntity = DocumentEntity.builder()
            .id(100L)
            .build();

        when(documentsRepository.findByDocumentIdUuid(docUuid)).thenReturn(Optional.of(docEntity));
        when(documentDownloadStatusesRepository.findByDocumentIdAndParty(100L, Party.SUBJECT)).thenReturn(Optional.empty());

        documentDownloadStatusService.recordDocumentDownload(ref, Party.SUBJECT, docUuid);

        verify(documentDownloadStatusesRepository, times(1)).save(org.mockito.ArgumentMatchers.any(DocumentDownloadStatusEntity.class));
    }

    @Test
    public void shouldUpdateExistingDocumentDownloadForSubject() {
        String ref = "1234567890123456";
        String docUuid = "uuid-123";

        DocumentEntity docEntity = DocumentEntity.builder()
            .id(100L)
            .build();

        DocumentDownloadStatusEntity statusEntity = DocumentDownloadStatusEntity.builder()
            .id(1L)
            .caseReferenceNumber(1234567890123456L)
            .documentId(100L)
            .party(Party.SUBJECT)
            .build();

        when(documentsRepository.findByDocumentIdUuid(docUuid)).thenReturn(Optional.of(docEntity));
        when(documentDownloadStatusesRepository.findByDocumentIdAndParty(100L, Party.SUBJECT)).thenReturn(Optional.of(statusEntity));

        documentDownloadStatusService.recordDocumentDownload(ref, Party.SUBJECT, docUuid);

        verify(documentDownloadStatusesRepository, times(1)).save(statusEntity);
    }

    @Test
    public void shouldNotRecordDownloadIfDocumentNotFound() {
        String ref = "1234567890123456";

        when(documentsRepository.findByDocumentIdUuid("uuid-123")).thenReturn(Optional.empty());

        documentDownloadStatusService.recordDocumentDownload(ref, Party.SUBJECT, "uuid-123");

        verify(documentDownloadStatusesRepository, times(0)).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void shouldNotRecordDownloadIfPartyIsNull() {
        documentDownloadStatusService.recordDocumentDownload("1234567890123456", null, "uuid-123");

        verify(documentDownloadStatusesRepository, times(0)).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void shouldReturnDownloadedDocumentIds() {
        String ref = "1234567890123456";

        DocumentDownloadStatusEntity status1 = DocumentDownloadStatusEntity.builder()
            .documentId(101L)
            .build();

        DocumentDownloadStatusEntity status2 = DocumentDownloadStatusEntity.builder()
            .documentId(102L)
            .build();

        when(documentDownloadStatusesRepository.findAllByCaseReferenceNumberAndParty(1234567890123456L, Party.SUBJECT))
            .thenReturn(List.of(status1, status2));

        Set<Long> result = documentDownloadStatusService.getDownloadedDocumentIds(ref, Party.SUBJECT);

        assertThat(result).containsExactlyInAnyOrder(101L, 102L);
    }

    @Test
    public void shouldReturnEmptySetIfPartyIsNull() {
        Set<Long> result = documentDownloadStatusService.getDownloadedDocumentIds("1234567890123456", null);

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldTrackIndependentDownloadStatusesForAllParties() {
        String ref = "1234567890123456";
        String docUuid = "uuid-123";

        List<Party> parties = List.of(
            Party.SUBJECT,
            Party.APPLICANT,
            Party.REPRESENTATIVE,
            Party.RESPONDENT
        );

        DocumentEntity docEntity = DocumentEntity.builder()
            .id(100L)
            .build();

        when(documentsRepository.findByDocumentIdUuid(docUuid)).thenReturn(Optional.of(docEntity));

        for (Party party : parties) {
            when(documentDownloadStatusesRepository.findByDocumentIdAndParty(100L, party)).thenReturn(Optional.empty());

            documentDownloadStatusService.recordDocumentDownload(ref, party, docUuid);

            verify(documentDownloadStatusesRepository, times(1)).save(org.mockito.ArgumentMatchers.argThat(status ->
                status.getDocumentId() == 100L && status.getParty() == party
            ));
        }
    }

    @Test
    public void shouldDeleteDocumentDownloadStatusesForCaseAndParty() {
        Long caseReferenceNumber = 1234567890123456L;
        Party party = Party.REPRESENTATIVE;

        documentDownloadStatusService.deleteDocumentDownloadStatusesForCaseAndParty(caseReferenceNumber, party);

        verify(documentDownloadStatusesRepository, times(1))
            .deleteByCaseReferenceNumberAndParty(caseReferenceNumber, party);
    }

    @Test
    public void shouldNotDeleteIfPartyOrCaseReferenceNumberIsNull() {
        documentDownloadStatusService.deleteDocumentDownloadStatusesForCaseAndParty(null, Party.REPRESENTATIVE);
        documentDownloadStatusService.deleteDocumentDownloadStatusesForCaseAndParty(1234567890123456L, null);

        verify(documentDownloadStatusesRepository, times(0))
            .deleteByCaseReferenceNumberAndParty(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
