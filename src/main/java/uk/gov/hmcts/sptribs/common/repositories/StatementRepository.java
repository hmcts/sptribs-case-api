package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.notification.persistence.CorrespondenceEntityId;
import uk.gov.hmcts.sptribs.notification.persistence.StatementsEntity;

import java.util.List;

@Repository
public interface StatementRepository extends JpaRepository<StatementsEntity, CorrespondenceEntityId> {
    List<StatementsEntity> findAllByCaseReferenceNumberOrderByCreatedOnDesc(Long caseReferenceNumber);
}
