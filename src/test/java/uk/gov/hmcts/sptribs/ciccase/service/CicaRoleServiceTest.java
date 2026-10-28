package uk.gov.hmcts.sptribs.ciccase.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_STRING;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class CicaRoleServiceTest {

    private static final String USER_AUTH_TOKEN = "Bearer citizen-token";
    private static final String USER_ID = "citizen-user-id";
    private static final String SYSTEM_TOKEN = "Bearer system-token";

    @Mock
    private CaseAssignmentApi caseAssignmentApi;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private CicaRoleService cicaRoleService;

    @BeforeEach
    void setUpCitizen() {
        when(idamService.retrieveUser(USER_AUTH_TOKEN))
            .thenReturn(new CICUser(USER_AUTH_TOKEN, UserInfo.builder().uid(USER_ID).build()));
    }

    private void setUpRoleLookup() {
        when(idamService.retrieveSystemUpdateUserDetails())
            .thenReturn(new CICUser(SYSTEM_TOKEN, UserInfo.builder().build()));
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
    }

    @Test
    void shouldAssignCreatorRoleViaCcdDataStoreOnlyAfterCheckingRoles() {
        setUpRoleLookup();
        when(caseAssignmentApi.getUserRoles(SYSTEM_TOKEN, TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID_STRING, USER_ID))
            .thenReturn(roles());

        cicaRoleService.ensureCreatorRole(TEST_CASE_ID_STRING, USER_AUTH_TOKEN);

        ArgumentCaptor<CaseAssignmentUserRolesRequest> requestCaptor =
            ArgumentCaptor.forClass(CaseAssignmentUserRolesRequest.class);
        var calls = inOrder(caseAssignmentApi);
        calls.verify(caseAssignmentApi).getUserRoles(SYSTEM_TOKEN, TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID_STRING, USER_ID);
        calls.verify(caseAssignmentApi).addCaseUserRoles(
            org.mockito.ArgumentMatchers.eq(SYSTEM_TOKEN),
            org.mockito.ArgumentMatchers.eq(TEST_SERVICE_AUTH_TOKEN),
            requestCaptor.capture()
        );

        var assignedRole = requestCaptor.getValue().getCaseAssignmentUserRolesWithOrganisation().getFirst();
        assertThat(assignedRole.getCaseDataId()).isEqualTo(TEST_CASE_ID_STRING);
        assertThat(assignedRole.getUserId()).isEqualTo(USER_ID);
        assertThat(assignedRole.getCaseRole()).isEqualTo("[CREATOR]");
        assertThat(assignedRole.getOrganisationId()).isNull();
    }

    @Test
    void shouldNotAssignCreatorRoleWhenAlreadyPresent() {
        setUpRoleLookup();
        when(caseAssignmentApi.getUserRoles(SYSTEM_TOKEN, TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID_STRING, USER_ID))
            .thenReturn(roles(creatorRole(TEST_CASE_ID_STRING, USER_ID)));

        cicaRoleService.ensureCreatorRole(TEST_CASE_ID_STRING, USER_AUTH_TOKEN);

        verify(caseAssignmentApi, never()).addCaseUserRoles(anyString(), anyString(), any());
    }

    @Test
    void shouldNotConfuseAnotherCaseOrCitizensRoleWithTheRequestedRole() {
        setUpRoleLookup();
        when(caseAssignmentApi.getUserRoles(SYSTEM_TOKEN, TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID_STRING, USER_ID))
            .thenReturn(roles(creatorRole("9999999999999999", USER_ID),
                creatorRole(TEST_CASE_ID_STRING, "another-citizen"),
                CaseAssignmentUserRole.builder().caseDataId(TEST_CASE_ID_STRING).userId(USER_ID)
                    .caseRole("[OTHER]").build()));

        cicaRoleService.ensureCreatorRole(TEST_CASE_ID_STRING, USER_AUTH_TOKEN);

        verify(caseAssignmentApi).addCaseUserRoles(anyString(), anyString(), any());
    }

    @Test
    void shouldNotAssignWhenRoleLookupFails() {
        setUpRoleLookup();
        FeignException lookupFailure = mock(FeignException.class);
        when(caseAssignmentApi.getUserRoles(SYSTEM_TOKEN, TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID_STRING, USER_ID))
            .thenThrow(lookupFailure);

        assertThatThrownBy(() -> cicaRoleService.ensureCreatorRole(TEST_CASE_ID_STRING, USER_AUTH_TOKEN))
            .isSameAs(lookupFailure);

        verify(caseAssignmentApi, never()).addCaseUserRoles(anyString(), anyString(), any());
    }

    @Test
    void shouldNotTreatMissingRoleInformationAsAnEmptyList() {
        setUpRoleLookup();
        when(caseAssignmentApi.getUserRoles(SYSTEM_TOKEN, TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID_STRING, USER_ID))
            .thenReturn(null, CaseAssignmentUserRolesResource.builder().build());

        for (int attempt = 0; attempt < 2; attempt++) {
            assertThatThrownBy(() -> cicaRoleService.ensureCreatorRole(TEST_CASE_ID_STRING, USER_AUTH_TOKEN))
                .isInstanceOf(IllegalStateException.class);
        }

        verify(caseAssignmentApi, never()).addCaseUserRoles(anyString(), anyString(), any());
    }

    @Test
    void shouldAcceptConcurrentAssignmentOnlyAfterConfirmingTheRoleExists() {
        setUpRoleLookup();
        FeignException assignmentFailure = mock(FeignException.class);
        when(caseAssignmentApi.getUserRoles(SYSTEM_TOKEN, TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID_STRING, USER_ID))
            .thenReturn(roles(), roles(creatorRole(TEST_CASE_ID_STRING, USER_ID)));
        doThrow(assignmentFailure).when(caseAssignmentApi).addCaseUserRoles(anyString(), anyString(), any());

        cicaRoleService.ensureCreatorRole(TEST_CASE_ID_STRING, USER_AUTH_TOKEN);

        verify(caseAssignmentApi, times(2))
            .getUserRoles(SYSTEM_TOKEN, TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID_STRING, USER_ID);
        verify(caseAssignmentApi).addCaseUserRoles(anyString(), anyString(), any());
    }

    @Test
    void shouldPropagateAssignmentFailureWhenRoleRemainsAbsent() {
        setUpRoleLookup();
        FeignException assignmentFailure = mock(FeignException.class);
        when(caseAssignmentApi.getUserRoles(SYSTEM_TOKEN, TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID_STRING, USER_ID))
            .thenReturn(roles());
        doThrow(assignmentFailure).when(caseAssignmentApi).addCaseUserRoles(anyString(), anyString(), any());

        assertThatThrownBy(() -> cicaRoleService.ensureCreatorRole(TEST_CASE_ID_STRING, USER_AUTH_TOKEN))
            .isSameAs(assignmentFailure);
    }

    @Test
    void shouldFailClosedIfRoleCannotBeConfirmedAfterAssignmentFailure() {
        setUpRoleLookup();
        FeignException lookupFailure = mock(FeignException.class);
        when(caseAssignmentApi.getUserRoles(SYSTEM_TOKEN, TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID_STRING, USER_ID))
            .thenReturn(roles()).thenThrow(lookupFailure);
        doThrow(mock(FeignException.class)).when(caseAssignmentApi).addCaseUserRoles(anyString(), anyString(), any());

        assertThatThrownBy(() -> cicaRoleService.ensureCreatorRole(TEST_CASE_ID_STRING, USER_AUTH_TOKEN))
            .isSameAs(lookupFailure);
    }

    @Test
    void shouldRejectMissingCitizenIdentityBeforeQueryingRoles() {
        when(idamService.retrieveUser(USER_AUTH_TOKEN))
            .thenReturn(null, new CICUser(USER_AUTH_TOKEN, null),
                new CICUser(USER_AUTH_TOKEN, UserInfo.builder().build()),
                new CICUser(USER_AUTH_TOKEN, UserInfo.builder().uid(" ").build()));

        for (int attempt = 0; attempt < 4; attempt++) {
            assertThatThrownBy(() -> cicaRoleService.ensureCreatorRole(TEST_CASE_ID_STRING, USER_AUTH_TOKEN))
                .isInstanceOf(UnauthorisedCaseAccessException.class);
        }

        verifyNoInteractions(caseAssignmentApi, authTokenGenerator);
        verify(idamService, never()).retrieveSystemUpdateUserDetails();
    }

    private CaseAssignmentUserRolesResource roles(CaseAssignmentUserRole... roles) {
        return CaseAssignmentUserRolesResource.builder().caseAssignmentUserRoles(List.of(roles)).build();
    }

    private CaseAssignmentUserRole creatorRole(String caseId, String citizenId) {
        return CaseAssignmentUserRole.builder().caseDataId(caseId).userId(citizenId).caseRole("[CREATOR]").build();
    }
}
