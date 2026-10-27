package uk.gov.hmcts.sptribs.ciccase.service;

import com.fasterxml.jackson.databind.JsonNode;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.exception.InvalidPostcodeException;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.idam.CICUser;
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
     * @throws CaseNotFoundException if no eligible case exists for the reference.
     * @throws UnauthorisedCaseAccessException if the user is not associated with the case.
     */
    public void checkIfUserHasAccess(String ccdReference, String authorisation) {
        try {
            CICUser user = retrieveUserForVerification(authorisation);
            if (!caseDataRepository.checkCaseExists(ccdReference)) {
                throw new CaseNotFoundException("No case found with CCD reference: " + ccdReference);
            }
            if (!caseDataRepository.checkIfUserHasAccessToCase(ccdReference, user.getUserInfo().getSub())) {
                throw new UnauthorisedCaseAccessException("User is not authorised to access case: " + ccdReference);
            }
        } catch (Exception e) {
            switch (e) {
                case CaseNotFoundException cnfe -> throw cnfe;
                case UnauthorisedCaseAccessException ucae -> throw ucae;
                case ResponseStatusException rse -> throw rse;
                default -> {
                    log.warn("Error checking case access for reference: {} (type: {})", ccdReference,
                        e.getClass().getSimpleName());
                    throw new IllegalStateException("Unable to check case access", e);
                }
            }
        }
    }

    /** Compare identity fields only after resolving the signed-in user's association with the case. */
    public boolean verifySubject(String ccdReference, String authorisation, String fullName, String dateOfBirth) {
        CICUser user = retrieveUserForVerification(authorisation);
        CicaCaseEntity cicaCase = caseDataRepository.findCase(ccdReference, user.getUserInfo().getSub())
            .orElseThrow(() -> new UnauthorisedCaseAccessException("User is not authorised to access case: " + ccdReference));

        if (cicaCase.getData() == null) {
            return false;
        }
        JsonNode storedName = cicaCase.getData().get("cicCaseFullName");
        JsonNode storedDob = cicaCase.getData().get("cicCaseDateOfBirth");
        return storedName != null && storedName.isTextual() && !storedName.asText().isBlank()
            && storedDob != null && storedDob.isTextual() && !storedDob.asText().isBlank()
            && storedName.asText().trim().equals(fullName.trim()) && storedDob.asText().equals(dateOfBirth);
    }

    private CICUser retrieveUserForVerification(String authorisation) {
        try {
            return idamService.retrieveUser(authorisation);
        } catch (FeignException exception) {
            if (exception.status() == 401 || exception.status() == 403) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user authorisation");
            }
            throw new IllegalStateException("Unable to validate user authorisation", exception);
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

    private String normalize(String postcode) {
        return postcode != null ? postcode.replace(" ", "").toLowerCase() : "";
    }
}
