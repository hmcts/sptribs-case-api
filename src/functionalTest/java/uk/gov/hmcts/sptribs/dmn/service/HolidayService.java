package uk.gov.hmcts.sptribs.dmn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
public class HolidayService {
    private final Set<LocalDate> holidays;

    @Autowired
    public HolidayService(Set<LocalDate> holidays) {
        this.holidays = holidays;
    }

    public boolean isHoliday(LocalDate localDate) {
        boolean res = holidays.contains(localDate);
        return holidays.contains(localDate);
    }
}
