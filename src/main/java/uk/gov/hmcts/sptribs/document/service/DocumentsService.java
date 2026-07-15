package uk.gov.hmcts.sptribs.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentDeleteException;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentLookupException;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentSaveException;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentUpdateException;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.ContactPartyDocumentDetails;
import uk.gov.hmcts.sptribs.document.model.DocumentDashboardModel;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil.getAllCaseDocuments;

@RequiredArgsConstructor
@Service
@Slf4j
public class DocumentsService {

    private final DocumentsRepository documentsRepository;
    private final CaseDocumentTypesCache caseDocumentTypesCache;

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
                .build());

        } catch (DataAccessException e) {
            throw new DocumentSaveException("Error saving document entity to database", e);
        }
    }

    public List<Long> getDocumentsViaSentByContactParties(CaseData caseData, final Map<String, String> uploadedDocuments) {

        List<ListValue<CaseworkerCICDocument>> allCaseDocuments =  getAllCaseDocuments(caseData);
        Set<String> uploadedDocumentIds = new HashSet<>(uploadedDocuments.values());

        List<String> binaryUrls = new ArrayList<>();

        for (ListValue<CaseworkerCICDocument> listValue : allCaseDocuments) {
            CaseworkerCICDocument document = listValue.getValue();

            if (uploadedDocumentIds.contains(getDocumentId(document))) {
                binaryUrls.add(getBinaryUrl(document));
            }
        }

        return getDocumentIdsByDocumentBinaryUrls(binaryUrls);
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

    private List<Long> getDocumentIdsByDocumentBinaryUrls(List<String> documentBinaryUrls) {
        try {
            List<Long> documentIds = documentsRepository.findIdsByDocumentBinaryUrls(documentBinaryUrls);
            log.info("Document Repository found the following documentIds {}.", documentIds);
            return documentIds;
        } catch (DataAccessException e) {
            throw new DocumentLookupException("Error getting document id's by documentBinaryUrls", e);
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

        final List<Long> orderAndDecisionTypeIds = Stream.of(
                CaseDocumentType.ORDER,
                CaseDocumentType.DECISION,
                CaseDocumentType.FINAL_DECISION
            )
            .map(caseDocumentTypesCache::getId)
            .toList();

        final List<Party> contactParties = List.of(
            Party.APPLICANT,
            Party.REPRESENTATIVE,
            Party.SUBJECT
        );

        // Documents that have been sent to contact parties, excluding order/decision docs with date from when email sent.
        List<ContactPartyDocumentDetails> contactPartyDocuments =
            documentsRepository.findContactPartyDocuments(
                ccdReference,
                contactParties,
                orderAndDecisionTypeIds
            );

        // Latest generated case bundle, if one exists.
        Optional<DocumentEntity> latestBundle =
            documentsRepository.findFirstByCaseReferenceNumberAndCaseDocumentTypeIdOrderBySavedAtDesc(
                ccdReference,
                caseDocumentTypesCache.getId(CaseDocumentType.BUNDLE)
            );

        // Order and decision documents displayed on the dashboard.
        List<DocumentEntity> orderDecisionDocuments =
            documentsRepository.findOrderAndDecisionDocuments(
                ccdReference,
                orderAndDecisionTypeIds
            );

        return DocumentDashboardModel.builder()
            .contactPartiesDocuments(contactPartyDocuments)
            .latestCaseBundleDocument(latestBundle.orElse(null))
            .orderAndDecisionDocuments(orderDecisionDocuments)
            .build();
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
}
