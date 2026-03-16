package uk.gov.hmcts.sptribs.bankholidays.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankHolidayEvent {
    private String title;
    private LocalDate date;
    private String notes;
    private boolean bunting;
}