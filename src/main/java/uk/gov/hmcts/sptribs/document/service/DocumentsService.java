package uk.gov.hmcts.sptribs.document.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.util.CasePartyUtil;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.common.repositories.DocumentDownloadStatusesRepository;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentDeleteException;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentSaveException;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentUpdateException;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentDashboardModel;
import uk.gov.hmcts.sptribs.document.model.DocumentDownloadStatusEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil.getAllCaseDocuments;

@RequiredArgsConstructor
@Service
@Slf4j
public class DocumentsService {

    private final DocumentsRepository documentsRepository;
    private final CaseDocumentTypesCache caseDocumentTypesCache;
    private final IdamService idamService;
    private final CaseDataRepository caseDataRepository;
    private final ObjectMapper objectMapper;
    private final DocumentDownloadStatusesRepository documentDownloadStatusesRepository;

    public void buildAndSaveNewDocumentEntity(Document document, Long caseReferenceNumber,
                                              DocumentType documentType, CaseDocumentType caseDocumentType) {
        try {

            documentsRepository.save(DocumentEntity.builder()
                .caseReferenceNumber(caseReferenceNumber)
                .documentUrl(document.getUrl())
                .documentFilename(document.getFilename())
                .documentBinaryUrl(document.getBinaryUrl())
                .documentTypeName(documentType != null ? documentType.name() : null)
                .caseDocumentTypeId(caseDocumentTypesCache.getId(caseDocumentType))
                .sentToApplicantViaContactParties(false)
                .build());

        } catch (DataAccessException e) {
            throw new DocumentSaveException("Error saving document entity to database", e);
        }
    }

    @Transactional
    public void updateDocumentsToSentViaContactParties(CaseData caseData, final Map<String, String> uploadedDocuments) {

        List<ListValue<CaseworkerCICDocument>> allCaseDocuments =  getAllCaseDocuments(caseData);
        Set<String> uploadedDocumentIds = new HashSet<>(uploadedDocuments.values());

        List<String> binaryUrls = new ArrayList<>();

        for (ListValue<CaseworkerCICDocument> listValue : allCaseDocuments) {
            CaseworkerCICDocument document = listValue.getValue();

            if (uploadedDocumentIds.contains(getDocumentId(document))) {
                binaryUrls.add(getBinaryUrl(document));
            }
        }

        setSentToApplicantViaContactPartiesToTrue(binaryUrls);
    }

    private String getDocumentId(CaseworkerCICDocument document) {
        String binaryUrl = document.getDocumentLink().getBinaryUrl();

        return StringUtils.substringAfterLast(
            binaryUrl.replaceFirst("/binary$", ""),
            "/"
        );
    }

    private String getBinaryUrl(CaseworkerCICDocument document) {
        return document.getDocumentLink()
            .getBinaryUrl();
    }

    public void setSentToApplicantViaContactPartiesToTrue(List<String> documentBinaryUrls) {
        try {
            int rowsUpdated =
                documentsRepository.setSentToApplicantViaContactPartiesToTrueByDocumentBinaryUrl(documentBinaryUrls);
            log.info("Document Repository updated {} documents to sent via contact parties.", rowsUpdated);
        } catch (DataAccessException e) {
            throw new DocumentUpdateException("Error updating sent_to_applicant_via_contact_parties to true", e);
        }
    }

    @Transactional
    public void setNewDocumentTypeName(String documentBinaryUrl, String documentTypeName) {
        try {
            int rowsUpdated =
                documentsRepository.setDocumentTypeNameByDocumentBinaryUrl(
                    documentBinaryUrl,
                    documentTypeName
                );
            log.info("Document Repository updated {} document type names.", rowsUpdated);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error updating document type name", e);
        }
    }

    @Transactional
    public void updateDocumentToNonDraft(String documentBinaryUrl) {

        try {
            Long orderDocumentTypeId = caseDocumentTypesCache.getId(CaseDocumentType.ORDER);
            documentsRepository.updateCaseDocumentTypeIdByDocumentBinaryUrl(documentBinaryUrl, orderDocumentTypeId);
            log.info("Draft order updated to non draft case document type successfully for url: {}", documentBinaryUrl);

        } catch (DataAccessException e) {
            throw new DocumentUpdateException("Error updating case document type from draft order to order", e);
        }
    }

    public DocumentDashboardModel getDocumentsOnCase(Long ccdReference) {

        //using 1 query then code rather than many queries
        List<DocumentEntity> allDocumentsOnCase =
            documentsRepository.findAllDocumentsByCaseReference(ccdReference);

        List<DocumentEntity> contactPartiesDocuments = new ArrayList<>();
        List<DocumentEntity> orderAndDecisionDocuments = new ArrayList<>();
        List<DocumentEntity> bundleDocuments = new ArrayList<>();


        //need to double check and maybe update the filters here
        // to reflect new case doc types, will do this later
        Long tribunalDocumentTypeId =
            caseDocumentTypesCache.getId(CaseDocumentType.ORDER);

        Long bundleDocumentTypeId =
            caseDocumentTypesCache.getId(CaseDocumentType.BUNDLE);

        for (DocumentEntity doc : allDocumentsOnCase) {
            //if doc order and sent out via contact parties it will appear in orders .
            if (tribunalDocumentTypeId.equals(doc.getCaseDocumentTypeId())) {
                orderAndDecisionDocuments.add(doc);
            } else if (bundleDocumentTypeId.equals(doc.getCaseDocumentTypeId())) {
                bundleDocuments.add(doc);
            } else if (doc.isSentToApplicantViaContactParties()) {
                //this will change with new work mapping correspondence to docs
                contactPartiesDocuments.add(doc);
            }
        }

        return DocumentDashboardModel.builder()
            .contactPartiesDocuments(contactPartiesDocuments)
            .latestCaseBundleDocument(getLatestBundleDocument(bundleDocuments))
            .orderAndDecisionDocuments(orderAndDecisionDocuments)
            .build();
    }

    private DocumentEntity getLatestBundleDocument(List<DocumentEntity> bundleDocuments) {
        OffsetDateTime latestBundleDate = bundleDocuments.stream()
            .map(DocumentEntity::getSavedAt)
            .max(OffsetDateTime::compareTo)
            .orElse(null);

        return bundleDocuments.stream()
            .filter(doc -> latestBundleDate.equals(doc.getSavedAt()))
            .findFirst()
            .orElse(null);
    }

    @Transactional
    public void removeEntryFromDocumentTableByBinaryURL(String value) {
        try {
            documentsRepository.deleteEntryByBinaryURL(value);
        } catch (DataAccessException e) {
            throw new DocumentDeleteException("Error deleting entry from document table. "
                + "BinaryUrl: " + value, e);
        }
    }

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

    public Set<Long> getDownloadedDocumentIds(String authorisation, String ccdReference, String postcode) {
        try {
            User user = idamService.retrieveUser(authorisation);
            String userEmail = user.getUserDetails().getEmail();

            Optional<CicaCaseEntity> cicaCaseOpt = caseDataRepository.findCase(ccdReference, userEmail, postcode);
            if (cicaCaseOpt.isEmpty()) {
                return Set.of();
            }

            CicaCaseEntity cicaCase = cicaCaseOpt.get();
            CaseData caseData = objectMapper.convertValue(cicaCase.getData(), CaseData.class);
            Party party = CasePartyUtil.determineParty(caseData, userEmail);

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
