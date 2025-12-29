package uk.gov.hmcts.sptribs.common.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AnonymisationRepositoryImpl implements  AnonymisationRepository {

    private static final String ANONYMISATION_GLOBAL_SEQ = "anonymisation_global_seq";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long getNextSequenceValue() {
        return jdbcTemplate.queryForObject(
                "SELECT nextval(?)",
                Long.class,
                ANONYMISATION_GLOBAL_SEQ);
    }
}
