package uk.gov.hmcts.sptribs.ciccase.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
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
    private final AuthTokenGenerator authTokenGenerator;

    public void assignCreatorRole(String ccdReference, String userId) {
        log.info("assignCreatorRole called for case {} and user {}", ccdReference, userId);
        try {
            log.info("Retrieving system update user details");
            CICUser systemUser = idamService.retrieveSystemUpdateUserDetails();
            log.info("Successfully retrieved system update user details");

            log.info("Building creator role payload for case {} and user {}", ccdReference, userId);
            CaseAssignmentUserRoleWithOrganisation creatorRole =
                CaseAssignmentUserRoleWithOrganisation.builder()
                    .caseDataId(ccdReference)
                    .userId(userId)
                    .caseRole(CREATOR_ROLE)
                    .build();

            log.info("Building case assignment request for case {}", ccdReference);
            CaseAssignmentUserRolesRequest request = CaseAssignmentUserRolesRequest.builder()
                .caseAssignmentUserRolesWithOrganisation(List.of(creatorRole))
                .build();

            log.info("Calling CCD addCaseUserRoles for case {} and user {}", ccdReference, userId);
            caseAssignmentApi.addCaseUserRoles(
                systemUser.getAuthToken(),
                authTokenGenerator.generate(),
                request
            );

            log.info("Assigned {} case role for case {} and user {}", CREATOR_ROLE, ccdReference, userId);
        } catch (Exception ex) {
            log.error(
                "Failed to assign {} case role for case {} and user {}",
                CREATOR_ROLE,
                ccdReference,
                userId,
                ex
            );
            throw ex;
        }
    }
}
