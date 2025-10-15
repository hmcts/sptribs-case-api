package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.notification.persistence.CorrespondenceRecord;

import java.util.List;

@Repository
public interface CorrespondenceRepository extends JpaRepository<CorrespondenceRecord, Long> {
    List<CorrespondenceRecord> findAllByCaseReferenceNumberOrderBySentAtDesc(Long caseReferenceNumber);
}
