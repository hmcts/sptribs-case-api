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
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
        when(user.getUserDetails()).thenReturn(userDetails());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkCaseExists(TEST_CASE_ID_STRING)).thenReturn(true);
        when(caseDataRepository.checkIfUserHasAccessToCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL)).thenReturn(true);

        assertDoesNotThrow(() -> cicaCaseService.checkIfUserHasAccess(TEST_CASE_ID_STRING, TEST_AUTHORIZATION));
    }

    @Test
    void shouldThrowUnauthorisedCaseAccessExceptionWhenCheckIfUserHasAccessFails() {
        when(user.getUserDetails()).thenReturn(userDetails());
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
    void shouldReturnPartyWhenVerifyUserAccessAndGetPartySuccessful() {
        CicaCaseEntity cicaCaseEntity = CicaCaseEntity.builder()
            .data(new HashMap<>())
            .build();

        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder().email(TEST_SYSTEM_UPDATE_USER_EMAIL).build())
            .build();

        when(user.getUserDetails()).thenReturn(userDetails());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.findCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL, TEST_POSTCODE))
            .thenReturn(Optional.of(cicaCaseEntity));
        when(objectMapper.convertValue(cicaCaseEntity.getData(), CaseData.class)).thenReturn(caseData);

        Party result = cicaCaseService.verifyUserAccessAndGetParty(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, TEST_POSTCODE);

        assertThat(result).isEqualTo(Party.SUBJECT);
    }

    @Test
    void shouldThrowUnauthorisedCaseAccessExceptionWhenVerifyUserAccessAndGetPartyFailsPostcode() {
        when(user.getUserDetails()).thenReturn(userDetails());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.findCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL, TEST_POSTCODE))
            .thenReturn(Optional.empty());

        assertThrows(UnauthorisedCaseAccessException.class, () ->
            cicaCaseService.verifyUserAccessAndGetParty(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, TEST_POSTCODE)
        );
    }

    @Test
    void shouldThrowUnauthorisedCaseAccessExceptionWhenVerifyUserAccessAndGetPartyFailsNoMatchingParty() {
        CicaCaseEntity cicaCaseEntity = CicaCaseEntity.builder()
            .data(new HashMap<>())
            .build();

        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder().email("stranger@test.com").build())
            .build();

        when(user.getUserDetails()).thenReturn(userDetails());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.findCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL, TEST_POSTCODE))
            .thenReturn(Optional.of(cicaCaseEntity));
        when(objectMapper.convertValue(cicaCaseEntity.getData(), CaseData.class)).thenReturn(caseData);

        assertThrows(UnauthorisedCaseAccessException.class, () ->
            cicaCaseService.verifyUserAccessAndGetParty(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, TEST_POSTCODE)
        );
    }

    @Test
    void shouldThrowUnauthorisedCaseAccessExceptionWhenVerifyUserAccessAndGetPartyFailsDueToIdamException() {
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenThrow(new RuntimeException("IDAM down"));

        assertThrows(UnauthorisedCaseAccessException.class, () ->
            cicaCaseService.verifyUserAccessAndGetParty(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, TEST_POSTCODE)
        );
    }
}




