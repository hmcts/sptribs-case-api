package uk.gov.hmcts.sptribs.common.repositories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.common.dtos.RemoveEventWithPrecedingData;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for querying the case_event table.
 * TODO: Relocate this class to a more appropriate package
 * once the cleanup/refactoring is complete.
 */
@RequiredArgsConstructor
@Repository
@Slf4j
public class CaseEventRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String SELECT_LIST_OF_CASE_IDS_BY_EVENT_TYPE_AND_DATE = "SELECT DISTINCT REFERENCE FROM ccd.case_data cd where "
            + "cd.id in (SELECT DISTINCT case_data_id from ccd.case_event WHERE event_id = :caseEventId AND created_date >= :createdDate) "
            + "AND jsonb_array_length(cd.data -> 'furtherUploadedDocuments') > 0";

    private static final String SELECT_REMOVE_EVENTS_WITH_PRECEDING_DATA =
            "WITH remove_events AS ( " +
                    "    SELECT case_data_id " +
                    "    FROM ccd.case_event " +
                    "    WHERE event_id = :caseEventId " +
                    "    AND created_date >= :startDate " +
                    "    AND created_date < :endDate " +
                    "), " +
                    "events_with_lag AS ( " +
                    "    SELECT " +
                    "        ce.case_data_id, " +
                    "        ce.created_date, " +
                    "        ce.data -> 'ordersList' AS orders_after, " +
                    "        LAG(ce.data -> 'ordersList') OVER " +
                    "            (PARTITION BY ce.case_data_id ORDER BY ce.created_date) AS orders_before " +
                    "    FROM ccd.case_event ce " +
                    "    INNER JOIN remove_events re ON ce.case_data_id = re.case_data_id " +
                    ") " +
                    "SELECT * FROM events_with_lag " +
                    "WHERE event_id = :caseEventId " +
                    "ORDER BY case_data_id, created_date DESC";

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

    public List<RemoveEventWithPrecedingData> getRemoveEventsWithPrecedingData(
            String caseEventId, LocalDate startDate, LocalDate endDate) {

        var params = Map.of(
                "caseEventId", caseEventId,
                "startDate", startDate,
                "endDate", endDate
        );

        try {
            List<RemoveEventWithPrecedingData> results = namedParameterJdbcTemplate.query(
                    SELECT_REMOVE_EVENTS_WITH_PRECEDING_DATA,
                    params,
                    (rs, rowNum) -> RemoveEventWithPrecedingData.builder()
                            .caseDataId(rs.getLong("case_data_id"))
                            .removeEventDate(rs.getTimestamp("created_date").toLocalDateTime())
                            .ordersAfterJson(rs.getString("orders_after"))
                            .ordersBeforeJson(rs.getString("orders_before"))
                            .build()
            );

            if (results.isEmpty()) {
                log.info("No remove events found for caseEventId={} between {} and {}",
                        caseEventId, startDate, endDate);
            }

            return results;

        } catch (DataAccessException e) {
            throw new RuntimeException("Error retrieving remove events with preceding data", e);
        }
    }
}