package uk.gov.hmcts.sptribs.ciccase.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CicaRoleService {

    private static final String CREATOR_ROLE = "[CREATOR]";

    private final CaseAssignmentApi caseAssignmentApi;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;

    /**
     * Ensures the authenticated citizen has the creator role. Callers must verify case membership first;
     * an assigned role does not replace downstream permission checks with the citizen's token.
     */
    public void ensureCreatorRole(String ccdReference, String userAuthToken) {
        CICUser user = idamService.retrieveUser(userAuthToken);
        if (user == null || user.getUserInfo() == null || user.getUserInfo().getUid() == null
            || user.getUserInfo().getUid().isBlank()) {
            throw new UnauthorisedCaseAccessException("Unable to determine citizen identity");
        }
        String actorId = user.getUserInfo().getUid();
        String systemToken = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        String serviceToken = authTokenGenerator.generate();

        if (hasCreatorRole(ccdReference, actorId, systemToken, serviceToken)) {
            return;
        }

        CaseAssignmentUserRoleWithOrganisation creatorRole = CaseAssignmentUserRoleWithOrganisation.builder()
            .caseDataId(ccdReference)
            .userId(actorId)
            .caseRole(CREATOR_ROLE)
            .build();
        CaseAssignmentUserRolesRequest request = CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(List.of(creatorRole))
            .build();

        try {
            caseAssignmentApi.addCaseUserRoles(systemToken, serviceToken, request);
        } catch (FeignException assignmentFailure) {
            // Do not assume a particular status means a duplicate: confirm the role exists after a race or timeout.
            if (!hasCreatorRole(ccdReference, actorId, systemToken, serviceToken)) {
                throw assignmentFailure;
            }
        }
        log.info("event=citizen_creator_role_ensured outcome=success");
    }

    private boolean hasCreatorRole(String ccdReference, String actorId, String systemToken, String serviceToken) {
        var response = caseAssignmentApi.getUserRoles(systemToken, serviceToken, ccdReference, actorId);
        if (response == null || response.getCaseAssignmentUserRoles() == null) {
            throw new IllegalStateException("CCD returned no case role information");
        }
        return response.getCaseAssignmentUserRoles().stream().anyMatch(role ->
            ccdReference.equals(role.getCaseDataId())
                && actorId.equals(role.getUserId())
                && CREATOR_ROLE.equals(role.getCaseRole()));
    }
}
