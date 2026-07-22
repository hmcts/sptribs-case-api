package uk.gov.hmcts.sptribs.ciccase.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.util.CasePartyUtil;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CicaCaseService {

    private final CaseDataRepository caseDataRepository;
    private final IdamService idamService;
    private final ObjectMapper objectMapper;

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
            User user = idamService.retrieveUser(authorisation);
            if (!caseDataRepository.checkCaseExists(ccdReference)) {
                throw new CaseNotFoundException("No case found with CCD reference: " + ccdReference);
            }
            boolean hasAccess = caseDataRepository.checkIfUserHasAccessToCase(ccdReference, user.getUserDetails().getEmail());
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
     * Checks if the user has access to the case and the postcode matches, then returns the user's party.
     *
     * @param ccdReference      the CCD reference number.
     * @param authorisation     user authorization token.
     * @param submittedPostcode the postcode submitted by the user.
     * @return the determined Party.
     * @throws UnauthorisedCaseAccessException if the user does not have access, postcode does not match, or validation fails.
     */
    public Party verifyUserAccessAndGetParty(String ccdReference, String authorisation, String submittedPostcode) {
        try {
            User user = idamService.retrieveUser(authorisation);
            String userEmail = user.getUserDetails().getEmail();

            Optional<CicaCaseEntity> cicaCaseOpt = caseDataRepository.findCase(ccdReference, userEmail, submittedPostcode);
            if (cicaCaseOpt.isEmpty()) {
                throw new UnauthorisedCaseAccessException("Submitted postcode does not match the postcode held in case data");
            }

            CicaCaseEntity cicaCase = cicaCaseOpt.get();
            CaseData caseData = objectMapper.convertValue(cicaCase.getData(), CaseData.class);
            Party party = CasePartyUtil.determineParty(caseData, userEmail);

            if (party == null) {
                throw new UnauthorisedCaseAccessException("User email does not match any registered party on case");
            }

            return party;
        } catch (Exception e) {
            if (e instanceof UnauthorisedCaseAccessException ucae) {
                throw ucae;
            }
            log.warn("Error checking case access, postcode and party for reference: {}", ccdReference, e);
            throw new UnauthorisedCaseAccessException("Error checking case access and postcode: " + e.getMessage());
        }
    }
}
