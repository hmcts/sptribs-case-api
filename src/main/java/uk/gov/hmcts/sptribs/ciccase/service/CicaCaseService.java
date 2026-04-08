package uk.gov.hmcts.sptribs.ciccase.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseResponse;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;

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
     * @return the case details
     * @throws CaseNotFoundException if no case is found with the given reference
     * @throws IllegalArgumentException if the reference format is invalid
     */
    public CicaCaseResponse getCaseByCCDReference(String ccdReference) {
        log.info("Looking up case by CCD reference: {}", ccdReference);

        validateCCDReferenceFormat(ccdReference);

        return caseDataRepository.findByCCDReferenceAndEmail(ccdReference)
            .orElseThrow(() -> {
                log.warn("No case found for CCD reference: {}", ccdReference);
                return new CaseNotFoundException("No case found with CCD reference: " + ccdReference);
            });
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




