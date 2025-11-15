package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.ciccase.persistence.AnonymisationEntity;

import java.util.Optional;

@Repository
public interface AnonymisationRepository extends JpaRepository<AnonymisationEntity, Long> {

    Optional<AnonymisationEntity> findByCaseReference(Long caseReference);

}
