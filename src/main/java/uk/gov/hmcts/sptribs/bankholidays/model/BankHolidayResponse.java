package uk.gov.hmcts.sptribs.bankholidays.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankHolidayResponse {
    private String division;
    private List<BankHolidayEvent> events;

    public Set<LocalDate> getDates() {
        return events.stream()
                .map(BankHolidayEvent::getDate)
                .collect(Collectors.toSet());
    }
}
