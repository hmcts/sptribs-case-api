package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.statement.persistence.StatementEntity;

import java.util.List;

@Repository
public interface StatementRepository extends JpaRepository<StatementEntity, Long> {
    List<StatementEntity> findAllByCaseReferenceNumberOrderByUploadedOnDesc(Long caseReferenceNumber);
}
