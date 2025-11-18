package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.ciccase.persistence.AnonymisationEntity;


@Repository
public interface AnonymisationRepository extends JpaRepository<AnonymisationEntity, Long> {

    @Query(value = "SELECT nextval('anonymisation_global_seq')", nativeQuery = true)
    Long getNextSequenceValue();

}
