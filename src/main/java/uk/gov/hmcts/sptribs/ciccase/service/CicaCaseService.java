package uk.gov.hmcts.sptribs.ciccase.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;

import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class CicaCaseService {

    private static final Pattern CICA_REFERENCE_PATTERN = Pattern.compile("^\\d{16}$", Pattern.CASE_INSENSITIVE);

    private final CaseDataRepository caseDataRepository;

    /**
     * Retrieves a case by CCD reference number.
     *
     * @param ccdReference the CCD reference number.
     * @param userEmail the email of the user.
     * @return the case details
     * @throws CaseNotFoundException if no case is found with the given reference
     * @throws IllegalArgumentException if the reference format is invalid
     * @throws UnauthorisedCaseAccessException if the email is not in the case
     */
    public CicaCaseEntity getCaseByCCDReference(String ccdReference, String userEmail) {
        log.info("Looking up case by CCD reference: {}", ccdReference);

        validateCCDReferenceFormat(ccdReference);
        if (!caseDataRepository.checkCaseExists(ccdReference)) {
            throw new CaseNotFoundException("No case found with CCD reference: " + ccdReference);
        }

        return caseDataRepository.findCase(ccdReference, userEmail).orElseThrow(() -> new UnauthorisedCaseAccessException(
            "User is not authorised to access case: " + ccdReference));
    }

    private void validateCCDReferenceFormat(String ccdReference) {
        if (ccdReference == null || ccdReference.isBlank()) {
            throw new IllegalArgumentException("CCD reference cannot be null or empty");
        }

        if (!CICA_REFERENCE_PATTERN.matcher(ccdReference).matches()) {
            log.warn("Invalid CCD reference format: {}", ccdReference);
            throw new IllegalArgumentException(
                "Invalid CCD reference format. Reference must be 16 digits"
            );
        }
    }
}




