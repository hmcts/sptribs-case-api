package uk.gov.hmcts.sptribs.document.service;

import lombok.RequiredArgsConstructor;
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
import static uk.gov.hmcts.sptribs.document.model.DocumentType.fromCategory;

@RequiredArgsConstructor
@Service
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
        return StringUtils.substringAfterLast(
            document.getDocumentLink().getUrl(),
            "/"
        );
    }

    private String getBinaryUrl(CaseworkerCICDocument document) {
        return document.getDocumentLink()
            .getBinaryUrl();
    }

    public void setSentToApplicantViaContactPartiesToTrue(List<String> documentBinaryUrls) {
        try {
            documentsRepository.setSentToApplicantViaContactPartiesToTrueByDocumentBinaryUrl(documentBinaryUrls);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error updating sent_to_applicant_via_contact_parties to true", e);
        }
    }

    public void setIsDraftToFalse(String documentBinaryUrl) {
        try {
            documentsRepository.setIsDraftToFalseByDocumentBinaryUrl(documentBinaryUrl);
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

            if (doc.isSentToApplicantViaContactParties()) {
                contactPartiesDocuments.add(doc);
            }

            if (tribunalDocumentTypeId.equals(doc.getDocumentTypeId())) {
                orderAndDecisionDocuments.add(doc);
            } else if (bundleDocumentTypeId.equals(doc.getDocumentTypeId())) {
                bundleDocuments.add(doc);
            }
        }

        OffsetDateTime latestBundleDate = bundleDocuments.stream()
            .map(DocumentEntity::getSavedAt)
            .max(OffsetDateTime::compareTo)
            .orElse(null);

        List<DocumentEntity> latestCaseBundleDocuments =
            latestBundleDate == null
                ? List.of()
                : bundleDocuments.stream()
                .filter(doc -> latestBundleDate.equals(doc.getSavedAt()))
                .toList();

        return DocumentDashboardModel.builder()
            .contactPartiesDocuments(contactPartiesDocuments)
            .latestCaseBundleDocuments(latestCaseBundleDocuments)
            .orderAndDecisionDocuments(orderAndDecisionDocuments)
            .build();
    }

}
