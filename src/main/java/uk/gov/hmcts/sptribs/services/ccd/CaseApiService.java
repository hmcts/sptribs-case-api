package uk.gov.hmcts.sptribs.services.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.sptribs.caseworker.model.Notification;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.edgecase.event.Event;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.systemupdate.convert.CaseDetailsConverter;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@SuppressWarnings("PMD")
public class CaseApiService {

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    IdamService idamService;

    @Autowired
    CaseDetailsConverter caseDetailsConverter;


    public CaseDetails createCase(String authorization, CaseData caseData,
                                  AppsConfig.AppsDetails appsDetails) {

        String userId = idamService.retrieveUser(authorization).getUserDetails().getId();

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

    public CaseDetails updateCaseForCitizen(String authorization, Event eventEnum, Long caseId,
                                            CaseData caseData, AppsConfig.AppsDetails appsDetails) {

        String userId = idamService.retrieveUser(authorization).getUserDetails().getId();

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

    public CaseDetails updateCaseForCaseworker(String authorization, Long caseId,
                                                           CaseData caseData, AppsConfig.AppsDetails appsDetails) {

        String userId = idamService.retrieveUser(authorization).getUserDetails().getId();

        return coreCaseDataApi.submitEventForCaseWorker(
            authorization,
            authTokenGenerator.generate(),
            userId,
            appsDetails.getJurisdiction(),
            appsDetails.getCaseType(),
            String.valueOf(caseId),
            true,
            getCaseDataContent(authorization, caseData, userId,
                String.valueOf(caseId), appsDetails)
        );
    }

    public CaseDetails updateNotificationCaseForCaseworker(String authorization, Long caseId,
                                                           CaseDetails caseDetails, AppsConfig.AppsDetails appsDetails,
                                                           NotificationRequest notificationRequest) {

        CaseData caseData = getCaseData(caseDetails, notificationRequest);

        String userId = idamService.retrieveUser(authorization).getUserDetails().getId();

        return coreCaseDataApi.submitEventForCaseWorker(
            authorization,
            authTokenGenerator.generate(),
            userId,
            appsDetails.getJurisdiction(),
            appsDetails.getCaseType(),
            String.valueOf(caseId),
            true,
            getCaseDataContentUpdateNotification(authorization, caseData, userId,
                String.valueOf(caseId), appsDetails)
        );
    }

    private CaseData getCaseData(CaseDetails caseDetails, NotificationRequest notificationRequest) {
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        List<ListValue<Notification>> notificationList = caseData.getCicCase().getNotificationList();
        if(notificationList != null) {
            notificationList.stream()
                .filter(notification -> !notification.getId().equals(notificationRequest.getReference()))
                .findFirst()
                .ifPresent(n -> {
                        n.getValue().setName(notificationRequest.getCaseId());
                        n.getValue().setTemplateId(notificationRequest.getCaseId());
                    }
                );
        }
        caseData.getCicCase().setNotificationList(notificationList);
        return caseData;
    }

    private CaseDataContent getCaseDataContentUpdateNotification(String authorization, CaseData caseData,
                                               String userId, String caseId, AppsConfig.AppsDetails appsDetails) {

        return CaseDataContent.builder().event(uk.gov.hmcts.reform.ccd.client.model.Event.builder().id(appsDetails.getEventIds().getUpdateNotificationEvent()).build())
            .eventToken(getEventToken(authorization, userId, appsDetails.getEventIds().getUpdateNotificationEvent(),
                caseId, appsDetails))
            .data(caseData)
            .build();
    }

    private CaseDataContent getCaseDataContent(String authorization, CaseData caseData,
                                               String userId, String caseId, AppsConfig.AppsDetails appsDetails) {

        return CaseDataContent.builder().event(uk.gov.hmcts.reform.ccd.client.model.Event.builder().id(appsDetails.getEventIds().getCreateNotificationEvent()).build())
            .eventToken(getEventToken(authorization, userId, appsDetails.getEventIds().getCreateNotificationEvent(),
                caseId, appsDetails))
            .data(caseData)
            .build();
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
        CaseDataContent.CaseDataContentBuilder builder = CaseDataContent.builder().data(caseData);
        if (eventEnum.getEventType().equalsIgnoreCase(Event.UPDATE.getEventType())) {
            builder.event(uk.gov.hmcts.reform.ccd.client.model.Event.builder().id(appsDetails.getEventIds().getUpdateEvent()).build())
                .eventToken(getEventTokenForUpdate(authorization, userId, appsDetails.getEventIds().getUpdateEvent(),
                                                   caseId, appsDetails));
        } else if (eventEnum.getEventType().equalsIgnoreCase(Event.SUBMIT.getEventType())) {
            builder.event(uk.gov.hmcts.reform.ccd.client.model.Event.builder().id(appsDetails.getEventIds().getSubmitEvent()).build())
                .eventToken(getEventTokenForUpdate(authorization, userId, appsDetails.getEventIds().getSubmitEvent(),
                                                   caseId, appsDetails));
        }

        return builder.build();
    }

    public String getEventToken(String authorization, String userId, String eventId,
                                AppsConfig.AppsDetails appsDetails) {
        StartEventResponse res = coreCaseDataApi.startForCitizen(authorization,
                                                                 authTokenGenerator.generate(),
                                                                 userId,
                                                                 appsDetails.getJurisdiction(),
                                                                 appsDetails.getCaseType(),
                                                                 eventId);

        //This has to be removed
        log.info("Response of create event token: " + res.getToken());

        return res.getToken();
    }

    public String getEventTokenForUpdate(String authorization, String userId, String eventId, String caseId,
                                         AppsConfig.AppsDetails appsDetails) {
        StartEventResponse res = coreCaseDataApi.startEventForCitizen(authorization,
                authTokenGenerator.generate(),
                userId,
                appsDetails.getJurisdiction(),
                appsDetails.getCaseType(),
                caseId,
                eventId);

        //This has to be removed
        log.info("Response of update event token: " + res.getToken());

        return res.getToken();
    }

    public String getEventToken(String authorization, String userId, String eventId, String caseId,
                                         AppsConfig.AppsDetails appsDetails) {
        StartEventResponse res = coreCaseDataApi.startEventForCaseWorker(authorization,
            authTokenGenerator.generate(),
            userId,
            appsDetails.getJurisdiction(),
            appsDetails.getCaseType(),
            caseId,
            eventId);

        //This has to be removed
        log.info("Response of update notification event token: " + res.getToken());

        return res.getToken();
    }

    public CaseDetails getCaseDetails(String authorization, Long caseId) {

        return coreCaseDataApi.getCase(
            authorization,
            authTokenGenerator.generate(),
            String.valueOf(caseId));
    }
}
