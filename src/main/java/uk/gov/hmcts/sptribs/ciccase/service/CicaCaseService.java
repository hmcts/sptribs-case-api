package uk.gov.hmcts.sptribs.ciccase.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.ciccase.repository.CicaCaseRepository;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseResponse;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;

import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class CicaCaseService {

    private static final Pattern CICA_REFERENCE_PATTERN = Pattern.compile("^[XG]\\d+$", Pattern.CASE_INSENSITIVE);

    private final CicaCaseRepository cicaCaseRepository;

    /**
     * Retrieves a case by CICA reference number.
     *
     * @param cicaReference the CICA reference number (must start with X or G followed by digits)
     * @return the case details
     * @throws CaseNotFoundException if no case is found with the given reference
     * @throws IllegalArgumentException if the reference format is invalid
     */
    public CicaCaseResponse getCaseByCicaReference(String cicaReference) {
        log.info("Looking up case by CICA reference: {}", cicaReference);

        validateCicaReferenceFormat(cicaReference);

        return cicaCaseRepository.findByCicaReference(cicaReference)
            .orElseThrow(() -> {
                log.warn("No case found for CICA reference: {}", cicaReference);
                return new CaseNotFoundException("No case found with CICA reference: " + cicaReference);
            });
    }

    private void validateCicaReferenceFormat(String cicaReference) {
        if (cicaReference == null || cicaReference.isBlank()) {
            throw new IllegalArgumentException("CICA reference cannot be null or empty");
        }

        if (!CICA_REFERENCE_PATTERN.matcher(cicaReference).matches()) {
            log.warn("Invalid CICA reference format: {}", cicaReference);
            throw new IllegalArgumentException(
                "Invalid CICA reference format. Reference must start with X or G followed by digits (e.g., X12345, G98765)"
            );
        }
    }
}



