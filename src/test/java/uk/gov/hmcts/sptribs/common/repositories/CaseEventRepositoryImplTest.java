package uk.gov.hmcts.sptribs.common.repositories;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.dto.RemoveEventWithPrecedingData;
import uk.gov.hmcts.sptribs.common.repositories.exception.CaseEventRepositoryException;
import uk.gov.hmcts.sptribs.common.repositories.impl.CaseEventRepositoryImpl;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseEventRepositoryImplTest {

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CaseEventRepositoryImpl caseEventRepositoryImpl;

    private static final String CASE_EVENT_ID = "caseworker-remove-document";
    private static final LocalDate START_DATE = LocalDate.of(2026, 2, 24);
    private static final LocalDate END_DATE = LocalDate.of(2026, 3, 6);
    private static final Long REFERENCE = 12345L;

    @Nested
    class GetListOfCasesByEventTypeAndDate {

        @Test
        void shouldReturnListOfCaseReferences() {
            when(namedParameterJdbcTemplate.query(
                anyString(), anyMap(), ArgumentMatchers.<RowMapper<Long>>any()))
                .thenReturn(List.of(111L, 222L));

            List<Long> results = caseEventRepositoryImpl.getListOfCasesByEventTypeAndDate(CASE_EVENT_ID, START_DATE);

            assertThat(results).hasSize(2).containsExactly(111L, 222L);
        }

        @Test
        void shouldReturnEmptyListAndLogWhenNoCasesFound() {
            when(namedParameterJdbcTemplate.query(
                anyString(), anyMap(), ArgumentMatchers.<RowMapper<Long>>any()))
                .thenReturn(List.of());

            List<Long> results = caseEventRepositoryImpl.getListOfCasesByEventTypeAndDate(CASE_EVENT_ID, START_DATE);

            assertThat(results).isEmpty();
        }

        @Test
        void shouldThrowCaseEventRepositoryExceptionOnDataAccessException() {
            when(namedParameterJdbcTemplate.query(
                anyString(), anyMap(), ArgumentMatchers.<RowMapper<Long>>any()))
                .thenThrow(new DataAccessResourceFailureException("DB error"));

            assertThatThrownBy(() -> caseEventRepositoryImpl.getListOfCasesByEventTypeAndDate(
                CASE_EVENT_ID, START_DATE))
                .isInstanceOf(CaseEventRepositoryException.class)
                .hasMessageContaining("Error whilst retrieving case events to clean further documents")
                .hasCauseInstanceOf(DataAccessResourceFailureException.class);
        }
    }

    @Nested
    class GetListOfCasesByEventIdDuringDateRange {

        @Test
        void shouldReturnListOfCaseReferences() {
            when(namedParameterJdbcTemplate.query(
                anyString(), anyMap(), ArgumentMatchers.<RowMapper<Long>>any()))
                .thenReturn(List.of(111L, 222L, 333L));

            List<Long> results = caseEventRepositoryImpl.getListOfCasesByEventIdDuringDateRange(CASE_EVENT_ID, START_DATE, END_DATE);

            assertThat(results).hasSize(3).containsExactly(111L, 222L, 333L);
        }

        @Test
        void shouldPassEndDatePlusOneDayAsParameter() {
            ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.captor();

            when(namedParameterJdbcTemplate.query(
                anyString(), paramsCaptor.capture(), ArgumentMatchers.<RowMapper<Long>>any()))
                .thenReturn(List.of());

            caseEventRepositoryImpl.getListOfCasesByEventIdDuringDateRange(CASE_EVENT_ID, START_DATE, END_DATE);

            assertThat(paramsCaptor.getValue()).containsEntry("endDate", END_DATE.plusDays(1));
        }

        @Test
        void shouldReturnEmptyListWhenNoCasesFound() {
            when(namedParameterJdbcTemplate.query(
                anyString(), anyMap(), ArgumentMatchers.<RowMapper<Long>>any()))
                .thenReturn(List.of());

            List<Long> results = caseEventRepositoryImpl.getListOfCasesByEventIdDuringDateRange(CASE_EVENT_ID, START_DATE, END_DATE);

            assertThat(results).isEmpty();
        }

        @Test
        void shouldThrowCaseEventRepositoryExceptionOnDataAccessException() {
            when(namedParameterJdbcTemplate.query(
                anyString(), anyMap(), ArgumentMatchers.<RowMapper<Long>>any()))
                .thenThrow(new DataAccessResourceFailureException("DB error"));

            assertThatThrownBy(() -> caseEventRepositoryImpl.getListOfCasesByEventIdDuringDateRange(CASE_EVENT_ID, START_DATE, END_DATE))
                .isInstanceOf(CaseEventRepositoryException.class)
                .hasMessageContaining("Failed to retrieve affected cases for eventID")
                .hasCauseInstanceOf(DataAccessResourceFailureException.class);
        }
    }

    @Nested
    class GetRemoveEventsWithPrecedingData {

        @Test
        void shouldReturnRemoveEventsWithPrecedingData() {
            RemoveEventWithPrecedingData expectedEvent = RemoveEventWithPrecedingData.builder()
                .caseDataId(999L)
                .currentEvent(CASE_EVENT_ID)
                .currentEventDate(LocalDateTime.of(2026, 2, 25, 10, 0))
                .precedingEventId("caseworker-upload-document")
                .precedingEventDate(LocalDateTime.of(2026, 2, 24, 9, 0))
                .build();


            List<RemoveEventWithPrecedingData> expectedResults = List.of(expectedEvent);
            when(namedParameterJdbcTemplate.query(
                anyString(),
                ArgumentMatchers.anyMap(),
                ArgumentMatchers.<RowMapper<RemoveEventWithPrecedingData>>any()))
                    .thenReturn(expectedResults);

            List<RemoveEventWithPrecedingData> results = caseEventRepositoryImpl
                    .getRemoveEventsWithPrecedingData(REFERENCE, CASE_EVENT_ID, START_DATE, END_DATE);

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getCurrentEvent()).isEqualTo(CASE_EVENT_ID);
            assertThat(results.getFirst().getPrecedingEventId()).isEqualTo("caseworker-upload-document");
        }

        @Test
        void shouldReturnMultipleRemoveEventsForSameCase() {
            RemoveEventWithPrecedingData firstEvent = RemoveEventWithPrecedingData.builder()
                .caseDataId(999L)
                .currentEvent(CASE_EVENT_ID)
                .currentEventDate(LocalDateTime.of(2026, 2, 25, 10, 0))
                .build();

            RemoveEventWithPrecedingData secondEvent = RemoveEventWithPrecedingData.builder()
                .caseDataId(999L)
                .currentEvent(CASE_EVENT_ID)
                .currentEventDate(LocalDateTime.of(2026, 2, 28, 14, 0))
                .build();

            when(namedParameterJdbcTemplate.query(
                anyString(),
                ArgumentMatchers.anyMap(),
                ArgumentMatchers.<RowMapper<RemoveEventWithPrecedingData>>any()))
                .thenReturn(List.of(firstEvent, secondEvent));

            List<RemoveEventWithPrecedingData> results =
                caseEventRepositoryImpl.getRemoveEventsWithPrecedingData(REFERENCE, CASE_EVENT_ID, START_DATE, END_DATE);

            assertThat(results).hasSize(2);
        }

        @Test
        void shouldReturnEmptyListAndLogWhenNoEventsFound() {
            when(namedParameterJdbcTemplate.query(
                anyString(),
                ArgumentMatchers.anyMap(),
                ArgumentMatchers.<RowMapper<RemoveEventWithPrecedingData>>any()))
                .thenReturn(List.of());

            List<RemoveEventWithPrecedingData> results =
                caseEventRepositoryImpl.getRemoveEventsWithPrecedingData(REFERENCE, CASE_EVENT_ID, START_DATE, END_DATE);

            assertThat(results).isEmpty();
        }

        @Test
        void shouldPassEndDatePlusOneDayAsParameter() {
            ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.captor();

            when(namedParameterJdbcTemplate.query(
                anyString(),
                paramsCaptor.capture(),
                ArgumentMatchers.<RowMapper<RemoveEventWithPrecedingData>>any())).thenReturn(List.of());

            caseEventRepositoryImpl.getRemoveEventsWithPrecedingData(REFERENCE, CASE_EVENT_ID, START_DATE, END_DATE);

            assertThat(paramsCaptor.getValue())
                .containsEntry("endDate", END_DATE.plusDays(1))
                .containsEntry("reference", REFERENCE)
                .containsEntry("caseEventId", CASE_EVENT_ID);
        }

        @Test
        void shouldThrowCaseEventRepositoryExceptionOnDataAccessException() {
            when(namedParameterJdbcTemplate.query(
                anyString(),
                ArgumentMatchers.anyMap(),
                ArgumentMatchers.<RowMapper<RemoveEventWithPrecedingData>>any()))
                .thenThrow(new DataAccessResourceFailureException("DB error"));

            assertThatThrownBy(() ->
                caseEventRepositoryImpl.getRemoveEventsWithPrecedingData(REFERENCE, CASE_EVENT_ID, START_DATE, END_DATE))
                .isInstanceOf(CaseEventRepositoryException.class)
                .hasMessageContaining("Failed to retrieve remove events for reference=")
                .hasCauseInstanceOf(DataAccessResourceFailureException.class);
        }

        @Test
        void shouldReturnNullPrecedingDataWhenPrecedingEventIsFirstEvent() {
            RemoveEventWithPrecedingData eventWithNullPreceding = RemoveEventWithPrecedingData.builder()
                .caseDataId(999L)
                .currentEvent(CASE_EVENT_ID)
                .currentEventDate(LocalDateTime.of(2026, 2, 25, 10, 0))
                .precedingEventId(null)
                .precedingEventDate(null)
                .precedingEventData(null)
                .build();

            when(namedParameterJdbcTemplate.query(
                anyString(),
                ArgumentMatchers.anyMap(),
                ArgumentMatchers.<RowMapper<RemoveEventWithPrecedingData>>any())).thenReturn(List.of(eventWithNullPreceding));

            List<RemoveEventWithPrecedingData> results = caseEventRepositoryImpl
                .getRemoveEventsWithPrecedingData(REFERENCE, CASE_EVENT_ID, START_DATE, END_DATE);

            assertThat(results.getFirst().getPrecedingEventId()).isNull();
            assertThat(results.getFirst().getPrecedingEventDate()).isNull();
            assertThat(results.getFirst().getPrecedingEventData()).isNull();
        }
    }

    @Nested
    class VerifyRowMappers {

        public static final String REFERENCE_COLUMN_LABEL = "reference";

        @Mock
        private ResultSet rs;

        @Test
        void shouldMapResultSetToLongForGetListOfCasesByEventTypeAndDate() throws Exception {
            when(rs.getLong(REFERENCE_COLUMN_LABEL)).thenReturn(111L);

            ArgumentCaptor<RowMapper<Long>> mapperCaptor = ArgumentCaptor.captor();

            when(namedParameterJdbcTemplate.query(
                anyString(),
                ArgumentMatchers.anyMap(),
                mapperCaptor.capture())).thenReturn(List.of());

            caseEventRepositoryImpl.getListOfCasesByEventTypeAndDate(CASE_EVENT_ID, START_DATE);

            Long result = mapperCaptor.getValue().mapRow(rs, 0);

            assertThat(result).isEqualTo(111L);
        }

        @Test
        void shouldMapResultSetToLongForGetListOfCasesByEventIdDuringDateRange() throws Exception {
            when(rs.getLong(REFERENCE_COLUMN_LABEL)).thenReturn(222L);

            ArgumentCaptor<RowMapper<Long>> mapperCaptor = ArgumentCaptor.captor();

            when(namedParameterJdbcTemplate.query(
                anyString(),
                ArgumentMatchers.anyMap(),
                mapperCaptor.capture())).thenReturn(List.of());

            caseEventRepositoryImpl.getListOfCasesByEventIdDuringDateRange(CASE_EVENT_ID, START_DATE, END_DATE);

            Long result = mapperCaptor.getValue().mapRow(rs, 0);

            assertThat(result).isEqualTo(222L);
        }

        @Test
        void shouldMapResultSetToRemoveEventWithPrecedingData() throws Exception {
            Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.of(2026, 2, 25, 10, 0));
            Timestamp prevTimestamp = Timestamp.valueOf(LocalDateTime.of(2026, 2, 24, 9, 0));

            when(rs.getLong("case_data_id")).thenReturn(999L);
            when(rs.getString("event_id")).thenReturn(CASE_EVENT_ID);
            when(rs.getTimestamp("created_date")).thenReturn(currentTimestamp);
            when(rs.getString("data")).thenReturn(null);
            when(rs.getString("prev_event")).thenReturn("caseworker-upload-document");
            when(rs.getTimestamp("prev_date")).thenReturn(prevTimestamp);
            when(rs.getString("prev_data")).thenReturn(null);

            ArgumentCaptor<RowMapper<RemoveEventWithPrecedingData>> mapperCaptor = ArgumentCaptor.captor();

            when(namedParameterJdbcTemplate.query(
                anyString(),
                ArgumentMatchers.anyMap(),
                mapperCaptor.capture())).thenReturn(List.of());

            caseEventRepositoryImpl.getRemoveEventsWithPrecedingData(REFERENCE, CASE_EVENT_ID, START_DATE, END_DATE);

            RemoveEventWithPrecedingData result = mapperCaptor.getValue().mapRow(rs, 0);

            assertThat(result).isNotNull();
            assertThat(result.getCaseDataId()).isEqualTo(999L);
            assertThat(result.getCurrentEvent()).isEqualTo(CASE_EVENT_ID);
            assertThat(result.getCurrentEventDate()).isEqualTo(LocalDateTime.of(2026, 2, 25, 10, 0));
            assertThat(result.getPrecedingEventId()).isEqualTo("caseworker-upload-document");
            assertThat(result.getPrecedingEventDate()).isEqualTo(LocalDateTime.of(2026, 2, 24, 9, 0));
            assertThat(result.getCurrentEventData()).isNull();
            assertThat(result.getPrecedingEventData()).isNull();
        }

        @Test
        void shouldMapNullPrevDateToNullPrecedingEventDate() throws Exception {
            when(rs.getLong("case_data_id")).thenReturn(999L);
            when(rs.getString("event_id")).thenReturn(CASE_EVENT_ID);
            when(rs.getTimestamp("created_date")).thenReturn(Timestamp.valueOf(LocalDateTime.of(2026, 2, 25, 10, 0)));
            when(rs.getString("data")).thenReturn(null);
            when(rs.getString("prev_event")).thenReturn(null);
            when(rs.getTimestamp("prev_date")).thenReturn(null);  // first event - no preceding
            when(rs.getString("prev_data")).thenReturn(null);

            ArgumentCaptor<RowMapper<RemoveEventWithPrecedingData>> mapperCaptor = ArgumentCaptor.captor();

            when(namedParameterJdbcTemplate.query(
                anyString(),
                ArgumentMatchers.anyMap(),
                mapperCaptor.capture())).thenReturn(List.of());

            caseEventRepositoryImpl.getRemoveEventsWithPrecedingData(REFERENCE, CASE_EVENT_ID, START_DATE, END_DATE);

            RemoveEventWithPrecedingData result = mapperCaptor.getValue().mapRow(rs, 0);

            assertNotNull(result);
            assertThat(result.getPrecedingEventDate()).isNull();
        }
    }

    @Nested
    class VerifyParseEventData {

        @Mock
        private ResultSet rs;

        @Test
        void shouldParseValidJsonIntoCaseData() throws Exception {
            String validJson = "{\"cicCase\": {}}";
            CaseData expectedCaseData = CaseData.builder().build();

            when(objectMapper.readValue(validJson, CaseData.class)).thenReturn(expectedCaseData);

            when(rs.getLong("case_data_id")).thenReturn(999L);
            when(rs.getString("event_id")).thenReturn(CASE_EVENT_ID);
            when(rs.getTimestamp("created_date")).thenReturn(Timestamp.valueOf(LocalDateTime.of(2026, 2, 25, 10, 0)));
            when(rs.getString("data")).thenReturn(validJson);
            when(rs.getString("prev_event")).thenReturn(null);
            when(rs.getTimestamp("prev_date")).thenReturn(null);
            when(rs.getString("prev_data")).thenReturn(null);

            ArgumentCaptor<RowMapper<RemoveEventWithPrecedingData>> mapperCaptor = ArgumentCaptor.captor();

            when(namedParameterJdbcTemplate.query(
                anyString(),
                ArgumentMatchers.anyMap(),
                mapperCaptor.capture())).thenReturn(List.of());

            caseEventRepositoryImpl.getRemoveEventsWithPrecedingData(REFERENCE, CASE_EVENT_ID, START_DATE, END_DATE);

            RemoveEventWithPrecedingData result = mapperCaptor.getValue().mapRow(rs, 0);

            assertNotNull(result);
            assertThat(result.getCurrentEventData()).isEqualTo(expectedCaseData);
        }

        @Test
        void shouldReturnNullWhenJsonParsingFails() throws Exception {
            String invalidJson = "invalid-json";

            when(objectMapper.readValue(invalidJson, CaseData.class)).thenThrow(new JsonProcessingException("parse error") {});

            when(rs.getLong("case_data_id")).thenReturn(999L);
            when(rs.getString("event_id")).thenReturn(CASE_EVENT_ID);
            when(rs.getTimestamp("created_date")).thenReturn(Timestamp.valueOf(LocalDateTime.of(2026, 2, 25, 10, 0)));
            when(rs.getString("data")).thenReturn(invalidJson);
            when(rs.getString("prev_event")).thenReturn(null);
            when(rs.getTimestamp("prev_date")).thenReturn(null);
            when(rs.getString("prev_data")).thenReturn(null);

            ArgumentCaptor<RowMapper<RemoveEventWithPrecedingData>> mapperCaptor = ArgumentCaptor.captor();

            when(namedParameterJdbcTemplate.query(
                anyString(),
                ArgumentMatchers.anyMap(),
                mapperCaptor.capture())).thenReturn(List.of());

            caseEventRepositoryImpl.getRemoveEventsWithPrecedingData(REFERENCE, CASE_EVENT_ID, START_DATE, END_DATE);

            RemoveEventWithPrecedingData result = mapperCaptor.getValue().mapRow(rs, 0);

            assertNotNull(result);
            assertThat(result.getCurrentEventData()).isNull();
        }

        @Test
        void shouldReturnNullWhenJsonIsNull() throws Exception {
            when(rs.getLong("case_data_id")).thenReturn(999L);
            when(rs.getString("event_id")).thenReturn(CASE_EVENT_ID);
            when(rs.getTimestamp("created_date")).thenReturn(Timestamp.valueOf(LocalDateTime.of(2026, 2, 25, 10, 0)));
            when(rs.getString("data")).thenReturn(null);
            when(rs.getString("prev_event")).thenReturn(null);
            when(rs.getTimestamp("prev_date")).thenReturn(null);
            when(rs.getString("prev_data")).thenReturn(null);

            ArgumentCaptor<RowMapper<RemoveEventWithPrecedingData>> mapperCaptor = ArgumentCaptor.captor();

            when(namedParameterJdbcTemplate.query(
                anyString(),
                ArgumentMatchers.anyMap(),
                mapperCaptor.capture())).thenReturn(List.of());

            caseEventRepositoryImpl.getRemoveEventsWithPrecedingData(REFERENCE, CASE_EVENT_ID, START_DATE, END_DATE);

            RemoveEventWithPrecedingData result = mapperCaptor.getValue().mapRow(rs, 0);

            assertNotNull(result);
            assertThat(result.getCurrentEventData()).isNull();
            assertThat(result.getPrecedingEventData()).isNull();
        }
    }
}
