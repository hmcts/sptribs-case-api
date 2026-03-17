package uk.gov.hmcts.sptribs.common.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.dto.RemoveEventWithPrecedingData;
import uk.gov.hmcts.sptribs.common.repositories.exception.CaseEventRepositoryException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for querying the case_event table.
 */
@RequiredArgsConstructor
@Repository
@Slf4j
public class CaseEventRepository {

    public static final String CASE_EVENT_ID = "caseEventId";
    public static final String CREATED_DATE = "createdDate";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String REFERENCE = "reference";
    public static final String CASE_DATA_ID = "case_data_id";
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final ObjectMapper objectMapper;

    private static final String SELECT_LIST_OF_CASE_IDS_BY_EVENT_TYPE_AND_DATE = "SELECT DISTINCT REFERENCE FROM ccd.case_data cd where "
        + "cd.id in (SELECT DISTINCT case_data_id from ccd.case_event WHERE event_id = :caseEventId AND created_date >= :createdDate) "
        + "AND jsonb_array_length(cd.data -> 'furtherUploadedDocuments') > 0";

    private static final String SELECT_LIST_OF_CASE_IDS_BY_EVENT_ID_DURING_DATE_RANGE =
        "SELECT DISTINCT REFERENCE FROM ccd.case_data cd where cd.id in "
            + "(SELECT DISTINCT case_data_id from ccd.case_event "
            + "WHERE event_id = :caseEventId AND created_date >= :startDate AND created_date < :endDate) ";

    private static final String SELECT_REMOVE_EVENTS_WITH_PRECEDING_DATA =
        "WITH events_with_context AS ( "
            + "SELECT ce.case_data_id, ce.event_id, ce.created_date, ce.data, "
            + "LAG(ce.event_id) OVER w AS prev_event, LAG(ce.created_date)  OVER w AS prev_date, LAG(ce.data) OVER w AS prev_data "
            + "FROM ccd.case_event ce WHERE ce.case_data_id = (SELECT id FROM ccd.case_data WHERE reference = :reference)"
            + "WINDOW w AS (PARTITION BY ce.case_data_id ORDER BY ce.created_date) "
            + ")"
        + "SELECT * FROM events_with_context WHERE event_id = :caseEventId AND created_date >= :startDate AND created_date < :endDate";

    public List<Long> getListOfCasesByEventTypeAndDate(String caseEventId, LocalDate createdDate) {

        List<Long> results;
        var params = Map.of(
                CASE_EVENT_ID, caseEventId,
                CREATED_DATE, createdDate
        );

        try {
            results = namedParameterJdbcTemplate.query(SELECT_LIST_OF_CASE_IDS_BY_EVENT_TYPE_AND_DATE,
                params,
                (rs, rowNum) -> rs.getLong(REFERENCE)
            );

        } catch (DataAccessException dataAccessException) {
            log.error("Failed to retrieve affected cases for eventId = {} after {}",
                caseEventId, createdDate, dataAccessException);
            throw new CaseEventRepositoryException(
                "Error whilst retrieving case events to clean further documents", dataAccessException);
        }

        if (results.isEmpty()) {
            log.info("No results found for caseEventId = {}", caseEventId);
            return results;
        }

        return results;

    }

    public List<Long> getListOfCasesByEventIdDuringDateRange(String caseEventId, LocalDate startDate, LocalDate endDate) {

        List<Long> results;
        var params = Map.of(
                CASE_EVENT_ID, caseEventId,
                START_DATE, startDate,
                END_DATE, endDate
        );

        try {
            results = namedParameterJdbcTemplate.query(SELECT_LIST_OF_CASE_IDS_BY_EVENT_ID_DURING_DATE_RANGE,
                params,
                (rs, rowNum) -> rs.getLong(REFERENCE)
            );

        } catch (DataAccessException dataAccessException) {
            log.error("Failed to retrieve list of affected cases for eventId = {} between {} and {}",
                    caseEventId, startDate, endDate, dataAccessException);
            throw new CaseEventRepositoryException(
                    "Failed to retrieve affected cases for eventID = " + caseEventId, dataAccessException);
        }
        return results;
    }

    public List<RemoveEventWithPrecedingData> getRemoveEventsWithPrecedingData(
        Long reference, String caseEventId, LocalDate startDate, LocalDate endDate) {

        var params = Map.of(
                REFERENCE, reference,
                CASE_EVENT_ID, caseEventId,
                START_DATE, startDate,
                END_DATE, endDate
        );

        try {
            List<RemoveEventWithPrecedingData> results = namedParameterJdbcTemplate.query(
                SELECT_REMOVE_EVENTS_WITH_PRECEDING_DATA,
                params,
                (rs, rowNum) -> RemoveEventWithPrecedingData.builder()
                    .caseDataId(rs.getLong(CASE_DATA_ID))
                    .currentEvent(rs.getString("event_id"))
                    .currentEventDate(rs.getTimestamp("created_date").toLocalDateTime())
                    .currentEventData(parseEventData(rs.getString("data"), rs.getLong(CASE_DATA_ID)))
                    .precedingEventId(rs.getString("prev_event"))
                    .precedingEventDate(rs.getTimestamp("prev_date") != null
                        ? rs.getTimestamp("prev_date").toLocalDateTime()
                        : null)
                    .precedingEventData(parseEventData(rs.getString("prev_data"), rs.getLong(CASE_DATA_ID)))
                    .build()
            );

            if (results.isEmpty()) {
                log.info("No remove events found for caseEventId={} between {} and {}",
                    caseEventId, startDate, endDate);
            }

            return results;

        } catch (DataAccessException dataAccessException) {
            log.error("Failed to retrieve remove events for reference={} between {} and {}",
                reference, startDate, endDate, dataAccessException);
            throw new CaseEventRepositoryException(
                "Failed to retrieve remove events for reference=" + reference, dataAccessException);
        }
    }

    private CaseData parseEventData(String json, Long caseDataId) {
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, CaseData.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse event data for case {}", caseDataId, e);
            return null;
        }
    }
}