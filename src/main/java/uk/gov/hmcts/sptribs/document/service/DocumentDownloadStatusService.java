package uk.gov.hmcts.sptribs.document.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.util.CasePartyUtil;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.common.repositories.DocumentDownloadStatusesRepository;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentDownloadStatusEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.idam.IdamService;
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

    private final IdamService idamService;
    private final CaseDataRepository caseDataRepository;
    private final ObjectMapper objectMapper;
    private final DocumentsRepository documentsRepository;
    private final DocumentDownloadStatusesRepository documentDownloadStatusesRepository;

    public void recordDocumentDownload(String authorisation, String ccdReference, String postcode, String documentId) {
        try {
            User user = idamService.retrieveUser(authorisation);
            String userEmail = user.getUserDetails().getEmail();

            Optional<CicaCaseEntity> cicaCaseOpt = caseDataRepository.findCase(ccdReference, userEmail, postcode);
            if (cicaCaseOpt.isEmpty()) {
                log.warn("Could not find authorized case to record download: case = {}, email = {}", ccdReference, userEmail);
                return;
            }

            CicaCaseEntity cicaCase = cicaCaseOpt.get();
            CaseData caseData = objectMapper.convertValue(cicaCase.getData(), CaseData.class);
            Party party = CasePartyUtil.determineParty(caseData, userEmail);

            if (party == null) {
                log.warn("User email {} does not match any registered party on case {}", userEmail, ccdReference);
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
}
