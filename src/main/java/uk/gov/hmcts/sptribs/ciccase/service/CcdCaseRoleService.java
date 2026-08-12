package uk.gov.hmcts.sptribs.ciccase.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CcdCaseRoleService {

    private static final String CREATOR_ROLE = "[CREATOR]";

    private final CaseAssignmentApi caseAssignmentApi;
    private final IdamService idamService;
    private final IdamClient idamClient;
    private final AuthTokenGenerator authTokenGenerator;

    public void assignCreatorRole(String ccdReference, String userEmail) {
        log.info("assignCreatorRole called for case {} and email {}", ccdReference, userEmail);
        try {
            CICUser systemUser = idamService.retrieveSystemUpdateUserDetails();
            String actorId = getActorIdForEmail(systemUser.getAuthToken(), userEmail);
            log.info("Building creator role payload for case {} and actorId {}", ccdReference, actorId);
            CaseAssignmentUserRoleWithOrganisation creatorRole =
                CaseAssignmentUserRoleWithOrganisation.builder()
                    .caseDataId(ccdReference)
                    .userId(actorId)
                    .caseRole(CREATOR_ROLE)
                    .build();

            log.info("Calling CCD addCaseUserRoles for case {} and actorId {}", ccdReference, actorId);
            log.info("Building case assignment request for case {}", ccdReference);
            CaseAssignmentUserRolesRequest request = CaseAssignmentUserRolesRequest.builder()
                .caseAssignmentUserRolesWithOrganisation(List.of(creatorRole))
                .build();
            caseAssignmentApi.addCaseUserRoles(
                systemUser.getAuthToken(),
                authTokenGenerator.generate(),
                request
            );

            log.info("Assigned {} case role for case {} and actorId {}", CREATOR_ROLE, ccdReference, actorId);
        } catch (Exception ex) {
            log.error(
                "Failed to assign {} case role for case {} and email {}",
                CREATOR_ROLE,
                ccdReference,
                userEmail,
                ex
            );
            throw ex;
        }
    }

    private String getActorIdForEmail(String systemToken, String userEmail) {
        List<UserDetails> users = idamClient.searchUsers(systemToken, "email:\"" + userEmail + "\"");
        if (users == null || users.isEmpty() || users.getFirst().getId() == null) {
            throw new IllegalStateException("Unable to resolve IDAM actor id for email: " + userEmail);
        }
        return users.getFirst().getId();
    }
}
