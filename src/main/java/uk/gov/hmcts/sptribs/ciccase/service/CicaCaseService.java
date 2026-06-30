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
}




