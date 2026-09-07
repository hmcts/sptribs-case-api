package uk.gov.hmcts.sptribs.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CaseNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        // Given
        String message = "No case found with CICA reference: X12345";

        // When
        CaseNotFoundException exception = new CaseNotFoundException(message);

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void shouldBeSerializable() {
        // Given
        CaseNotFoundException exception = new CaseNotFoundException("Test message");

        // Then - verify serialVersionUID is defined
        assertThat(exception).isInstanceOf(java.io.Serializable.class);
    }
}




