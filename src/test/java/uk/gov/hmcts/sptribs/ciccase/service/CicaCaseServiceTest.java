package uk.gov.hmcts.sptribs.ciccase.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.exception.InvalidPostcodeException;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
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

    @Mock
    private CcdCaseRoleService ccdCaseRoleService;

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
    void shouldReturnCaseWhenCheckIfUserHasAccessWithPostcodeSuccessful() {
        CicaCaseEntity expectedCase = CicaCaseEntity.builder()
            .id(TEST_CASE_ID_STRING)
            .data(Map.of(
                "cicCaseAddress", new ObjectMapper().createObjectNode()
                    .put("PostCode", TEST_POSTCODE)
            ))
            .build();

        when(user.getUserInfo()).thenReturn(userInfo());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.findCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL))
            .thenReturn(Optional.of(expectedCase));

        CicaCaseEntity result = cicaCaseService.checkIfUserHasAccessWithPostcode(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, TEST_POSTCODE);

        assertThat(result).isEqualTo(expectedCase);
    }

    @Test
    void shouldThrowUnauthorisedCaseAccessExceptionWhenCheckIfUserHasAccessWithPostcodeFails() {
        when(user.getUserInfo()).thenReturn(userInfo());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.findCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL))
            .thenReturn(Optional.empty());

        assertThrows(UnauthorisedCaseAccessException.class, () ->
            cicaCaseService.checkIfUserHasAccessWithPostcode(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, TEST_POSTCODE)
        );
    }

    @Test
    void shouldThrowInvalidPostcodeExceptionWhenCheckIfUserHasAccessWithPostcodeFails() {
        CicaCaseEntity expectedCase = CicaCaseEntity.builder()
            .id(TEST_CASE_ID_STRING)
            .data(Map.of(
                "cicCaseAddress", new ObjectMapper().createObjectNode()
                    .put("PostCode", "WRONG_POSTCODE")
            ))
            .build();

        when(user.getUserInfo()).thenReturn(userInfo());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.findCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL))
            .thenReturn(Optional.of(expectedCase));

        assertThrows(InvalidPostcodeException.class, () ->
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

    @Test
    void shouldAssignCreatorRoleAfterEmailAndPostcodeValidation() {
        CicaCaseEntity expectedCase = CicaCaseEntity.builder()
            .id(TEST_CASE_ID_STRING)
            .data(Map.of(
                "cicCaseAddress", new ObjectMapper().createObjectNode()
                    .put("PostCode", TEST_POSTCODE)
            ))
            .build();

        when(user.getUserInfo()).thenReturn(userInfo());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.findCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL))
            .thenReturn(Optional.of(expectedCase));

        cicaCaseService.linkCaseToUser(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, TEST_POSTCODE);

        verify(ccdCaseRoleService).assignCreatorRole(TEST_CASE_ID_STRING, TEST_AUTHORIZATION);
    }
}

