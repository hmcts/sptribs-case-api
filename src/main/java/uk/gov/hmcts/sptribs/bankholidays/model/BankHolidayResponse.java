package uk.gov.hmcts.sptribs.bankholidays.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankHolidayResponse {
    private String division;
    private List<BankHolidayEvent> events;
}
