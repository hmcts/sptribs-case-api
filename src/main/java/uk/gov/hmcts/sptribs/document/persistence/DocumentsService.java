package uk.gov.hmcts.sptribs.document.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepositoryJPA;

@Service
public class DocumentsService {

    private final DocumentsRepositoryJPA documentsRepositoryJPA;

    @Autowired
    public DocumentsService(DocumentsRepositoryJPA documentsRepositoryJPA) {
        this.documentsRepositoryJPA = documentsRepositoryJPA;
    }

    public void buildAndSaveNewDocumentEntity(Document document, Long caseReferenceNumber, boolean isDraft) {
        try {
                documentsRepositoryJPA.save(DocumentEntity.builder()
                    .caseReferenceNumber(caseReferenceNumber)
                    .documentUrl(document.getUrl())
                    .documentFilename(document.getFilename())
                    .documentBinaryUrl(document.getBinaryUrl())
                    .categoryId(document.getCategoryId())
                    .isDraft(isDraft)
                    .sentToApplicantViaContactParties(false)
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Error saving document entity to database", e);
        }
    }

    @Transactional
    public void setSentToApplicantViaContactPartiesToTrue(String documentBinaryUrl) {
        try {
            DocumentEntity documentEntity = documentsRepositoryJPA.findAllByDocumentBinaryUrl(documentBinaryUrl).getFirst();
            documentEntity.setSentToApplicantViaContactParties(true);
            documentsRepositoryJPA.save(documentEntity);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error saving document entity to database", e);
        }
    }

    public void getDocumentsOnCase(Long ccdReference) {
        //get all docs from db,
        //sort into the 3 different sections
        //return serivce layer object that gets converetd to the documentresponse...


        //so we need three parts:
        //
        // docs sent to applicant via contact parties !!! use the db column
        // we have draft column to exclude draft docs from some events
        //
        //
        //
        //How to get lastest case bundles????
        //
        // order and decisions????
        //
    }
}
