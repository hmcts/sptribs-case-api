package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.document.persistence.DocumentEntity;

import java.util.List;

@Repository
public interface DocumentsRepository extends JpaRepository<DocumentEntity, Integer> {
    List<DocumentEntity> findAllByCaseReferenceNumberOrderBySavedAtDesc(Long caseReferenceNumber);

    List<DocumentEntity> findAllByDocumentBinaryUrl(String documentBinaryUrl);
}
