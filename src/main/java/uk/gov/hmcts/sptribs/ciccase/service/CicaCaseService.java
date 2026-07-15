package uk.gov.hmcts.sptribs.ciccase.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.idam.IdamService;

@Slf4j
@Service
@RequiredArgsConstructor
public class CicaCaseService {

    private final CaseDataRepository caseDataRepository;
    private final IdamService idamService;

    /**
     * Checks if the user has access to the case.
     *
     * @param ccdReference  the CCD reference number.
     * @param authorisation user authorization token.
     * @throws UnauthorisedCaseAccessException if the user does not have access or validation fails.
     */
    public void checkIfUserHasAccess(String ccdReference, String authorisation) {
        try {
            User user = idamService.retrieveUser(authorisation);
            boolean hasAccess = caseDataRepository.checkIfUserHasAccessToCase(ccdReference, user.getUserDetails().getEmail());
            if (!hasAccess) {
                throw new UnauthorisedCaseAccessException("User is not authorised to access case: " + ccdReference);
            }
        } catch (UnauthorisedCaseAccessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Error checking case access for reference: {}", ccdReference, e);
            throw new UnauthorisedCaseAccessException("Error checking case access: " + e.getMessage());
        }
    }

    /**
     * Checks if the user has access to the case and the postcode matches.
     *
     * @param ccdReference      the CCD reference number.
     * @param authorisation     user authorization token.
     * @param submittedPostcode the postcode submitted by the user.
     * @throws UnauthorisedCaseAccessException if the user does not have access, postcode does not match, or validation fails.
     */
    public void checkIfUserHasAccessWithPostcode(String ccdReference, String authorisation, String submittedPostcode) {
        try {
            User user = idamService.retrieveUser(authorisation);
            boolean hasAccess = caseDataRepository.checkIfUserHasAccessToCase(
                ccdReference, user.getUserDetails().getEmail(), submittedPostcode);
            if (!hasAccess) {
                throw new UnauthorisedCaseAccessException("Submitted postcode does not match the postcode held in case data");
            }
        } catch (UnauthorisedCaseAccessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Error checking case access and postcode for reference: {}", ccdReference, e);
            throw new UnauthorisedCaseAccessException("Error checking case access and postcode: " + e.getMessage());
        }
    }


    /**
     * Retrieves a case by CCD reference number.
     *
     * @param ccdReference  the CCD reference number.
     * @param authorisation auth string used to ger user from idam
     * @return the case details
     * @throws CaseNotFoundException           if no case is found with the given reference
     * @throws UnauthorisedCaseAccessException if the email is not in the case
     */
    public CicaCaseEntity getCaseByCCDReference(String ccdReference, String authorisation, String postcode) {
        log.info("Looking up case by CCD reference: {}", ccdReference);

        User user = idamService.retrieveUser(authorisation);

        if (!caseDataRepository.checkCaseExists(ccdReference)) {
            throw new CaseNotFoundException("No case found with CCD reference: " + ccdReference);
        }

        return caseDataRepository.findCase(ccdReference,
            user.getUserDetails().getEmail(), postcode).orElseThrow(() -> new UnauthorisedCaseAccessException(
            "User is not authorised to access case: " + ccdReference));
    }
}
