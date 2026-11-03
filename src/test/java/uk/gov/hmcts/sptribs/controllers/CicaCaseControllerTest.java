package uk.gov.hmcts.sptribs.controllers;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseService;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseUpdateService;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseUpdate;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CicaCaseControllerTest {

    private static final String TEST_AUTHORIZATION = "Bearer test-token";
    private static final String TEST_SERVICE_AUTHORIZATION = "Bearer s2s-token";

    @Mock
    private CicaCaseService cicaCaseService;

    @Mock
    private CicaCaseUpdateService cicaCaseUpdateService;

    @InjectMocks
    private CicaCaseController cicaCaseController;

    @Test
    void shouldSubmitUpdateAndReturnOnlyCaseReference() {
        String ccdReference = "1234567891234567";
        CicaCaseUpdate update = new CicaCaseUpdate("New information", List.of());

        ResponseEntity<Map<String, String>> response = cicaCaseController.updateCase(
            TEST_AUTHORIZATION, TEST_SERVICE_AUTHORIZATION, ccdReference, update);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsExactlyEntriesOf(Map.of("case_id", ccdReference));
        verify(cicaCaseUpdateService).updateCase(ccdReference, TEST_AUTHORIZATION, update);
    }

    @ParameterizedTest
    @CsvSource({"401,401", "403,403", "404,404", "409,409", "422,422", "500,500", "503,503", "-1,502"})
    void shouldReturnUpdateFailureWithoutDownstreamBody(int downstreamStatus, int responseStatus) {
        String ccdReference = "1234567891234567";
        CicaCaseUpdate update = new CicaCaseUpdate("New information", List.of());
        FeignException failure = mock(FeignException.class);
        when(failure.status()).thenReturn(downstreamStatus);
        doThrow(failure).when(cicaCaseUpdateService).updateCase(ccdReference, TEST_AUTHORIZATION, update);

        var response = cicaCaseController.updateCase(TEST_AUTHORIZATION, TEST_SERVICE_AUTHORIZATION, ccdReference, update);

        assertThat(response.getStatusCode().value()).isEqualTo(responseStatus);
        assertThat(response.getBody()).isNull();
    }

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

    @Test
    void shouldEnsureCreatorRoleForAuthenticatedCaller() {
        String ccdReference = "1234567891234567";

        ResponseEntity<Void> response = cicaCaseController.ensureCreatorRole(
            TEST_AUTHORIZATION, TEST_SERVICE_AUTHORIZATION, ccdReference);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(cicaCaseService).ensureCreatorRole(ccdReference, TEST_AUTHORIZATION);
    }

    @ParameterizedTest
    @CsvSource({"401,401", "403,403", "404,404", "409,409", "500,500", "503,503", "-1,502", "201,502"})
    void shouldReturnDownstreamErrorStatusWithoutResponseBody(int downstreamStatus, int responseStatus) {
        String ccdReference = "1234567891234567";
        FeignException failure = mock(FeignException.class);
        when(failure.status()).thenReturn(downstreamStatus);
        doThrow(failure).when(cicaCaseService).ensureCreatorRole(ccdReference, TEST_AUTHORIZATION);

        ResponseEntity<Void> response = cicaCaseController.ensureCreatorRole(
            TEST_AUTHORIZATION, TEST_SERVICE_AUTHORIZATION, ccdReference);

        assertThat(response.getStatusCode().value()).isEqualTo(responseStatus);
        assertThat(response.getBody()).isNull();
    }
}
