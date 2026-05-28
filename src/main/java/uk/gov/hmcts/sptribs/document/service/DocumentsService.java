package uk.gov.hmcts.sptribs.document.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.document.model.DocumentDashboardModel;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DocumentsService {

    private final DocumentsRepository documentsRepository;

    public void buildAndSaveNewDocumentEntity(Document document, Long caseReferenceNumber, boolean isDraft) {
        try {
            documentsRepository.save(DocumentEntity.builder()
                .caseReferenceNumber(caseReferenceNumber)
                .documentUrl(document.getUrl())
                .documentFilename(document.getFilename())
                .documentBinaryUrl(document.getBinaryUrl())
                .categoryId(document.getCategoryId())
                .isDraft(isDraft)
                .sentToApplicantViaContactParties(false)
                .build());
        } catch (DataAccessException e) {
            throw new RuntimeException("Error saving document entity to database", e);
        }
    }

    @Transactional
    public void setSentToApplicantViaContactPartiesToTrue(String documentBinaryUrl) {
        try {
            documentsRepository.setSentToApplicantViaContactPartiesToTrueByDocumentBinaryUrl(documentBinaryUrl);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error updating sent_to_applicant_via_contact_parties to true", e);
        }
    }

    public DocumentDashboardModel getDocumentsOnCase(Long ccdReference) {

        //using 1 query then code rather than many queries
        List<DocumentEntity> allDocumentsOnCase =
            documentsRepository.findAllNonDraftDocumentsByCaseReference(ccdReference);

        List<DocumentEntity> contactPartiesDocuments = new ArrayList<>();
        List<DocumentEntity> orderAndDecisionDocuments = new ArrayList<>();
        List<DocumentEntity> bundleDocuments = new ArrayList<>();

        for (DocumentEntity doc : allDocumentsOnCase) {

            if (doc.isSentToApplicantViaContactParties()) {
                contactPartiesDocuments.add(doc);
            }

            if (DocumentType.TRIBUNAL_DIRECTION.getCategory().equals(doc.getCategoryId())) {
                orderAndDecisionDocuments.add(doc);
            }

            if (DocumentType.BUNDLE.getCategory().equals(doc.getCategoryId())) {
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
