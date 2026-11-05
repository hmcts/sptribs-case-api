package uk.gov.hmcts.sptribs.systemupdate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_JURISDICTION;

@Repository
@RequiredArgsConstructor
public class FirstHearingDateMigrationRepository {

    private static final String CASE_TYPE = "caseType";
    private static final String JURISDICTION = "jurisdiction";
    private static final String REFERENCE = "reference";
    private static final String CASE_FILTER = "c.case_type_id = :caseType AND c.jurisdiction = :jurisdiction "
        + "AND jsonb_exists(c.data, 'firstHearingDate')";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<Long> findNextBatch(long afterReference, int batchSize) {
        return jdbcTemplate.query(
            "SELECT c.reference FROM ccd.case_data c WHERE " + CASE_FILTER
                + " AND c.reference > :afterReference ORDER BY c.reference LIMIT :batchSize",
            Map.of(CASE_TYPE, ST_CIC_CASE_TYPE, JURISDICTION, ST_CIC_JURISDICTION,
                "afterReference", afterReference, "batchSize", batchSize),
            (resultSet, rowNumber) -> resultSet.getLong(REFERENCE));
    }

    public boolean hasFirstHearingDate(long reference) {
        final Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ccd.case_data c WHERE " + CASE_FILTER + " AND c.reference = :reference",
            Map.of(CASE_TYPE, ST_CIC_CASE_TYPE, JURISDICTION, ST_CIC_JURISDICTION,
                REFERENCE, reference), Long.class);
        return count != null && count > 0;
    }

    /** Called only inside a SystemEventExecutor action, after it locks the case. */
    public boolean removeFirstHearingDate(long reference) {
        // The executor saves the event using the version it read before this update.
        return jdbcTemplate.update(
            "UPDATE ccd.case_data c SET data = c.data - 'firstHearingDate' "
                + "WHERE " + CASE_FILTER + " AND c.reference = :reference",
            Map.of(CASE_TYPE, ST_CIC_CASE_TYPE, JURISDICTION, ST_CIC_JURISDICTION,
                REFERENCE, reference)) == 1;
    }

    public long countRemaining() {
        final Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ccd.case_data c WHERE " + CASE_FILTER,
            Map.of(CASE_TYPE, ST_CIC_CASE_TYPE, JURISDICTION, ST_CIC_JURISDICTION), Long.class);
        return count == null ? 0 : count;
    }
}
