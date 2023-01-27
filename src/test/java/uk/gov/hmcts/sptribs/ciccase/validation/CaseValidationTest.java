package uk.gov.hmcts.sptribs.ciccase.validation;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.YEARS;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.validation.ValidationUtil.notNull;
import static uk.gov.hmcts.sptribs.ciccase.validation.ValidationUtil.validateMarriageDate;

public class CaseValidationTest {

    private static final String LESS_THAN_ONE_YEAR_AGO = " can not be less than one year ago.";
    private static final String EMPTY = " cannot be empty or null";
    private static final String IN_THE_FUTURE = " can not be in the future.";
    private static final String MORE_THAN_ONE_HUNDRED_YEARS_AGO = " can not be more than 100 years ago.";

    @Test
    public void shouldReturnErrorWhenStringIsNull() {
        //When
        List<String> response = notNull(null, "field");
        //Then
        assertThat(response).isEqualTo(List.of("field" + EMPTY));
    }

    @Test
    public void shouldReturnErrorWhenDateIsInTheFuture() {
        //When
        List<String> response = validateMarriageDate(LocalDate.now().plus(2, YEARS), "field");
        //Then
        assertThat(response).isEqualTo(List.of("field" + IN_THE_FUTURE));
    }

    @Test
    public void shouldReturnErrorWhenDateIsOverOneHundredYearsAgo() {
        //Given
        LocalDate oneHundredYearsAndOneDayAgo = LocalDate.now()
            .minus(100, YEARS)
            .minus(1, DAYS);

        //When
        List<String> response = validateMarriageDate(oneHundredYearsAndOneDayAgo, "field");

        //Then
        assertThat(response).isEqualTo(List.of("field" + MORE_THAN_ONE_HUNDRED_YEARS_AGO));
    }

    @Test
    public void shouldReturnErrorWhenDateIsLessThanOneYearAgo() {
        //When
        List<String> response = validateMarriageDate(LocalDate.now().minus(360, DAYS), "field");

        //Then
        assertThat(response).isEqualTo(List.of("field" + LESS_THAN_ONE_YEAR_AGO));
    }
}
