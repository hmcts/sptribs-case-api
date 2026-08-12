package uk.gov.hmcts.sptribs.ciccase.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_STRING;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class CcdCaseRoleServiceTest {

    private static final String USER_EMAIL = "citizen@example.com";
    private static final String USER_ID = "citizen-user-id";
    private static final String SYSTEM_TOKEN = "Bearer system-token";

    @Mock
    private CaseAssignmentApi caseAssignmentApi;

    @Mock
    private IdamService idamService;

    @Mock
    private IdamClient idamClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private CcdCaseRoleService ccdCaseRoleService;

    @Test
    void shouldAssignCreatorRoleViaCcdDataStore() {
        when(idamService.retrieveSystemUpdateUserDetails())
            .thenReturn(new CICUser(SYSTEM_TOKEN, UserInfo.builder().build()));
        when(idamClient.searchUsers(SYSTEM_TOKEN, "email:\"" + USER_EMAIL + "\""))
            .thenReturn(List.of(UserDetails.builder().id(USER_ID).build()));
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        ccdCaseRoleService.assignCreatorRole(TEST_CASE_ID_STRING, USER_EMAIL);

        ArgumentCaptor<CaseAssignmentUserRolesRequest> requestCaptor =
            ArgumentCaptor.forClass(CaseAssignmentUserRolesRequest.class);
        verify(caseAssignmentApi).addCaseUserRoles(
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
}
