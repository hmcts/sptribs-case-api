package uk.gov.hmcts.sptribs.endpoint.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.endpoint.data.ValidationStatus.ERRORS;
import static uk.gov.hmcts.sptribs.endpoint.data.ValidationStatus.SUCCESS;
import static uk.gov.hmcts.sptribs.endpoint.data.ValidationStatus.WARNINGS;

@ExtendWith(MockitoExtension.class)
class ValidationStatusTest {

    @Test
    void shouldReturnValidationStatusErrorSIfNonEmptyErrorsPassed() {
        //Given
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        errors.add("ERROR");

        //When
        ValidationStatus validationStatus = ValidationStatus.getValidationStatus(errors, warnings);

        //Then
        assertThat(validationStatus).isEqualTo(ERRORS);
    }

    @Test
    void shouldReturnValidationStatusErrorSIfNonEmptyErrorsAndWarningsPassed() {
        //Given
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        errors.add("ERROR");
        warnings.add("WARNING");

        //When
        ValidationStatus validationStatus = ValidationStatus.getValidationStatus(errors, warnings);

        //Then
        assertThat(validationStatus).isEqualTo(ERRORS);
    }

    @Test
    void shouldReturnValidationStatusWarningsIfEmptyErrorsAndNonEmptyWarningsPassed() {
        //Given
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        warnings.add("WARNING");

        //When
        ValidationStatus validationStatus = ValidationStatus.getValidationStatus(errors, warnings);

        //Then
        assertThat(validationStatus).isEqualTo(WARNINGS);
    }

    @Test
    void shouldReturnValidationStatusSuccessIfEmptyErrorsAndWarningsPassed() {
        //Given
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        //When
        ValidationStatus validationStatus = ValidationStatus.getValidationStatus(errors, warnings);

        //Then
        assertThat(validationStatus).isEqualTo(SUCCESS);
    }
}
