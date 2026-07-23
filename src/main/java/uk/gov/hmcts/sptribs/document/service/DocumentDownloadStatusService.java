package uk.gov.hmcts.sptribs.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.common.repositories.DocumentDownloadStatusesRepository;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.document.model.DocumentDownloadStatusEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class DocumentDownloadStatusService {

    private final DocumentsRepository documentsRepository;
    private final DocumentDownloadStatusesRepository documentDownloadStatusesRepository;

    public void recordDocumentDownload(String ccdReference, Party party, String documentId) {
        try {
            if (party == null) {
                log.warn("Party is null, cannot record download: case = {}, document = {}", ccdReference, documentId);
                return;
            }

            Optional<DocumentEntity> docEntityOpt = documentsRepository.findByDocumentIdUuid(documentId);
            if (docEntityOpt.isEmpty()) {
                log.warn("Could not find local DocumentEntity for CDAM document UUID: {}", documentId);
                return;
            }

            DocumentEntity docEntity = docEntityOpt.get();
            long docIdLong = docEntity.getId();

            saveOrUpdateStatus(Long.valueOf(ccdReference), docIdLong, party);
        } catch (Exception e) {
            log.error("Error recording document download status for document: {}, case: {}", documentId, ccdReference, e);
        }
    }

    public Set<Long> getDownloadedDocumentIds(String ccdReference, Party party) {
        try {
            if (party == null) {
                return Set.of();
            }

            List<DocumentDownloadStatusEntity> statuses =
                findDownloadStatusesByCaseAndParty(Long.valueOf(ccdReference), party);

            return statuses.stream()
                .map(DocumentDownloadStatusEntity::getDocumentId)
                .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Error fetching downloaded document IDs for case {}", ccdReference, e);
            return Set.of();
        }
    }

    public Optional<DocumentDownloadStatusEntity> findDownloadStatusByDocumentIdAndParty(long documentId, Party party) {
        return documentDownloadStatusesRepository.findByDocumentIdAndParty(documentId, party);
    }

    public List<DocumentDownloadStatusEntity> findDownloadStatusesByCaseAndParty(Long caseReferenceNumber, Party party) {
        return documentDownloadStatusesRepository.findAllByCaseReferenceNumberAndParty(caseReferenceNumber, party);
    }

    public DocumentDownloadStatusEntity saveOrUpdateStatus(Long caseReferenceNumber, long documentId, Party party) {
        Optional<DocumentDownloadStatusEntity> existingStatusOpt =
            findDownloadStatusByDocumentIdAndParty(documentId, party);

        DocumentDownloadStatusEntity status;
        if (existingStatusOpt.isPresent()) {
            status = existingStatusOpt.get();
            status.setDownloadedAt(OffsetDateTime.now());
            log.info("Updated download status for document: {}, party: {}", documentId, party);
        } else {
            status = DocumentDownloadStatusEntity.builder()
                .caseReferenceNumber(caseReferenceNumber)
                .documentId(documentId)
                .party(party)
                .downloadedAt(OffsetDateTime.now())
                .build();
            log.info("Recorded new download status for document: {}, party: {}", documentId, party);
        }
        return documentDownloadStatusesRepository.save(status);
    }
}
