package uk.gov.hmcts.sptribs.ciccase.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_STRING;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_POSTCODE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SYSTEM_UPDATE_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.feignException;

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
    void shouldAllowAccessToExistingCase() {
        when(user.getUserInfo()).thenReturn(userInfo());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkCaseExists(TEST_CASE_ID_STRING)).thenReturn(true);
        when(caseDataRepository.checkIfUserHasAccessToCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL))
            .thenReturn(true);

        assertDoesNotThrow(() -> cicaCaseService.checkIfUserHasAccess(TEST_CASE_ID_STRING, TEST_AUTHORIZATION));
        verify(caseDataRepository).checkCaseExists(TEST_CASE_ID_STRING);
        verify(caseDataRepository).checkIfUserHasAccessToCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL);
    }

    @Test
    void shouldThrowUnauthorisedCaseAccessExceptionWhenCheckIfUserHasAccessFails() {
        when(user.getUserInfo()).thenReturn(userInfo());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkCaseExists(TEST_CASE_ID_STRING)).thenReturn(true);

        assertThrows(UnauthorisedCaseAccessException.class, () ->
            cicaCaseService.checkIfUserHasAccess(TEST_CASE_ID_STRING, TEST_AUTHORIZATION)
        );
        verify(caseDataRepository).checkIfUserHasAccessToCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL);
    }

    @Test
    void shouldReturnNotFoundWhenCaseDoesNotExistOrIsExpired() {
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);

        assertThrows(CaseNotFoundException.class, () ->
            cicaCaseService.checkIfUserHasAccess(TEST_CASE_ID_STRING, TEST_AUTHORIZATION));
        verify(caseDataRepository, never()).checkIfUserHasAccessToCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL);
    }

    @Test
    void shouldThrowUnauthorisedCaseAccessExceptionWhenCheckIfUserHasAccessFailsDueToIdamException() {
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenThrow(new RuntimeException("IDAM down"));

        assertThatThrownBy(() -> cicaCaseService.checkIfUserHasAccess(TEST_CASE_ID_STRING, TEST_AUTHORIZATION))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Unable to check case access")
            .hasCauseInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403})
    void shouldReturnUnauthorizedForInvalidUserTokens(int idamStatus) {
        when(idamService.retrieveUser(TEST_AUTHORIZATION))
            .thenThrow(feignException(idamStatus, "Invalid user token"));

        ResponseStatusException accessError = assertThrows(ResponseStatusException.class, () ->
            cicaCaseService.checkIfUserHasAccess(TEST_CASE_ID_STRING, TEST_AUTHORIZATION));
        ResponseStatusException verificationError = assertThrows(ResponseStatusException.class, () ->
            cicaCaseService.verifySubject(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, "Name", "2000-01-01"));

        assertThat(accessError.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(verificationError.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(caseDataRepository);
    }

    @Test
    void shouldNotDiscloseIdamFailureDuringVerification() {
        when(idamService.retrieveUser(TEST_AUTHORIZATION))
            .thenThrow(feignException(500, "IDAM unavailable"));

        assertThatThrownBy(() -> cicaCaseService.verifySubject(
            TEST_CASE_ID_STRING, TEST_AUTHORIZATION, "Name", "2000-01-01"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Unable to validate user authorisation");
        assertThatThrownBy(() -> cicaCaseService.checkIfUserHasAccess(TEST_CASE_ID_STRING, TEST_AUTHORIZATION))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Unable to check case access");
        verifyNoInteractions(caseDataRepository);
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
    void shouldVerifySubjectForAssociatedDraftCase() {
        CicaCaseEntity cicaCase = CicaCaseEntity.builder().state("DSS_Draft").data(Map.of(
            "cicCaseFullName", new ObjectMapper().getNodeFactory().textNode(" Subject Name "),
            "cicCaseDateOfBirth", new ObjectMapper().getNodeFactory().textNode("2000-01-01")
        )).build();
        when(user.getUserInfo()).thenReturn(userInfo());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.findCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL))
            .thenReturn(Optional.of(cicaCase));

        assertThat(cicaCaseService.verifySubject(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, "Subject Name", "2000-01-01"))
            .isTrue();
        assertThat(cicaCaseService.verifySubject(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, "subject name", "2000-01-01"))
            .isFalse();
        assertThat(cicaCaseService.verifySubject(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, "Subject Name", "2000-01-02"))
            .isFalse();
    }

    @Test
    void shouldNotVerifyWhenCaseHasNoData() {
        when(user.getUserInfo()).thenReturn(userInfo());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.findCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL))
            .thenReturn(Optional.of(CicaCaseEntity.builder().build()));

        assertThat(cicaCaseService.verifySubject(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, "Name", "2000-01-01"))
            .isFalse();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("incompleteSubjectDetails")
    void shouldNotVerifyIncompleteSubjectDetails(String reason, Map<String, JsonNode> data) {
        when(user.getUserInfo()).thenReturn(userInfo());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.findCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL))
            .thenReturn(Optional.of(CicaCaseEntity.builder().data(data).build()));

        assertThat(cicaCaseService.verifySubject(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, "Subject Name", "2000-01-01"))
            .as(reason)
            .isFalse();
    }

    private static Stream<Arguments> incompleteSubjectDetails() {
        JsonNode name = JsonNodeFactory.instance.textNode("Subject Name");
        JsonNode dateOfBirth = JsonNodeFactory.instance.textNode("2000-01-01");
        return Stream.of(
            Arguments.of("missing name", Map.of("cicCaseDateOfBirth", dateOfBirth)),
            Arguments.of("non-text name", Map.of("cicCaseFullName", JsonNodeFactory.instance.numberNode(1),
                "cicCaseDateOfBirth", dateOfBirth)),
            Arguments.of("blank name", Map.of("cicCaseFullName", JsonNodeFactory.instance.textNode(" "),
                "cicCaseDateOfBirth", dateOfBirth)),
            Arguments.of("missing date of birth", Map.of("cicCaseFullName", name)),
            Arguments.of("non-text date of birth", Map.of("cicCaseFullName", name,
                "cicCaseDateOfBirth", JsonNodeFactory.instance.numberNode(1))),
            Arguments.of("blank date of birth", Map.of("cicCaseFullName", name,
                "cicCaseDateOfBirth", JsonNodeFactory.instance.textNode(" ")))
        );
    }

    @Test
    void shouldVerifySubjectForAssociatedExpiredCase() {
        CicaCaseEntity expiredCase = CicaCaseEntity.builder().state("DSS_Expired").data(Map.of(
            "cicCaseFullName", new ObjectMapper().getNodeFactory().textNode("Subject Name"),
            "cicCaseDateOfBirth", new ObjectMapper().getNodeFactory().textNode("2000-01-01")
        )).build();
        when(user.getUserInfo()).thenReturn(userInfo());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.findCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL))
            .thenReturn(Optional.of(expiredCase));

        assertThat(cicaCaseService.verifySubject(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, "Subject Name", "2000-01-01"))
            .isTrue();
    }

    @Test
    void shouldDenySubjectVerificationForUnassociatedUser() {
        when(user.getUserInfo()).thenReturn(userInfo());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.findCase(TEST_CASE_ID_STRING, TEST_SYSTEM_UPDATE_USER_EMAIL)).thenReturn(Optional.empty());

        assertThrows(UnauthorisedCaseAccessException.class, () ->
            cicaCaseService.verifySubject(TEST_CASE_ID_STRING, TEST_AUTHORIZATION, "Name", "2000-01-01"));
    }
}
