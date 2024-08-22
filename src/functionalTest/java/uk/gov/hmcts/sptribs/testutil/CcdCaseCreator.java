package uk.gov.hmcts.sptribs.testutil;

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
                                                   Map<String, Object> caseData) {

        final String userToken = idamTokenGenerator.generateIdamTokenForCaseworker();
        final String serviceToken = serviceAuthenticationGenerator.generate();
        final String userId = idamService.retrieveUser(userToken).getUserDetails().getId();

        //Fire start event
        final StartEventResponse startCase = coreCaseDataApi.startForCaseworker(
            userToken,
            serviceToken,
            userId,
            jurisdiction,
            caseType,
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
        final CaseDetails caseDetails = coreCaseDataApi.submitForCaseworker(
            userToken,
            serviceToken,
            userId,
            jurisdiction,
            caseType,
            true,
            caseDataContent
        );

        System.out.println("Created case [" + caseDetails.getId() + "]");

        return caseDetails.getId().toString();
    }
}