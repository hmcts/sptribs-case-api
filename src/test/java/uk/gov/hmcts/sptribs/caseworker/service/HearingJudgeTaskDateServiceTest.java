package uk.gov.hmcts.sptribs.caseworker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.bankholidays.model.BankHolidayEvent;
import uk.gov.hmcts.sptribs.bankholidays.model.BankHolidayResponse;
import uk.gov.hmcts.sptribs.bankholidays.service.BankHolidayService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingJudgeTaskDateServiceTest {

    @Mock
    private BankHolidayService bankHolidayService;

    private HearingJudgeTaskDateService service;

    @BeforeEach
    void setUp() {
        service = new HearingJudgeTaskDateService(bankHolidayService, "https://example.invalid/bank-holidays");
    }

    @Test
    void countsWeekendsWhenCalculatingFourteenDays() {
        holidays();
        assertThat(service.releaseDate(LocalDate.of(2026, 10, 30))).isEqualTo(LocalDate.of(2026, 10, 16));
    }

    @Test
    void skipsBankHolidaysWithoutSkippingWeekends() {
        holidays(LocalDate.of(2026, 10, 19));
        assertThat(service.releaseDate(LocalDate.of(2026, 10, 30))).isEqualTo(LocalDate.of(2026, 10, 15));
    }

    @Test
    void doesNotCreateTaskForPastHearingOrBeforeReleaseDate() {
        LocalDate today = service.today();
        assertThat(service.isDue(today.plusDays(15), today.plusDays(1))).isFalse();
        assertThat(service.isDue(today.plusDays(14), today)).isTrue();
        assertThat(service.isDue(today.plusDays(1), today.minusDays(13))).isTrue();
        assertThat(service.isDue(today.minusDays(1), today.minusDays(15))).isFalse();
    }

    @Test
    void returnsNullWhenHearingDateIsNull() {
        assertThat(service.releaseDate(null)).isNull();
    }

    @Test
    void returnsFalseWhenEitherDateIsNull() {
        LocalDate today = service.today();
        assertThat(service.isDue(null, today)).isFalse();
        assertThat(service.isDue(today, null)).isFalse();
        assertThat(service.isDue(null, null)).isFalse();
    }

    private void holidays(LocalDate... dates) {
        BankHolidayResponse response = new BankHolidayResponse();
        response.setEvents(Stream.of(dates).map(date -> {
            BankHolidayEvent event = new BankHolidayEvent();
            event.setDate(date);
            return event;
        }).toList());
        when(bankHolidayService.getScottishBankHolidays("https://example.invalid/bank-holidays")).thenReturn(response);
    }
}
