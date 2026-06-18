package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;

@Repository
public interface CorrespondenceDocumentRepository extends JpaRepository<DocumentEntity, Integer> {

}
