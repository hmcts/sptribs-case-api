package uk.gov.hmcts.sptribs.ciccase.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.idam.IdamService;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;
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

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CicaCaseService cicaCaseService;

    private UserDetails userDetails() {
        return UserDetails
            .builder()
            .id(SYSTEM_USER_USER_ID)
            .email(TEST_SYSTEM_UPDATE_USER_EMAIL)
            .build();
    }

    @Test
    void shouldNotThrowExceptionWhenCheckIfUserHasAccessSuccessful() {
        String ccdReference = "1234567891234567";

        when(user.getUserDetails()).thenReturn(userDetails());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkCaseExists(ccdReference)).thenReturn(true);
        when(caseDataRepository.checkIfUserHasAccessToCase(ccdReference, TEST_SYSTEM_UPDATE_USER_EMAIL)).thenReturn(true);

        cicaCaseService.checkIfUserHasAccess(ccdReference, TEST_AUTHORIZATION);
    }

    @Test
    void shouldThrowUnauthorisedCaseAccessExceptionWhenCheckIfUserHasAccessFails() {
        String ccdReference = "1234567891234567";

        when(user.getUserDetails()).thenReturn(userDetails());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkCaseExists(ccdReference)).thenReturn(true);
        when(caseDataRepository.checkIfUserHasAccessToCase(ccdReference, TEST_SYSTEM_UPDATE_USER_EMAIL)).thenReturn(false);

        assertThrows(UnauthorisedCaseAccessException.class, () ->
            cicaCaseService.checkIfUserHasAccess(ccdReference, TEST_AUTHORIZATION)
        );
    }

    @Test
    void shouldThrowCaseNotFoundExceptionWhenCheckIfUserHasAccessFailsOnNonExistentCase() {
        String ccdReference = "1234567891234567";

        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkCaseExists(ccdReference)).thenReturn(false);

        assertThrows(CaseNotFoundException.class, () ->
            cicaCaseService.checkIfUserHasAccess(ccdReference, TEST_AUTHORIZATION)
        );
    }

    @Test
    void shouldThrowUnauthorisedCaseAccessExceptionWhenCheckIfUserHasAccessException() {
        String ccdReference = "1234567891234567";

        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenThrow(new RuntimeException("IDAM down"));

        assertThrows(UnauthorisedCaseAccessException.class, () ->
            cicaCaseService.checkIfUserHasAccess(ccdReference, TEST_AUTHORIZATION)
        );
    }

    @Test
    void shouldNotThrowExceptionWhenCheckIfUserHasAccessWithPostcodeSuccessful() {
        String ccdReference = "1234567891234567";
        String postcode = "SW11 1PD";

        when(user.getUserDetails()).thenReturn(userDetails());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkIfUserHasAccessToCase(ccdReference, TEST_SYSTEM_UPDATE_USER_EMAIL, postcode)).thenReturn(true);

        cicaCaseService.checkIfUserHasAccessWithPostcode(ccdReference, TEST_AUTHORIZATION, postcode);
    }

    @Test
    void shouldThrowUnauthorisedCaseAccessExceptionWhenCheckIfUserHasAccessWithPostcodeFails() {
        String ccdReference = "1234567891234567";
        String postcode = "SW11 1PD";

        when(user.getUserDetails()).thenReturn(userDetails());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkIfUserHasAccessToCase(ccdReference, TEST_SYSTEM_UPDATE_USER_EMAIL, postcode)).thenReturn(false);

        assertThrows(UnauthorisedCaseAccessException.class, () ->
            cicaCaseService.checkIfUserHasAccessWithPostcode(ccdReference, TEST_AUTHORIZATION, postcode)
        );
    }
}




