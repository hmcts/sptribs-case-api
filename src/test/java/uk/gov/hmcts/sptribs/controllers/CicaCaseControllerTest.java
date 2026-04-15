package uk.gov.hmcts.sptribs.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseService;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseResponse;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.mapper.CicaCaseMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.CicaCaseTestHelper.createCicaCaseEntity;
import static uk.gov.hmcts.sptribs.testutil.CicaCaseTestHelper.createCicaCaseResponse;

@ExtendWith(MockitoExtension.class)
class CicaCaseControllerTest {

    private static final String TEST_AUTHORIZATION = "Bearer test-token";
    private static final String TEST_SERVICE_AUTHORIZATION = "Bearer s2s-token";

    @Mock
    private CicaCaseService cicaCaseService;

    @Mock
    private CicaCaseMapper cicaCaseMapper;

    @InjectMocks
    private CicaCaseController cicaCaseController;

    @Test
    void shouldReturnOkWithCaseDataWhenCaseFound() {
        // Given
        String ccdReference = "1234567891234567";
        CicaCaseEntity cicaCaseEntity = createCicaCaseEntity(ccdReference);
        CicaCaseResponse expectedResponse = createCicaCaseResponse(ccdReference);
        when(cicaCaseService.getCaseByCCDReference(ccdReference, TEST_AUTHORIZATION)).thenReturn(cicaCaseEntity);

        when(cicaCaseMapper.toResponse(cicaCaseEntity)).thenReturn(expectedResponse);

        // When
        ResponseEntity<CicaCaseResponse> response = cicaCaseController.getCaseByCCDReference(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            ccdReference
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getId()).isEqualTo("1234567891234567");
        assertThat(response.getBody().getState()).isEqualTo("Submitted");
        verify(cicaCaseService).getCaseByCCDReference(ccdReference, TEST_AUTHORIZATION);
        verify(cicaCaseMapper).toResponse(cicaCaseEntity);
    }

    @Test
    void shouldPropagateExceptionWhenCaseNotFound() {
        // Given
        String ccdReference = "1234567891234567";
        when(cicaCaseService.getCaseByCCDReference(ccdReference, TEST_AUTHORIZATION))
            .thenThrow(new CaseNotFoundException("No case found with CICA reference: X99999"));

        // When / Then
        assertThatThrownBy(() -> cicaCaseController.getCaseByCCDReference(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            ccdReference
        ))
            .isExactlyInstanceOf(CaseNotFoundException.class)
            .hasMessageContaining("No case found with CICA reference: X99999");

        verifyNoInteractions(cicaCaseMapper);
    }

    @Test
    void shouldPropagateExceptionWhenInvalidReferenceFormat() {
        // Given
        String ccdReference = "1234567891234";
        when(cicaCaseService.getCaseByCCDReference(ccdReference, TEST_AUTHORIZATION))
            .thenThrow(new IllegalArgumentException("Invalid CICA reference format"));

        // When / Then
        assertThatThrownBy(() -> cicaCaseController.getCaseByCCDReference(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            ccdReference
        ))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid CICA reference format");

        verifyNoInteractions(cicaCaseMapper);
    }
}




