package uk.gov.hmcts.sptribs.systemupdate.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for querying the case_event table.
 * TODO: Relocate this class to a more appropriate package
 * once the cleanup/refactoring is complete.
 */
@Repository
@Slf4j
public class CaseEventRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String SELECT_LIST_OF_CASE_IDS_BY_EVENT_TYPE_AND_DATE = "SELECT DISTINCT REFERENCE FROM ccd.case_data cd where "
        + "cd.id in (SELECT DISTINCT case_data_id from ccd.case_event WHERE event_id = :caseEventId AND created_date >= :createdDate) "
        + "AND jsonb_array_length(cd.data -> 'furtherUploadedDocuments') > 0";

    @Autowired
    public CaseEventRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<Long> getListOfCasesByEventTypeAndDate(String caseEventId, LocalDate createdDate) {

        List<Long> results;
        var params = Map.of(
            "caseEventId", caseEventId,
            "createdDate", createdDate
        );

        try {
            results = namedParameterJdbcTemplate.query(SELECT_LIST_OF_CASE_IDS_BY_EVENT_TYPE_AND_DATE,
                params,
                (rs, rowNum) -> rs.getLong("reference")
            );

        } catch (DataAccessException dataAccessException) {
            throw new RuntimeException("Error whilst retrieving case events to clean further documents",
                dataAccessException);
        }


        if (results.isEmpty()) {
            log.info("No results found for caseEventId = {}", caseEventId);
            return results;
        }

        return results;

    }
}
