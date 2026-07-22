package uk.gov.hmcts.sptribs.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

            Optional<DocumentDownloadStatusEntity> existingStatusOpt =
                documentDownloadStatusesRepository.findByDocumentIdAndParty(docIdLong, party);

            if (existingStatusOpt.isPresent()) {
                DocumentDownloadStatusEntity status = existingStatusOpt.get();
                status.setDownloadedAt(OffsetDateTime.now());
                documentDownloadStatusesRepository.save(status);
                log.info("Updated download status for document: {}, party: {}", documentId, party);
            } else {
                DocumentDownloadStatusEntity status = DocumentDownloadStatusEntity.builder()
                    .caseReferenceNumber(Long.valueOf(ccdReference))
                    .documentId(docIdLong)
                    .party(party)
                    .downloadedAt(OffsetDateTime.now())
                    .build();
                documentDownloadStatusesRepository.save(status);
                log.info("Recorded new download status for document: {}, party: {}", documentId, party);
            }
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
                documentDownloadStatusesRepository.findAllByCaseReferenceNumberAndParty(Long.valueOf(ccdReference), party);

            return statuses.stream()
                .map(DocumentDownloadStatusEntity::getDocumentId)
                .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Error fetching downloaded document IDs for case {}", ccdReference, e);
            return Set.of();
        }
    }

    @Transactional
    public void deleteDocumentDownloadStatusesForCaseAndParty(Long caseReferenceNumber, Party party) {
        try {
            if (party == null || caseReferenceNumber == null) {
                return;
            }
            documentDownloadStatusesRepository.deleteByCaseReferenceNumberAndParty(caseReferenceNumber, party);
            log.info("Deleted document download statuses for case reference number {} and party {}", caseReferenceNumber, party);
        } catch (Exception e) {
            log.error("Error deleting document download statuses for case reference number {} and party {}", caseReferenceNumber, party, e);
        }
    }
}
