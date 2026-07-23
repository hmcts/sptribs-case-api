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
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_STRING;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_POSTCODE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SYSTEM_UPDATE_USER_EMAIL;

@ExtendWith(MockitoExtension.class)
class CicaCaseServiceTest {

    private static final String TEST_AUTHORIZATION = "Bearer test-token";

    @Mock
    private CaseDataRepository caseDataRepository;

    @Mock
    private IdamService idamService;

    @Mock
    private CICUser user;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CicaCaseService cicaCaseService;

    private UserInfo userInfo() {
        return UserInfo
            .builder()
            .uid(SYSTEM_USER_USER_ID)
            .sub(TEST_SYSTEM_UPDATE_USER_EMAIL)
            .build();
    }

    @Test
    void shouldNotThrowExceptionWhenCheckIfUserHasAccessSuccessful() {
        when(user.getUserInfo()).thenReturn(userInfo());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkCaseExists(TEST_CASE_ID_STRING)).thenReturn(true);
        when(caseDataRepository.checkIfUserHasAccessToCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL)).thenReturn(true);

        assertDoesNotThrow(() -> cicaCaseService.checkIfUserHasAccess(TEST_CASE_ID_STRING, TEST_AUTHORIZATION));
    }

    @Test
    void shouldThrowUnauthorisedCaseAccessExceptionWhenCheckIfUserHasAccessFails() {
        when(user.getUserInfo()).thenReturn(userInfo());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkCaseExists(TEST_CASE_ID_STRING)).thenReturn(true);
        when(caseDataRepository.checkIfUserHasAccessToCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL)).thenReturn(false);

        assertThrows(UnauthorisedCaseAccessException.class, () ->
            cicaCaseService.checkIfUserHasAccess(TEST_CASE_ID_STRING, TEST_AUTHORIZATION)
        );
    }

    @Test
    void shouldThrowCaseNotFoundExceptionWhenCheckIfUserHasAccessFailsOnNonExistentCase() {
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkCaseExists(TEST_CASE_ID_STRING)).thenReturn(false);

        assertThrows(CaseNotFoundException.class, () ->
            cicaCaseService.checkIfUserHasAccess(TEST_CASE_ID_STRING, TEST_AUTHORIZATION)
        );
    }

    @Test
    void shouldThrowUnauthorisedCaseAccessExceptionWhenCheckIfUserHasAccessFailsDueToIdamException() {
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenThrow(new RuntimeException("IDAM down"));

        assertThrows(UnauthorisedCaseAccessException.class, () ->
            cicaCaseService.checkIfUserHasAccess(TEST_CASE_ID_STRING, TEST_AUTHORIZATION)
        );
    }

    @Test
    void shouldNotThrowExceptionWhenCheckIfUserHasAccessWithPostcodeSuccessful() {
        when(user.getUserInfo()).thenReturn(userInfo());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkIfUserHasAccessToCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL, TEST_POSTCODE))
            .thenReturn(true);

        assertDoesNotThrow(() -> cicaCaseService.checkIfUserHasAccessWithPostcode(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, TEST_POSTCODE));
    }

    @Test
    void shouldThrowUnauthorisedCaseAccessExceptionWhenCheckIfUserHasAccessWithPostcodeFails() {
        when(user.getUserInfo()).thenReturn(userInfo());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkIfUserHasAccessToCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL, TEST_POSTCODE))
            .thenReturn(false);

        assertThrows(UnauthorisedCaseAccessException.class, () ->
            cicaCaseService.checkIfUserHasAccessWithPostcode(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, TEST_POSTCODE)
        );
    }

    @Test
    void shouldThrowUnauthorisedCaseAccessExceptionWhenCheckIfUserHasAccessWithPostcodeFailsDueToIdamException() {
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenThrow(new RuntimeException("IDAM down"));

        assertThrows(UnauthorisedCaseAccessException.class, () ->
            cicaCaseService.checkIfUserHasAccessWithPostcode(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, TEST_POSTCODE)
        );
    }
}




