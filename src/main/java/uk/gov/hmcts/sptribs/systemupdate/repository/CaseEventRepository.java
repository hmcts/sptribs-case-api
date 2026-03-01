package uk.gov.hmcts.sptribs.systemupdate.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Class to query the case event table
 * we should move to more appropriate place after this cleanup iis done
 *
 */
@Repository
@Slf4j
public class CaseEventRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String SELECT_LIST_OF_CASE_IDS_BY_EVENT_TYPE_AND_DATE = "SELECT DISTINCT case_data_id from ccd.case_event " +
        "WHERE event_id = :caseEventId AND created_date >= :createdDate AND jsonb_exists(data::jsonb, 'furtherUploadedDocuments')";

    @Autowired
    public CaseEventRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<Long> getListOfCasesByEventTypeAndDate(String caseEventId, String createdDate) {

        List<Long> results;
        var params = Map.of(
            "caseEventId", caseEventId,
            "createdDate", createdDate
        );

        try {
            results = namedParameterJdbcTemplate.query(SELECT_LIST_OF_CASE_IDS_BY_EVENT_TYPE_AND_DATE,
                params,
                (rs, rowNum) -> rs.getLong("id")
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
