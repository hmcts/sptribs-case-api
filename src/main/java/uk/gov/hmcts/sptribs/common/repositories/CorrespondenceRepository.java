package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.notification.persistence.CorrespondenceEntity;
import uk.gov.hmcts.sptribs.notification.persistence.CorrespondenceEntityId;

import java.util.List;

@Repository
public interface CorrespondenceRepository extends JpaRepository<CorrespondenceEntity, CorrespondenceEntityId> {
    List<CorrespondenceEntity> findAllByCaseReferenceNumberOrderBySentOnDesc(Long caseReferenceNumber);
}

