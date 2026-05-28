package uk.gov.hmcts.sptribs.document.services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;

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

    public void setSentToApplicantViaContactPartiesToTrue(String documentBinaryUrl) {
        try {
            documentsRepository.setSentToApplicantViaContactPartiesToTrueByDocumentBinaryUrl(documentBinaryUrl);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error updating sent_to_applicant_via_contact_parties to true", e);
        }
    }
}
