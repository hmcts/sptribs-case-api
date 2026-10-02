package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.common.repositories.model.CorrespondenceDocumentEntity;
import uk.gov.hmcts.sptribs.common.repositories.model.CorrespondenceDocumentId;

@Repository
public interface CorrespondenceDocumentRepository extends JpaRepository<CorrespondenceDocumentEntity, CorrespondenceDocumentId> {

}
