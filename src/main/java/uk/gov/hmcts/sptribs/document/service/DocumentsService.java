package uk.gov.hmcts.sptribs.document.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.document.model.DocumentDashboardModel;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentsService {

    private final DocumentsRepository documentsRepository;

    @Autowired
    public DocumentsService(DocumentsRepository documentsRepository) {
        this.documentsRepository = documentsRepository;
    }

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
        //probs exclude draft in query, could use query for more control also 
        List<DocumentEntity> allDocumentsOnCase = documentsRepository.findByCaseReferenceNumberOrderBySavedAtDesc(ccdReference);

        List<DocumentEntity> applicantDocuments = new ArrayList<>();
        List<DocumentEntity> lastestCaseBundle = new ArrayList<>();
        List<DocumentEntity> orderAndDecisionDocuments = new ArrayList<>();


        for (DocumentEntity document : allDocumentsOnCase) {
            //my logic
            //check for
            document.getCategoryId()
        }

        return DocumentDashboardModel.builder()
            .contactPartiesDocuments(applicantDocuments)
            .latestCaseBundleDocuments(lastestCaseBundle)
            .orderAndDecisionDocuments(orderAndDecisionDocuments)
            .build();
    }

}
