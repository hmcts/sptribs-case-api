package uk.gov.hmcts.sptribs.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseService;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CicaCaseControllerTest {

    private static final String TEST_AUTHORIZATION = "Bearer test-token";
    private static final String TEST_SERVICE_AUTHORIZATION = "Bearer s2s-token";

    @Mock
    private CicaCaseService cicaCaseService;

    @InjectMocks
    private CicaCaseController cicaCaseController;

    @Test
    void shouldReturnOkWhenCheckingIfUserHasAccess() {
        // Given
        String ccdReference = "1234567891234567";

        // When
        ResponseEntity<Void> response = cicaCaseController.checkIfUserHasAccess(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            ccdReference
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(cicaCaseService).checkIfUserHasAccess(ccdReference, TEST_AUTHORIZATION);
    }

    @Test
    void shouldThrowUnauthorisedCaseAccessExceptionWhenServiceThrowsIt() {
        // Given
        String ccdReference = "1234567891234567";
        doThrow(new UnauthorisedCaseAccessException("User is not associated with this case"))
            .when(cicaCaseService).checkIfUserHasAccess(ccdReference, TEST_AUTHORIZATION);

        // When & Then
        assertThatThrownBy(() -> cicaCaseController.checkIfUserHasAccess(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            ccdReference
        ))
            .isInstanceOf(UnauthorisedCaseAccessException.class)
            .hasMessage("User is not associated with this case");

        verify(cicaCaseService).checkIfUserHasAccess(ccdReference, TEST_AUTHORIZATION);
    }

    @Test
    void shouldThrowCaseNotFoundExceptionWhenServiceThrowsIt() {
        // Given
        String ccdReference = "1234567891234567";
        doThrow(new CaseNotFoundException("Case not found"))
            .when(cicaCaseService).checkIfUserHasAccess(ccdReference, TEST_AUTHORIZATION);

        // When & Then
        assertThatThrownBy(() -> cicaCaseController.checkIfUserHasAccess(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            ccdReference
        ))
            .isInstanceOf(CaseNotFoundException.class)
            .hasMessage("Case not found");

        verify(cicaCaseService).checkIfUserHasAccess(ccdReference, TEST_AUTHORIZATION);
    }
}
