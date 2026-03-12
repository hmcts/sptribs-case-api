package uk.gov.hmcts.sptribs.bankholidays.service;

import feign.RequestLine;
import uk.gov.hmcts.sptribs.bankholidays.model.BankHolidayResponse;

public interface BankHolidaysApi {
    @RequestLine("GET")
    BankHolidayResponse retrieveAll();
}
