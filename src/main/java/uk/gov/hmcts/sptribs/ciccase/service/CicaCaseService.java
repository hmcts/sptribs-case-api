package uk.gov.hmcts.sptribs.ciccase.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CicaCaseService {

    private final CaseDataRepository caseDataRepository;
    private final IdamService idamService;
    private final ObjectMapper objectMapper;

    /**
     * Retrieves a case by CCD reference number.
     *
     * @param ccdReference  the CCD reference number.
     * @param authorisation auth string used to ger user from idam
     * @return the case details
     * @throws CaseNotFoundException           if no case is found with the given reference
     * @throws UnauthorisedCaseAccessException if the email is not in the case
     */
    public CicaCaseEntity getCaseByCCDReference(String ccdReference, String authorisation) {
        log.info("Looking up case by CCD reference: {}", ccdReference);

        User user = idamService.retrieveUser(authorisation);

        if (!caseDataRepository.checkCaseExists(ccdReference)) {
            throw new CaseNotFoundException("No case found with CCD reference: " + ccdReference);
        }

        return caseDataRepository.findCase(ccdReference,
            user.getUserDetails().getEmail()).orElseThrow(() -> new UnauthorisedCaseAccessException(
            "User is not authorised to access case: " + ccdReference));
    }

    /**
     * Validates the submitted postcode against the stored subject postcode on the case entity.
     *
     * @param cicaCaseEntity     the case entity.
     * @param submittedPostcode  the postcode submitted by the user.
     * @throws UnauthorisedCaseAccessException if the postcodes do not match or cannot be found.
     */
    public void validatePostcode(CicaCaseEntity cicaCaseEntity, String submittedPostcode) {
        log.info("Validating submitted postcode against case data");

        if (submittedPostcode == null) {
            throw new UnauthorisedCaseAccessException("Submitted postcode cannot be null");
        }

        CaseData caseData = objectMapper.convertValue(cicaCaseEntity.getData(), CaseData.class);
        CicCase cicCase = Optional.ofNullable(caseData)
            .map(CaseData::getCicCase)
            .orElseThrow(() -> new UnauthorisedCaseAccessException("CicCase data not found in case data"));

        AddressGlobalUK targetAddress = cicCase.getAddress();

        if (targetAddress == null || targetAddress.getPostCode() == null) {
            throw new UnauthorisedCaseAccessException("Postcode not found in case data");
        }

        String storedPostcode = targetAddress.getPostCode();
        String sanitisedSubmitted = submittedPostcode.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        String sanitisedStored = storedPostcode.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);

        if (!sanitisedSubmitted.equals(sanitisedStored)) {
            throw new UnauthorisedCaseAccessException("Submitted postcode does not match the postcode held in case data");
        }
    }

    /**
     * Retrieves the case after validating the submitted postcode.
     *
     * @param ccdReference       the CCD reference number.
     * @param authorisation      user authorization token.
     * @param submittedPostcode  the postcode submitted by the user.
     * @return the verified case details.
     */
    public CicaCaseEntity getCaseWithVerifiedPostcode(String ccdReference, String authorisation, String submittedPostcode) {
        CicaCaseEntity cicaCaseEntity = getCaseByCCDReference(ccdReference, authorisation);
        validatePostcode(cicaCaseEntity, submittedPostcode);
        return cicaCaseEntity;
    }
}
