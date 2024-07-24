package uk.gov.hmcts.sptribs.dmn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class DateProviderService {
    private final HolidayService holidayService;

    @Autowired
    public DateProviderService(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    public LocalDate calculateDate(CalculateDateParameters calculateDateParameters) {

        LocalDate now = LocalDate.now(ZoneId.of("Europe/London"));

        if (calculateDateParameters.isWorkingDays()) {
            //Calculate with working days
            if (calculateDateParameters.getPlusOrMinus() == '+') {
                now = addWorkingDays(now, calculateDateParameters.getDayAdjustment());
            } else {
                now = minusWorkingDays(now, calculateDateParameters.getDayAdjustment());
            }
        } else {
            if (calculateDateParameters.getPlusOrMinus() == '+') {
                now = now.plusDays(calculateDateParameters.getDayAdjustment());
            } else {
                now = now.minusDays(calculateDateParameters.getDayAdjustment());
            }
        }

        return now;
    }

    private LocalDate addWorkingDays(LocalDate now, int numberOfDays) {
        if (numberOfDays == 0) {
            return now;
        }

        LocalDate newDate = now.plusDays(1);
        if (isWeekend(newDate) || holidayService.isHoliday(newDate)) {
            return addWorkingDays(newDate, numberOfDays);
        } else {
            return addWorkingDays(newDate, numberOfDays - 1);
        }
    }

    private LocalDate minusWorkingDays(LocalDate now, int numberOfDays) {
        if (numberOfDays == 0) {
            return now;
        }

        LocalDate newDate = now.minusDays(1);
        if (isWeekend(newDate) || holidayService.isHoliday(newDate)) {
            return minusWorkingDays(newDate, numberOfDays);
        } else {
            return minusWorkingDays(newDate, numberOfDays - 1);
        }
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
