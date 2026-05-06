package uk.gov.hmcts.sptribs.ciccase.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.CicaCaseTestHelper.createCicaCaseEntity;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SYSTEM_UPDATE_USER_EMAIL;

@ExtendWith(MockitoExtension.class)
class CicaCaseServiceTest {

    private static final String TEST_AUTHORIZATION = "Bearer test-token";

    @Mock
    private CaseDataRepository caseDataRepository;

    @Mock
    private IdamService idamService;

    @Mock
    private User user;

    @InjectMocks
    private CicaCaseService cicaCaseService;

    @Test
    void whenGetCaseByCCDReference_thenShouldReturnCaseSuccessfully() {
        // Given
        String ccdReference = "1234567891234567";
        CicaCaseEntity expectedEntity = createCicaCaseEntity(ccdReference);

        when(user.getUserDetails()).thenReturn(userDetails());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkCaseExists(ccdReference)).thenReturn(true);
        when(caseDataRepository.findCase(ccdReference, TEST_SYSTEM_UPDATE_USER_EMAIL))
            .thenReturn(Optional.of(expectedEntity));

        // When
        CicaCaseEntity actualEntity = cicaCaseService.getCaseByCCDReference(ccdReference, TEST_AUTHORIZATION);

        // Then
        assertThat(actualEntity).isEqualTo(expectedEntity);
        verify(idamService).retrieveUser(TEST_AUTHORIZATION);
        verify(caseDataRepository).checkCaseExists(ccdReference);
        verify(caseDataRepository).findCase(ccdReference, TEST_SYSTEM_UPDATE_USER_EMAIL);
    }

    @Test
    void whenGetCaseByCCDReference_thenThrowUnauthorisedCaseAccessExceptionAsEmailNotPresent() {
        // Given
        String ccdReference = "1234567891234567";

        when(user.getUserDetails()).thenReturn(userDetails());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkCaseExists(ccdReference)).thenReturn(true);
        when(caseDataRepository.findCase(ccdReference, TEST_SYSTEM_UPDATE_USER_EMAIL))
            .thenReturn(Optional.empty());

        // When & Then
        UnauthorisedCaseAccessException exception = assertThrows(
            UnauthorisedCaseAccessException.class,
            () -> cicaCaseService.getCaseByCCDReference(ccdReference, TEST_AUTHORIZATION)
        );

        // Then
        assertEquals("User is not authorised to access case: " + ccdReference, exception.getMessage());

        verify(caseDataRepository).findCase(ccdReference, TEST_SYSTEM_UPDATE_USER_EMAIL);
        verify(idamService).retrieveUser(TEST_AUTHORIZATION);

    }

    @Test
    void whenGetCaseByCCDReference_thenThrowCaseNotFoundException() {
        // Given
        String ccdReference = "1234567891234567";
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkCaseExists(ccdReference)).thenReturn(false);

        // When & Then
        CaseNotFoundException exception = assertThrows(
            CaseNotFoundException.class,
            () -> cicaCaseService.getCaseByCCDReference(ccdReference, TEST_AUTHORIZATION)
        );

        // Then
        assertEquals("No case found with CCD reference: " + ccdReference, exception.getMessage());

        verify(caseDataRepository).checkCaseExists(ccdReference);
        verify(idamService).retrieveUser(TEST_AUTHORIZATION);
        verifyNoMoreInteractions(caseDataRepository);
    }

    private UserDetails userDetails() {
        return UserDetails
            .builder()
            .id(SYSTEM_USER_USER_ID)
            .email(TEST_SYSTEM_UPDATE_USER_EMAIL)
            .build();
    }

}




