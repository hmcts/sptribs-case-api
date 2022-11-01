package uk.gov.hmcts.sptribs.endpoint;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.sptribs.endpoint.data.OcrDataValidationRequest;
import uk.gov.hmcts.sptribs.endpoint.data.OcrValidationResponse;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.endpoint.data.ValidationStatus.ERRORS;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.ocrDataValidationRequest;

@ExtendWith(MockitoExtension.class)
class ValidationControllerTest {

    @InjectMocks
    private ValidationController controller;

    @Test
    void shouldReturnErrorsIfInvalidFormTypePassed() {
        //Given
        final String invalidFormType = "invalid-form-type";
        final OcrDataValidationRequest request = ocrDataValidationRequest();

        final OcrValidationResponse expectedResponse = OcrValidationResponse.builder()
            .warnings(emptyList())
            .errors(singletonList("Form type '" + invalidFormType + "' not found"))
            .status(ERRORS)
            .build();

        //When
        ResponseEntity<OcrValidationResponse> response = controller.validate(invalidFormType, request);

        //Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void shouldReturnNullIfValidFormTypePassed() {
        //Given
        final String validFormType = "D8";
        final OcrDataValidationRequest request = ocrDataValidationRequest();

        //When
        ResponseEntity<OcrValidationResponse> response = controller.validate(validFormType, request);

        //Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNull();
    }
}
