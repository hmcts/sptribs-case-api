package uk.gov.hmcts.sptribs.common.repositories;


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
import uk.gov.hmcts.sptribs.common.dto.RemoveEventWithPrecedingData;
import uk.gov.hmcts.sptribs.common.repositories.exception.CaseEventRepositoryException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseEventRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CaseEventRepository caseEventRepository;

    private static final String CASE_EVENT_ID = "caseworker-remove-document";
    private static final LocalDate START_DATE = LocalDate.of(2026, 2, 24);
    private static final LocalDate END_DATE = LocalDate.of(2026, 3, 5);
    private static final Long REFERENCE = 12345L;

    @Nested
    class GetListOfCasesByEventTypeAndDate {

        @Test
        void shouldReturnListOfCaseReferences() {
            when(namedParameterJdbcTemplate.query(
                    anyString(), anyMap(), ArgumentMatchers.<RowMapper<Long>>any()))
                    .thenReturn(List.of(111L, 222L));

            List<Long> results = caseEventRepository.getListOfCasesByEventTypeAndDate(
                    CASE_EVENT_ID, START_DATE);

            assertThat(results).hasSize(2).containsExactly(111L, 222L);
        }

        @Test
        void shouldReturnEmptyListAndLogWhenNoCasesFound() {
            when(namedParameterJdbcTemplate.query(
                    anyString(), anyMap(), ArgumentMatchers.<RowMapper<Long>>any()))
                    .thenReturn(List.of());

            List<Long> results = caseEventRepository.getListOfCasesByEventTypeAndDate(
                    CASE_EVENT_ID, START_DATE);

            assertThat(results).isEmpty();
        }

        @Test
        void shouldThrowCaseEventRepositoryExceptionOnDataAccessException() {
            when(namedParameterJdbcTemplate.query(
                    anyString(), anyMap(), ArgumentMatchers.<RowMapper<Long>>any()))
                    .thenThrow(new DataAccessResourceFailureException("DB error"));

            assertThatThrownBy(() -> caseEventRepository.getListOfCasesByEventTypeAndDate(
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

            List<Long> results = caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                    CASE_EVENT_ID, START_DATE, END_DATE);

            assertThat(results).hasSize(3).containsExactly(111L, 222L, 333L);
        }

        @Test
        void shouldPassEndDatePlusOneDayAsParameter() {
            ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.captor();

            when(namedParameterJdbcTemplate.query(
                    anyString(), paramsCaptor.capture(), ArgumentMatchers.<RowMapper<Long>>any()))
                    .thenReturn(List.of());

            caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                    CASE_EVENT_ID, START_DATE, END_DATE);

            assertThat(paramsCaptor.getValue())
                    .containsEntry("endDate", END_DATE.plusDays(1));
        }

        @Test
        void shouldReturnEmptyListWhenNoCasesFound() {
            when(namedParameterJdbcTemplate.query(
                    anyString(), anyMap(), ArgumentMatchers.<RowMapper<Long>>any()))
                    .thenReturn(List.of());

            List<Long> results = caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                    CASE_EVENT_ID, START_DATE, END_DATE);

            assertThat(results).isEmpty();
        }

        @Test
        void shouldThrowCaseEventRepositoryExceptionOnDataAccessException() {
            when(namedParameterJdbcTemplate.query(
                    anyString(), anyMap(), ArgumentMatchers.<RowMapper<Long>>any()))
                    .thenThrow(new DataAccessResourceFailureException("DB error"));

            assertThatThrownBy(() -> caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                    CASE_EVENT_ID, START_DATE, END_DATE))
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

            List<RemoveEventWithPrecedingData> results = caseEventRepository
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

            List<RemoveEventWithPrecedingData> results = caseEventRepository
                    .getRemoveEventsWithPrecedingData(REFERENCE, CASE_EVENT_ID, START_DATE, END_DATE);

            assertThat(results).hasSize(2);
        }

        @Test
        void shouldReturnEmptyListAndLogWhenNoEventsFound() {
            when(namedParameterJdbcTemplate.query(
                    anyString(),
                    ArgumentMatchers.anyMap(),
                    ArgumentMatchers.<RowMapper<RemoveEventWithPrecedingData>>any()))
                    .thenReturn(List.of());

            List<RemoveEventWithPrecedingData> results = caseEventRepository
                    .getRemoveEventsWithPrecedingData(REFERENCE, CASE_EVENT_ID, START_DATE, END_DATE);

            assertThat(results).isEmpty();
        }

        @Test
        void shouldPassEndDatePlusOneDayAsParameter() {
            ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.captor();

            when(namedParameterJdbcTemplate.query(
                    anyString(),
                    paramsCaptor.capture(),
                    ArgumentMatchers.<RowMapper<RemoveEventWithPrecedingData>>any()))
                    .thenReturn(List.of());

            caseEventRepository.getRemoveEventsWithPrecedingData(
                    REFERENCE, CASE_EVENT_ID, START_DATE, END_DATE);

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

            assertThatThrownBy(() -> caseEventRepository.getRemoveEventsWithPrecedingData(
                    REFERENCE, CASE_EVENT_ID, START_DATE, END_DATE))
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
                    ArgumentMatchers.<RowMapper<RemoveEventWithPrecedingData>>any()))
                    .thenReturn(List.of(eventWithNullPreceding));

            List<RemoveEventWithPrecedingData> results = caseEventRepository
                    .getRemoveEventsWithPrecedingData(REFERENCE, CASE_EVENT_ID, START_DATE, END_DATE);

            assertThat(results.getFirst().getPrecedingEventId()).isNull();
            assertThat(results.getFirst().getPrecedingEventDate()).isNull();
            assertThat(results.getFirst().getPrecedingEventData()).isNull();
        }
    }
}