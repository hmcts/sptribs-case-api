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
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentDashboardModel;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil.getAllCaseDocuments;

@RequiredArgsConstructor
@Service
@Slf4j
public class DocumentsService {

    private final DocumentsRepository documentsRepository;
    private final CaseDocumentTypesCache caseDocumentTypesCache;

    public void buildAndSaveNewDocumentEntity(Document document, Long caseReferenceNumber, boolean isDraft, boolean isStitchedDocument) {
        try {

            CaseDocumentType caseDocumentType = getCaseDocumentType(document.getCategoryId(), isStitchedDocument);

            documentsRepository.save(DocumentEntity.builder()
                .caseReferenceNumber(caseReferenceNumber)
                .documentUrl(document.getUrl())
                .documentFilename(document.getFilename())
                .documentBinaryUrl(document.getBinaryUrl())
                .categoryId(document.getCategoryId())
                .documentTypeId(caseDocumentTypesCache.getId(caseDocumentType))
                .isDraft(isDraft)
                .sentToApplicantViaContactParties(false)
                .build());

        } catch (DataAccessException e) {
            throw new RuntimeException("Error saving document entity to database", e);
        }
    }

    private CaseDocumentType getCaseDocumentType(String categoryId, boolean isStitchedDocument) {

        if (isStitchedDocument) {
            return CaseDocumentType.BUNDLE;
        } else {
            return DocumentType.fromCategory(categoryId).getCaseDocumentType();
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
            throw new RuntimeException("Error updating sent_to_applicant_via_contact_parties to true", e);
        }
    }

    @Transactional
    public void setIsDraftToFalse(String documentBinaryUrl) {
        try {
            documentsRepository.setIsDraftToFalseByDocumentBinaryUrl(documentBinaryUrl);
            log.info("Draft order updated to non draft successfully for url: {}", documentBinaryUrl);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error updating is_draft to false", e);
        }
    }

    public DocumentDashboardModel getDocumentsOnCase(Long ccdReference) {

        //using 1 query then code rather than many queries
        List<DocumentEntity> allDocumentsOnCase =
            documentsRepository.findAllNonDraftDocumentsByCaseReference(ccdReference);

        List<DocumentEntity> contactPartiesDocuments = new ArrayList<>();
        List<DocumentEntity> orderAndDecisionDocuments = new ArrayList<>();
        List<DocumentEntity> bundleDocuments = new ArrayList<>();

        Long tribunalDocumentTypeId =
            caseDocumentTypesCache.getId(CaseDocumentType.TRIBUNAL_DOCUMENT);

        Long bundleDocumentTypeId =
            caseDocumentTypesCache.getId(CaseDocumentType.BUNDLE);

        for (DocumentEntity doc : allDocumentsOnCase) {
            //if doc order and sent out via contact parties it will appear in orders .
            if (tribunalDocumentTypeId.equals(doc.getDocumentTypeId())) {
                orderAndDecisionDocuments.add(doc);
            } else if (bundleDocumentTypeId.equals(doc.getDocumentTypeId())) {
                bundleDocuments.add(doc);
            } else if (doc.isSentToApplicantViaContactParties()) {
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

    private DocumentEntity getDocumentEntityDocs(long caseRef, String value) {
        return documentsRepository.findDocumentByCaseReferenceAndBinaryUrl(caseRef, value);
    }

    public void removeEntryFromDocumentTable(long caseRef, String value) {
        documentsRepository.delete(getDocumentEntityDocs(caseRef, value));
    }
}
