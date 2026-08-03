package uk.gov.hmcts.sptribs.ciccase.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.exception.InvalidPostcodeException;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.services.roleassignment.RoleAssignmentService;

@Slf4j
@Service
@RequiredArgsConstructor
public class CicaCaseService {

    private final CaseDataRepository caseDataRepository;
    private final IdamService idamService;
    private final RoleAssignmentService roleAssignmentService;

    /**
     * Checks if the user has access to the case.
     *
     * @param ccdReference  the CCD reference number.
     * @param authorisation user authorization token.
     * @throws CaseNotFoundException if the case does not exist
     * @throws UnauthorisedCaseAccessException if the user does not have access or validation fails.
     */
    public void checkIfUserHasAccess(String ccdReference, String authorisation) {
        try {
            CICUser user = idamService.retrieveUser(authorisation);
            if (!caseDataRepository.checkCaseExists(ccdReference)) {
                throw new CaseNotFoundException("No case found with CCD reference: " + ccdReference);
            }
            boolean hasAccess = caseDataRepository.checkIfUserHasAccessToCase(ccdReference, user.getUserInfo().getSub());
            if (!hasAccess) {
                throw new UnauthorisedCaseAccessException("User is not authorised to access case: " + ccdReference);
            }
        } catch (Exception e) {
            switch (e) {
                case CaseNotFoundException cnfe -> throw cnfe;
                case UnauthorisedCaseAccessException ucae -> throw ucae;
                default -> {
                    log.warn("Error checking case access for reference: {}", ccdReference, e);
                    throw new UnauthorisedCaseAccessException("Error checking case access: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Checks if the user has access to the case and the postcode matches.
     *
     * @param ccdReference      the CCD reference number.
     * @param authorisation     user authorization token.
     * @param submittedPostcode the postcode submitted by the user.
     * @return the CicaCaseEntity.
     * @throws UnauthorisedCaseAccessException if the user does not have access, postcode does not match, or validation fails.
     */
    public CicaCaseEntity checkIfUserHasAccessWithPostcode(String ccdReference, String authorisation, String submittedPostcode) {
        try {
            CICUser user = idamService.retrieveUser(authorisation);
            String userEmail = user.getUserInfo().getSub();

            CicaCaseEntity cicaCaseEntity = caseDataRepository.findCase(ccdReference, userEmail)
                .orElseThrow(() -> new UnauthorisedCaseAccessException("User is not authorised to access case: " + ccdReference));

            String storedPostcode = "";
            if (cicaCaseEntity.getData() != null) {
                JsonNode addressNode = cicaCaseEntity.getData().get("cicCaseAddress");
                if (addressNode != null && addressNode.has("PostCode")) {
                    storedPostcode = addressNode.get("PostCode").asText();
                }
            }

            if (!normalize(storedPostcode).equals(normalize(submittedPostcode))) {
                throw new InvalidPostcodeException("Submitted postcode does not match the postcode held in case data");
            }
            return cicaCaseEntity;
        } catch (Exception e) {
            if (e instanceof UnauthorisedCaseAccessException || e instanceof InvalidPostcodeException) {
                throw e;
            }
            log.warn("Error checking case access and postcode for reference: {}", ccdReference, e);
            throw new UnauthorisedCaseAccessException("Error checking case access and postcode: " + e.getMessage());
        }
    }

    public void assignCaseRoleForUser(String ccdReference, String authorisation) {
        try {
            CICUser user = idamService.retrieveUser(authorisation);
            roleAssignmentService.assignCaseRoleForActor(Long.valueOf(ccdReference), user.getUserInfo().getUid());
        } catch (Exception e) {
            log.warn("Failed to assign case role for reference: {}", ccdReference, e);
        }
    }

    private String normalize(String postcode) {
        return postcode != null ? postcode.replace(" ", "").toLowerCase() : "";
    }
}
