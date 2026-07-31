package uk.gov.hmcts.sptribs.services.roleassignment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.services.roleassignment.model.RequestedRole;
import uk.gov.hmcts.sptribs.services.roleassignment.model.RoleAssignmentRequest;
import uk.gov.hmcts.sptribs.services.roleassignment.model.RoleRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleAssignmentService {

    private final RoleAssignmentApi roleAssignmentApi;
    private final IdamClient idamClient;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;

    public void assignCaseRoles(Long caseId, CaseData caseData) {
        final CicCase cicCase = caseData.getCicCase();
        final List<String> emails = new ArrayList<>();

        if (isNotEmpty(cicCase.getSubjectCIC()) && cicCase.getEmail() != null) {
            emails.add(cicCase.getEmail());
        }

        if (isNotEmpty(cicCase.getApplicantCIC()) && cicCase.getApplicantEmailAddress() != null) {
            emails.add(cicCase.getApplicantEmailAddress());
        }

        if (isNotEmpty(cicCase.getRepresentativeCIC()) && cicCase.getRepresentativeEmailAddress() != null) {
            emails.add(cicCase.getRepresentativeEmailAddress());
        }

        if (emails.isEmpty()) {
            log.info("No citizen/subject/applicant/representative emails to assign roles for case ID: {}", caseId);
            return;
        }

        try {
            final CICUser systemUser = idamService.retrieveSystemUpdateUserDetails();
            final String systemToken = systemUser.getAuthToken();
            final String systemUserId = systemUser.getUserInfo().getUid();
            final String serviceAuthToken = authTokenGenerator.generate();

            for (String email : emails) {
                log.info("Searching IDAM for user with email: {}", email);
                final List<UserDetails> users = idamClient.searchUsers(systemToken, "email:\"" + email + "\"");
                if (users != null && !users.isEmpty()) {
                    final String actorId = users.get(0).getId();
                    log.info("Found IDAM user with id: {} for email: {}. Assigning [CREATOR] role.", actorId, email);

                    assignRoleForUser(caseId, actorId, systemUserId, systemToken, serviceAuthToken);
                } else {
                    log.warn("No IDAM user found for email: {}, role assignment skipped.", email);
                }
            }
        } catch (Exception e) {
            log.error("Failed to assign case roles for case ID: {} with exception: {}", caseId, e.getMessage(), e);
        }
    }

    public void assignCaseRoleForActor(Long caseId, String actorId) {
        try {
            final CICUser systemUser = idamService.retrieveSystemUpdateUserDetails();
            final String systemToken = systemUser.getAuthToken();
            final String systemUserId = systemUser.getUserInfo().getUid();
            final String serviceAuthToken = authTokenGenerator.generate();

            assignRoleForUser(caseId, actorId, systemUserId, systemToken, serviceAuthToken);
        } catch (Exception e) {
            log.error("Failed to dynamically assign role for actor: {} on case ID: {} with exception: {}", actorId, caseId, e.getMessage());
        }
    }

    private void assignRoleForUser(Long caseId, String actorId, String assignerId, String authToken, String serviceAuthToken) {
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("caseId", caseId.toString());
        attributes.put("caseType", "CriminalInjuriesCompensation");
        attributes.put("jurisdiction", "ST_CIC");

        final RoleRequest roleRequest = RoleRequest.builder()
            .assignerId(assignerId)
            .process("case-allocation")
            .reference(caseId + "/[CREATOR]")
            .replaceExisting(true)
            .build();

        final RequestedRole requestedRole = RequestedRole.builder()
            .actorIdType("IDAM")
            .actorId(actorId)
            .roleType("CASE")
            .roleName("[CREATOR]")
            .roleCategory("CITIZEN")
            .classification("RESTRICTED")
            .grantType("SPECIFIC")
            .readOnly(false)
            .attributes(attributes)
            .build();

        final RoleAssignmentRequest request = RoleAssignmentRequest.builder()
            .roleRequest(roleRequest)
            .requestedRoles(List.of(requestedRole))
            .build();

        roleAssignmentApi.assignRole(authToken, serviceAuthToken, request);
        log.info("Successfully assigned [CREATOR] role to user: {} on case: {}", actorId, caseId);
    }
}
