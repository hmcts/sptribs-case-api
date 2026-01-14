package uk.gov.hmcts.sptribs.ciccase.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.repository.CicaCaseRepository;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseResponse;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CicaCaseServiceTest {

    @Mock
    private CicaCaseRepository cicaCaseRepository;

    @InjectMocks
    private CicaCaseService cicaCaseService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturnCaseWhenFoundByCicaReference() {
        // Given
        String cicaReference = "X12345";
        CicaCaseResponse expectedResponse = createCicaCaseResponse(cicaReference);
        when(cicaCaseRepository.findByCicaReference(cicaReference)).thenReturn(Optional.of(expectedResponse));

        // When
        CicaCaseResponse actualResponse = cicaCaseService.getCaseByCicaReference(cicaReference);

        // Then
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(cicaCaseRepository).findByCicaReference(cicaReference);
    }

    @Test
    void shouldReturnCaseWhenFoundByLowercaseCicaReference() {
        // Given
        String cicaReference = "x12345";
        CicaCaseResponse expectedResponse = createCicaCaseResponse(cicaReference.toUpperCase());
        when(cicaCaseRepository.findByCicaReference(cicaReference)).thenReturn(Optional.of(expectedResponse));

        // When
        CicaCaseResponse actualResponse = cicaCaseService.getCaseByCicaReference(cicaReference);

        // Then
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(cicaCaseRepository).findByCicaReference(cicaReference);
    }

    @Test
    void shouldReturnCaseWhenFoundByGPrefixCicaReference() {
        // Given
        String cicaReference = "G98765";
        CicaCaseResponse expectedResponse = createCicaCaseResponse(cicaReference);
        when(cicaCaseRepository.findByCicaReference(cicaReference)).thenReturn(Optional.of(expectedResponse));

        // When
        CicaCaseResponse actualResponse = cicaCaseService.getCaseByCicaReference(cicaReference);

        // Then
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(cicaCaseRepository).findByCicaReference(cicaReference);
    }

    @Test
    void shouldThrowCaseNotFoundExceptionWhenCaseNotFound() {
        // Given
        String cicaReference = "X99999";
        when(cicaCaseRepository.findByCicaReference(cicaReference)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> cicaCaseService.getCaseByCicaReference(cicaReference))
            .isExactlyInstanceOf(CaseNotFoundException.class)
            .hasMessageContaining("No case found with CICA reference: X99999");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCicaReferenceIsNull() {
        // When / Then
        assertThatThrownBy(() -> cicaCaseService.getCaseByCicaReference(null))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CICA reference cannot be null or empty");

        verify(cicaCaseRepository, never()).findByCicaReference(anyString());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCicaReferenceIsEmpty() {
        // When / Then
        assertThatThrownBy(() -> cicaCaseService.getCaseByCicaReference(""))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CICA reference cannot be null or empty");

        verify(cicaCaseRepository, never()).findByCicaReference(anyString());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCicaReferenceIsBlank() {
        // When / Then
        assertThatThrownBy(() -> cicaCaseService.getCaseByCicaReference("   "))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CICA reference cannot be null or empty");

        verify(cicaCaseRepository, never()).findByCicaReference(anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "A12345", "XYZ", "X", "G", "X1234A", "123X456"})
    void shouldThrowIllegalArgumentExceptionWhenCicaReferenceHasInvalidFormat(String invalidReference) {
        // When / Then
        assertThatThrownBy(() -> cicaCaseService.getCaseByCicaReference(invalidReference))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid CICA reference format");

        verify(cicaCaseRepository, never()).findByCicaReference(anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"X1", "X12345", "X123456789", "G1", "G98765", "x12345", "g98765"})
    void shouldAcceptValidCicaReferenceFormats(String validReference) {
        // Given
        CicaCaseResponse expectedResponse = createCicaCaseResponse(validReference.toUpperCase());
        when(cicaCaseRepository.findByCicaReference(validReference)).thenReturn(Optional.of(expectedResponse));

        // When
        CicaCaseResponse actualResponse = cicaCaseService.getCaseByCicaReference(validReference);

        // Then
        assertThat(actualResponse).isNotNull();
        verify(cicaCaseRepository).findByCicaReference(validReference);
    }

    private CicaCaseResponse createCicaCaseResponse(String cicaReference) {
        JsonNode cicaRefNode = objectMapper.valueToTree(cicaReference);
        return CicaCaseResponse.builder()
            .id("1624351572550045")
            .state("Submitted")
            .data(Map.of("cicCaseCicaReferenceNumber", cicaRefNode))
            .build();
    }
}




