package uk.gov.hmcts.sptribs.systemupdate.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

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
    private final ObjectMapper objectMapper;

    private static final String SELECT_LIST_OF_CASE_IDS_BY_EVENT_TYPE_AND_DATE = "SELECT DISTINCT case_data_id from ccd.case_event " +
        "WHERE event_id = :caseEventId AND created_date >= :createdDate AND data::jsonb ? 'furtherUploadedDocuments'";

    @Autowired
    public CaseEventRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate, ObjectMapper objectMapper) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Long> getListOfCasesByEventTypeAndDate(String caseEventId, String createdDate) {

        var params = Map.of(
            "caseEventId", caseEventId,
            "createdDate", createdDate
        );

        List<Long> results = namedParameterJdbcTemplate.query(SELECT_LIST_OF_CASE_IDS_BY_EVENT_TYPE_AND_DATE,
            params,
            (rs, rowNum) -> rs.getLong("id")
        );


        if (results.isEmpty()) {
             log.info("No results found for caseEventId = {}", caseEventId);
             return results;
        }

        return results;

    }
}
