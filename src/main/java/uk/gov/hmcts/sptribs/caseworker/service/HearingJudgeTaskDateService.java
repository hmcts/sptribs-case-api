package uk.gov.hmcts.sptribs.caseworker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.bankholidays.service.BankHolidayService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

@Service
public class HearingJudgeTaskDateService {

    private static final int NOTICE_DAYS = 14;
    private static final ZoneId HEARING_ZONE = ZoneId.of("Europe/London");

    private final BankHolidayService bankHolidayService;
    private final String bankHolidayUrl;

    public HearingJudgeTaskDateService(BankHolidayService bankHolidayService,
                                       @Value("${bank-holidays.api.url}") String bankHolidayUrl) {
        this.bankHolidayService = bankHolidayService;
        this.bankHolidayUrl = bankHolidayUrl;
    }

    public LocalDate today() {
        return LocalDate.now(HEARING_ZONE);
    }

    public LocalDate releaseDate(LocalDate hearingDate) {
        if (hearingDate == null) {
            return null;
        }
        Set<LocalDate> bankHolidays = bankHolidayService.getScottishBankHolidays(bankHolidayUrl).getDates();
        LocalDate releaseDate = hearingDate;
        int days = 0;
        while (days < NOTICE_DAYS) {
            releaseDate = releaseDate.minusDays(1);
            if (!bankHolidays.contains(releaseDate)) {
                days++;
            }
        }
        return releaseDate;
    }

    public boolean isDue(LocalDate hearingDate, LocalDate releaseDate) {
        LocalDate today = today();
        return hearingDate != null && releaseDate != null
            && !hearingDate.isBefore(today) && !releaseDate.isAfter(today);
    }
}
