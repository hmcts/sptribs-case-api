package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.notification.persistence.CorrespondenceEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CorrespondenceRepository extends JpaRepository<CorrespondenceEntity, UUID> {
    List<CorrespondenceEntity> findAllByCaseReferenceNumberOrderBySentOnDesc(Long caseReferenceNumber);
}

