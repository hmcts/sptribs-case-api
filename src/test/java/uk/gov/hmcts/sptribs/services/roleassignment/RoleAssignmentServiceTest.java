package uk.gov.hmcts.sptribs.services.roleassignment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.services.roleassignment.model.RequestedRole;
import uk.gov.hmcts.sptribs.services.roleassignment.model.RoleAssignmentRequest;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_JURISDICTION;

@ExtendWith(MockitoExtension.class)
class RoleAssignmentServiceTest {

    private static final Long TEST_CASE_ID = 1234567812345678L;
    private static final String SYSTEM_TOKEN = "Bearer system-token";
    private static final String SERVICE_TOKEN = "s2s-token";
    private static final String SYSTEM_USER_ID = "system-user-id";
    private static final String ACTOR_ID = "actor-user-id";

    @Mock
    private RoleAssignmentApi roleAssignmentApi;

    @Mock
    private IdamClient idamClient;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CICUser systemUser;

    @Mock
    private UserInfo systemUserInfo;

    @Captor
    private ArgumentCaptor<RoleAssignmentRequest> requestCaptor;

    @InjectMocks
    private RoleAssignmentService roleAssignmentService;

    @Test
    void shouldAssignCaseRolesSuccessfullyForSubjectApplicantAndRepresentative() {
        CicCase cicCase = CicCase.builder()
            .subjectCIC(Set.of(SubjectCIC.SUBJECT))
            .email("subject@test.com")
            .applicantCIC(Set.of(ApplicantCIC.APPLICANT_CIC))
            .applicantEmailAddress("applicant@test.com")
            .representativeCIC(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .representativeEmailAddress("rep@test.com")
            .build();

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(SYSTEM_TOKEN);
        when(systemUser.getUserInfo()).thenReturn(systemUserInfo);
        when(systemUserInfo.getUid()).thenReturn(SYSTEM_USER_ID);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        UserDetails subjectUser = UserDetails.builder().id("subject-actor-id").build();
        UserDetails applicantUser = UserDetails.builder().id("applicant-actor-id").build();
        UserDetails repUser = UserDetails.builder().id("rep-actor-id").build();

        when(idamClient.searchUsers(SYSTEM_TOKEN, "email:\"subject@test.com\"")).thenReturn(List.of(subjectUser));
        when(idamClient.searchUsers(SYSTEM_TOKEN, "email:\"applicant@test.com\"")).thenReturn(List.of(applicantUser));
        when(idamClient.searchUsers(SYSTEM_TOKEN, "email:\"rep@test.com\"")).thenReturn(List.of(repUser));

        CaseData caseData = CaseData.builder().cicCase(cicCase).build();

        roleAssignmentService.assignCaseRoles(TEST_CASE_ID, caseData);

        verify(roleAssignmentApi, times(3)).assignRole(eq(SYSTEM_TOKEN), eq(SERVICE_TOKEN), requestCaptor.capture());

        List<RoleAssignmentRequest> capturedRequests = requestCaptor.getAllValues();
        assertThat(capturedRequests).hasSize(3);

        RoleAssignmentRequest firstRequest = capturedRequests.get(0);
        assertThat(firstRequest.getRoleRequest().getAssignerId()).isEqualTo(SYSTEM_USER_ID);
        assertThat(firstRequest.getRoleRequest().getProcess()).isEqualTo("case-allocation");
        assertThat(firstRequest.getRoleRequest().getReference()).isEqualTo(TEST_CASE_ID + "/[CREATOR]/subject-actor-id");
        assertThat(firstRequest.getRoleRequest().isReplaceExisting()).isTrue();

        RequestedRole requestedRole = firstRequest.getRequestedRoles().get(0);
        assertThat(requestedRole.getActorIdType()).isEqualTo("IDAM");
        assertThat(requestedRole.getActorId()).isEqualTo("subject-actor-id");
        assertThat(requestedRole.getRoleType()).isEqualTo("CASE");
        assertThat(requestedRole.getRoleName()).isEqualTo("[CREATOR]");
        assertThat(requestedRole.getRoleCategory()).isEqualTo("CITIZEN");
        assertThat(requestedRole.getClassification()).isEqualTo("RESTRICTED");
        assertThat(requestedRole.getGrantType()).isEqualTo("SPECIFIC");
        assertThat(requestedRole.isReadOnly()).isFalse();
        assertThat(requestedRole.getAttributes())
            .containsEntry("caseId", TEST_CASE_ID.toString())
            .containsEntry("caseType", ST_CIC_CASE_TYPE)
            .containsEntry("jurisdiction", ST_CIC_JURISDICTION)
            .containsEntry("substantive", "Y");
    }

    @Test
    void shouldSkipRoleAssignmentWhenNoEmailsPresent() {
        CaseData caseData = CaseData.builder().cicCase(CicCase.builder().build()).build();

        roleAssignmentService.assignCaseRoles(TEST_CASE_ID, caseData);

        verifyNoInteractions(idamService, idamClient, authTokenGenerator, roleAssignmentApi);
    }

    @Test
    void shouldSkipRoleAssignmentWhenSearchUsersReturnsEmptyOrNull() {
        CicCase cicCase = CicCase.builder()
            .subjectCIC(Set.of(SubjectCIC.SUBJECT))
            .email("notfound@test.com")
            .build();

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(SYSTEM_TOKEN);
        when(systemUser.getUserInfo()).thenReturn(systemUserInfo);
        when(systemUserInfo.getUid()).thenReturn(SYSTEM_USER_ID);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(idamClient.searchUsers(SYSTEM_TOKEN, "email:\"notfound@test.com\"")).thenReturn(Collections.emptyList());

        CaseData caseData = CaseData.builder().cicCase(cicCase).build();

        roleAssignmentService.assignCaseRoles(TEST_CASE_ID, caseData);

        verify(roleAssignmentApi, never()).assignRole(anyString(), anyString(), any());
    }

    @Test
    void shouldHandleExceptionWhenAssignCaseRolesFails() {
        CicCase cicCase = CicCase.builder()
            .subjectCIC(Set.of(SubjectCIC.SUBJECT))
            .email("subject@test.com")
            .build();
        CaseData caseData = CaseData.builder().cicCase(cicCase).build();

        when(idamService.retrieveSystemUpdateUserDetails()).thenThrow(new RuntimeException("IDAM service unavailable"));

        assertDoesNotThrow(() -> roleAssignmentService.assignCaseRoles(TEST_CASE_ID, caseData));
    }

    @Test
    void shouldAssignCaseRoleForActorSuccessfully() {
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(SYSTEM_TOKEN);
        when(systemUser.getUserInfo()).thenReturn(systemUserInfo);
        when(systemUserInfo.getUid()).thenReturn(SYSTEM_USER_ID);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        roleAssignmentService.assignCaseRoleForActor(TEST_CASE_ID, ACTOR_ID);

        verify(roleAssignmentApi).assignRole(eq(SYSTEM_TOKEN), eq(SERVICE_TOKEN), requestCaptor.capture());

        RoleAssignmentRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getRoleRequest().getAssignerId()).isEqualTo(SYSTEM_USER_ID);
        assertThat(capturedRequest.getRoleRequest().getProcess()).isEqualTo("case-allocation");
        assertThat(capturedRequest.getRoleRequest().getReference()).isEqualTo(TEST_CASE_ID + "/[CREATOR]/" + ACTOR_ID);

        RequestedRole requestedRole = capturedRequest.getRequestedRoles().get(0);
        assertThat(requestedRole.getActorId()).isEqualTo(ACTOR_ID);
        assertThat(requestedRole.getAttributes())
            .containsEntry("caseId", TEST_CASE_ID.toString())
            .containsEntry("caseType", ST_CIC_CASE_TYPE)
            .containsEntry("jurisdiction", ST_CIC_JURISDICTION)
            .containsEntry("substantive", "Y");
    }

    @Test
    void shouldHandleExceptionWhenAssignCaseRoleForActorFails() {
        when(idamService.retrieveSystemUpdateUserDetails()).thenThrow(new RuntimeException("Service error"));

        assertDoesNotThrow(() -> roleAssignmentService.assignCaseRoleForActor(TEST_CASE_ID, ACTOR_ID));
    }
}
