package uk.gov.hmcts.sptribs.ciccase.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseResource;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.sptribs.controllers.model.DssCaseDataRequest;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_DSS_UPDATE_CASE_SUBMISSION;

@Service
@RequiredArgsConstructor
public class CicaCaseUpdateService {

    private static final String CASE_NOT_FOUND_FOR_REFERENCE = "No case found for reference";

    private final CicaCaseService cicaCaseService;
    private final CicaRoleService cicaRoleService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    public CaseResource updateCase(String ccdReference, String userAuthToken, DssCaseDataRequest update) {
        cicaCaseService.checkIfUserHasAccess(ccdReference, userAuthToken);

        String serviceAuthToken = authTokenGenerator.generate();
        StartEventResponse startEventResponse;
        try {
            startEventResponse = startUpdate(ccdReference, userAuthToken, serviceAuthToken);
        } catch (FeignException exception) {
            if (!isMissingCaseRole(exception)) {
                throw exception;
            }
            // CCD masks an inaccessible case as not found. Membership was checked above.
            cicaRoleService.assignCreatorRole(ccdReference, userAuthToken);
            startEventResponse = startUpdate(ccdReference, userAuthToken, serviceAuthToken);
        }

        CaseDataContent content = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(CITIZEN_DSS_UPDATE_CASE_SUBMISSION).build())
            .data(update)
            .build();

        return coreCaseDataApi.createEvent(userAuthToken, serviceAuthToken, ccdReference, content);
    }

    private StartEventResponse startUpdate(String ccdReference, String userAuthToken, String serviceAuthToken) {
        return coreCaseDataApi.startEvent(userAuthToken, serviceAuthToken, ccdReference, CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
    }

    private boolean isMissingCaseRole(FeignException exception) {
        return exception.status() == 404
            && (exception.contentUTF8().contains(CASE_NOT_FOUND_FOR_REFERENCE)
                || exception.getMessage().contains(CASE_NOT_FOUND_FOR_REFERENCE));
    }
}
