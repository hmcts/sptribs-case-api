package uk.gov.hmcts.sptribs.testutil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.Map;

@Service
@Slf4j
public class CcdCaseCreator {

    @Autowired
    protected CoreCaseDataApi coreCaseDataApi;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected IdamService idamService;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    public String createInitialStartEventAndSubmit(String eventId,
                                                   String jurisdiction,
                                                   String caseType,
                                                   String caseId,
                                                   Map<String, Object> caseData) {

        final String userToken = idamTokenGenerator.generateIdamTokenForWASeniorCaseworker();
        final String serviceToken = serviceAuthenticationGenerator.generate();
        final String userId = idamService.retrieveUser(userToken).getUserDetails().getId();

        //Fire start event
        final StartEventResponse startCase = coreCaseDataApi.startEventForCaseWorker(
            userToken,
            serviceToken,
            userId,
            jurisdiction,
            caseType,
            caseId,
            eventId
        );

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startCase.getToken())
            .event(Event.builder()
                .id(startCase.getEventId())
                .summary("summary")
                .description("description")
                .build())
            .data(caseData)
            .build();

        //Fire submit event
        final CaseDetails caseDetails = coreCaseDataApi.submitEventForCaseWorker(
            userToken,
            serviceToken,
            userId,
            jurisdiction,
            caseType,
            caseId,
            true,
            caseDataContent
        );

        log.debug("Created case [" + caseDetails.getId() + "]");

        return caseDetails.getId().toString();
    }
}
