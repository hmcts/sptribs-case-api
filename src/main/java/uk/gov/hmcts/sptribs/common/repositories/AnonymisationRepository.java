package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.ciccase.persistence.AnonymityEntity;

@Repository
public interface AnonymisationRepository extends JpaRepository<AnonymityEntity, Long> {
    
}
