package uk.gov.hmcts.sptribs.document.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;

@Service
public class DocumentsService {

    private final DocumentsRepository documentsRepository;

    @Autowired
    public DocumentsService(DocumentsRepository documentsRepository) {
        this.documentsRepository = documentsRepository;
    }

    @Transactional
    public void buildAndSaveNewDocumentEntity(Document document, Long caseReferenceNumber, boolean isDraft) {
        try {
            if (documentsRepository.findAllByDocumentBinaryUrl(document.getBinaryUrl()).isEmpty()) {
                documentsRepository.save(DocumentEntity.builder()
                    .caseReferenceNumber(caseReferenceNumber)
                    .documentUrl(document.getUrl())
                    .documentFilename(document.getFilename())
                    .documentBinaryUrl(document.getBinaryUrl())
                    .categoryId(document.getCategoryId())
                    .isDraft(isDraft)
                    .sentToApplicantViaContactParties(false)
                    .build());
            }
        } catch (DataAccessException e) {
            throw new RuntimeException("Error saving document entity to database", e);
        }
    }

    @Transactional
    public void setSentToApplicantViaContactPartiesToTrue(String documentBinaryUrl) {
        try {
            DocumentEntity documentEntity = documentsRepository.findAllByDocumentBinaryUrl(documentBinaryUrl).getFirst();
            documentEntity.setSentToApplicantViaContactParties(true);
            documentsRepository.save(documentEntity);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error saving document entity to database", e);
        }
    }
}
