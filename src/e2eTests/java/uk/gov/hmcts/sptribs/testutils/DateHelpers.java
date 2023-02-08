package uk.gov.hmcts.sptribs.testutils;

import java.util.Calendar;

public class DateHelpers {

    private DateHelpers() {
    }

    public static Calendar getYesterdaysDate() {
        Calendar date = Calendar.getInstance();
        Calendar yesterday = (Calendar) date.clone();
        yesterday.add(Calendar.DATE, -1);
        return yesterday;
    }

    public static Calendar getFutureDate(int numberOfDays) {
        Calendar date = Calendar.getInstance();
        Calendar futureDate = (Calendar) date.clone();
        futureDate.add(Calendar.DATE, numberOfDays);
        return futureDate;
    }
}
