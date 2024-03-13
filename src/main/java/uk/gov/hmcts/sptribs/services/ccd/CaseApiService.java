package uk.gov.hmcts.sptribs.services.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.edgecase.event.Event;
import uk.gov.hmcts.sptribs.idam.IdamService;

@Service
@Slf4j
public class CaseApiService {

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    IdamService idamService;


    public CaseDetails createCase(String authorization, CaseData caseData,
                                  AppsConfig.AppsDetails appsDetails) {

        final String userId = idamService.retrieveUser(authorization).getUserDetails().getId();

        return coreCaseDataApi.submitForCitizen(
            authorization,
            authTokenGenerator.generate(),
            userId,
            appsDetails.getJurisdiction(),
            appsDetails.getCaseType(),
            true,
            getCaseDataContent(authorization, caseData, userId, appsDetails)
        );
    }

    public CaseDetails updateCase(String authorization, Event eventEnum, Long caseId,
                                  CaseData caseData, AppsConfig.AppsDetails appsDetails) {

        final String userId = idamService.retrieveUser(authorization).getUserDetails().getId();

        return coreCaseDataApi.submitEventForCitizen(
            authorization,
            authTokenGenerator.generate(),
            userId,
            appsDetails.getJurisdiction(),
            appsDetails.getCaseType(),
            String.valueOf(caseId),
            true,
            getCaseDataContent(authorization, caseData, eventEnum, userId,
                    String.valueOf(caseId), appsDetails)
        );
    }

    public String getEventToken(String authorization, String userId, String eventId,
                                AppsConfig.AppsDetails appsDetails) {
        final StartEventResponse res = coreCaseDataApi.startForCitizen(authorization,
            authTokenGenerator.generate(),
            userId,
            appsDetails.getJurisdiction(),
            appsDetails.getCaseType(),
            eventId);

        return res.getToken();
    }

    public String getEventTokenForUpdate(String authorization, String userId, String eventId, String caseId,
                                         AppsConfig.AppsDetails appsDetails) {
        final StartEventResponse res = coreCaseDataApi.startEventForCitizen(authorization,
            authTokenGenerator.generate(),
            userId,
            appsDetails.getJurisdiction(),
            appsDetails.getCaseType(),
            caseId,
            eventId);

        return res.getToken();
    }

    public CaseDetails getCaseDetails(String authorization, Long caseId) {

        return coreCaseDataApi.getCase(
            authorization,
            authTokenGenerator.generate(),
            String.valueOf(caseId));
    }

    private CaseDataContent getCaseDataContent(String authorization, CaseData caseData, String userId,
                                               AppsConfig.AppsDetails appsDetails) {
        return CaseDataContent.builder()
            .data(caseData)
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder().id(appsDetails.getEventIds().getCreateEvent()).build())
            .eventToken(getEventToken(authorization, userId, appsDetails.getEventIds().getCreateEvent(), appsDetails))
            .build();
    }

    private CaseDataContent getCaseDataContent(String authorization, CaseData caseData, Event eventEnum,
                                               String userId, String caseId, AppsConfig.AppsDetails appsDetails) {

        final CaseDataContent.CaseDataContentBuilder builder = CaseDataContent.builder().data(caseData);

        if (eventEnum.getEventType().equalsIgnoreCase(Event.UPDATE.getEventType())) {

            builder.event(uk.gov.hmcts.reform.ccd.client.model.Event.builder().id(appsDetails.getEventIds().getUpdateEvent()).build())
                .eventToken(getEventTokenForUpdate(authorization, userId, appsDetails.getEventIds().getUpdateEvent(),
                                                   caseId, appsDetails));

        } else if (eventEnum.getEventType().equalsIgnoreCase(Event.SUBMIT.getEventType())) {

            builder.event(uk.gov.hmcts.reform.ccd.client.model.Event.builder().id(appsDetails.getEventIds().getSubmitEvent()).build())
                .eventToken(getEventTokenForUpdate(authorization, userId, appsDetails.getEventIds().getSubmitEvent(),
                                                   caseId, appsDetails));

        } else if (eventEnum.getEventType().equalsIgnoreCase(Event.UPDATE_CASE.getEventType())) {

            builder.event(uk.gov.hmcts.reform.ccd.client.model.Event.builder().id(appsDetails.getEventIds().getUpdateCaseEvent()).build())
                .eventToken(getEventTokenForUpdate(authorization, userId, appsDetails.getEventIds().getUpdateCaseEvent(),
                    caseId, appsDetails));

        }
        return builder.build();
    }
}
