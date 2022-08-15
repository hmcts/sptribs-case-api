package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.ciccase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static uk.gov.hmcts.divorce.testutil.TestConstants.APP_1_SOL_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOLICITOR_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(SpringExtension.class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CcdAccessServiceIT {

    @Autowired
    private CcdAccessService ccdAccessService;

    @MockBean
    private CaseAssignmentApi caseAssignmentApi;

    @MockBean
    private IdamService idamService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    private CaseAssignmentUserRolesRequest getCaseAssignmentUserRolesRequest(String orgId, UserRole role, String userId) {
        return CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(
                List.of(
                    CaseAssignmentUserRoleWithOrganisation.builder()
                        .organisationId(orgId)
                        .caseDataId(String.valueOf(TEST_CASE_ID))
                        .caseRole(role.getRole())
                        .userId(userId)
                        .build()
                )
            )
            .build();
    }

    private User caseworkerUser() {
        return getUser(CASEWORKER_AUTH_TOKEN, CASEWORKER_USER_ID);
    }

    private User solicitorUser() {
        return getUser(APP_1_SOL_AUTH_TOKEN, SOLICITOR_USER_ID);
    }

    private User systemUpdateUser() {
        return getUser(SYSTEM_UPDATE_AUTH_TOKEN, SYSTEM_USER_USER_ID);
    }

    private User getUser(String systemUpdateAuthToken, String systemUserUserId) {
        return new User(
            systemUpdateAuthToken,
            UserDetails.builder()
                .id(systemUserUserId)
                .build());
    }
}
