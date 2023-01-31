package uk.gov.hmcts.sptribs.ciccase.validation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.validation.ValidationUtil.notNull;

public class CaseValidationTest {

    private static final String EMPTY = " cannot be empty or null";

    @Test
    public void shouldReturnErrorWhenStringIsNull() {
        //When
        List<String> response = notNull(null, "field");
        //Then
        assertThat(response).isEqualTo(List.of("field" + EMPTY));
    }
}
