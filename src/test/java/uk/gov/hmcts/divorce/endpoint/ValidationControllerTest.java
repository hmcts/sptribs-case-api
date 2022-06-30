package uk.gov.hmcts.divorce.endpoint;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.divorce.endpoint.data.OcrDataValidationRequest;
import uk.gov.hmcts.divorce.endpoint.data.OcrValidationResponse;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.endpoint.data.FormType.D8;
import static uk.gov.hmcts.divorce.endpoint.data.FormType.D8S;
import static uk.gov.hmcts.divorce.endpoint.data.ValidationStatus.ERRORS;
import static uk.gov.hmcts.divorce.endpoint.data.ValidationStatus.SUCCESS;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.ocrDataValidationRequest;

@ExtendWith(MockitoExtension.class)
public class ValidationControllerTest {

    @InjectMocks
    private ValidationController controller;

    @Test
    void shouldReturnErrorsIfInvalidFormTypePassed() {
        final String invalidFormType = "invalid-form-type";
        final OcrDataValidationRequest request = ocrDataValidationRequest();

        final OcrValidationResponse expectedResponse = OcrValidationResponse.builder()
            .warnings(emptyList())
            .errors(singletonList("Form type '" + invalidFormType + "' not found"))
            .status(ERRORS)
            .build();

        ResponseEntity<OcrValidationResponse> response = controller.validate(invalidFormType, request);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }
}
