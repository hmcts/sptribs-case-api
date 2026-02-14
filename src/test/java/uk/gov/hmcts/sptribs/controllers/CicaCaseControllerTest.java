package uk.gov.hmcts.sptribs.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseService;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseResponse;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CicaCaseControllerTest {

    private static final String TEST_AUTHORIZATION = "Bearer test-token";
    private static final String TEST_SERVICE_AUTHORIZATION = "Bearer s2s-token";

    @Mock
    private CicaCaseService cicaCaseService;

    @InjectMocks
    private CicaCaseController cicaCaseController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturnOkWithCaseDataWhenCaseFound() {
        // Given
        String cicaReference = "X12345";
        CicaCaseResponse expectedResponse = createCicaCaseResponse(cicaReference);
        when(cicaCaseService.getCaseByCicaReference(cicaReference)).thenReturn(expectedResponse);

        // When
        ResponseEntity<CicaCaseResponse> response = cicaCaseController.getCaseByCicaReference(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            cicaReference
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
        assertThat(response.getBody().getId()).isEqualTo("1624351572550045");
        assertThat(response.getBody().getState()).isEqualTo("Submitted");
        verify(cicaCaseService).getCaseByCicaReference(cicaReference);
    }

    @Test
    void shouldReturnOkWithCaseDataWhenCaseFoundWithGPrefix() {
        // Given
        String cicaReference = "G98765";
        CicaCaseResponse expectedResponse = createCicaCaseResponse(cicaReference);
        when(cicaCaseService.getCaseByCicaReference(cicaReference)).thenReturn(expectedResponse);

        // When
        ResponseEntity<CicaCaseResponse> response = cicaCaseController.getCaseByCicaReference(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            cicaReference
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(cicaCaseService).getCaseByCicaReference(cicaReference);
    }

    @Test
    void shouldPropagateExceptionWhenCaseNotFound() {
        // Given
        String cicaReference = "X99999";
        when(cicaCaseService.getCaseByCicaReference(cicaReference))
            .thenThrow(new CaseNotFoundException("No case found with CICA reference: X99999"));

        // When / Then
        assertThatThrownBy(() -> cicaCaseController.getCaseByCicaReference(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            cicaReference
        ))
            .isExactlyInstanceOf(CaseNotFoundException.class)
            .hasMessageContaining("No case found with CICA reference: X99999");
    }

    @Test
    void shouldPropagateExceptionWhenInvalidReferenceFormat() {
        // Given
        String cicaReference = "invalid";
        when(cicaCaseService.getCaseByCicaReference(cicaReference))
            .thenThrow(new IllegalArgumentException("Invalid CICA reference format"));

        // When / Then
        assertThatThrownBy(() -> cicaCaseController.getCaseByCicaReference(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            cicaReference
        ))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid CICA reference format");
    }

    @Test
    void shouldHandleLowercaseCicaReference() {
        // Given
        String cicaReference = "x12345";
        CicaCaseResponse expectedResponse = createCicaCaseResponse("X12345");
        when(cicaCaseService.getCaseByCicaReference(cicaReference)).thenReturn(expectedResponse);

        // When
        ResponseEntity<CicaCaseResponse> response = cicaCaseController.getCaseByCicaReference(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            cicaReference
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(cicaCaseService).getCaseByCicaReference(cicaReference);
    }

    private CicaCaseResponse createCicaCaseResponse(String cicaReference) {
        JsonNode cicaRefNode = objectMapper.valueToTree(cicaReference);
        JsonNode fullNameNode = objectMapper.valueToTree("John Smith");
        return CicaCaseResponse.builder()
            .id("1624351572550045")
            .state("Submitted")
            .data(Map.of(
                "cicCaseCicaReferenceNumber", cicaRefNode,
                "cicCaseFullName", fullNameNode
            ))
            .build();
    }
}




