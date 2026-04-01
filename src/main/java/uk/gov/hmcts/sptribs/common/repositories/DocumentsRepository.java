package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.document.persistence.DocumentEntity;
import uk.gov.hmcts.sptribs.document.persistence.DocumentEntityId;

import java.util.List;

@Repository
public interface DocumentsRepository extends JpaRepository<DocumentEntity, DocumentEntityId> {
    List<DocumentEntity> findAllByCaseReferenceNumberOrderBySavedAtDesc(Long caseReferenceNumber);

    List<DocumentEntity> findAllByDocumentBinaryUrl(String documentBinaryUrl);
}
