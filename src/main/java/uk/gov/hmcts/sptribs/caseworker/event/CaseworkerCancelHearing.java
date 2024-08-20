package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.CancelHearingReasonSelect;
import uk.gov.hmcts.sptribs.caseworker.event.page.RecordNotifyParties;
import uk.gov.hmcts.sptribs.caseworker.event.page.SelectHearing;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.notification.CancelHearingNotification;

import java.time.LocalDate;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CANCEL_HEARING;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Cancelled;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerCancelHearing implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration hearingDateSelect = new SelectHearing();
    private static final CcdPageConfiguration reasonSelect = new CancelHearingReasonSelect();

    private static final CcdPageConfiguration recordNotifyParties = new RecordNotifyParties();

    private final HearingService hearingService;

    private final CancelHearingNotification cancelHearingNotification;

    @Value("${feature.wa.enabled}")
    private boolean isWorkAllocationEnabled;

    @Autowired
    public CaseworkerCancelHearing(HearingService hearingService,
                                   CancelHearingNotification cancelHearingNotification) {
        this.hearingService = hearingService;
        this.cancelHearingNotification = cancelHearingNotification;
    }

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_CANCEL_HEARING)
                .forStates(AwaitingHearing)
                .name("Hearings: Cancel hearing")
                .description("Hearings: Cancel hearing")
                .showSummary()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE)
                .grantHistoryOnly(ST_CIC_JUDGE);

        if (isWorkAllocationEnabled) {
            eventBuilder.publishToCamunda()
                .grant(CREATE_READ_UPDATE, ST_CIC_WA_CONFIG_USER);
        }
        final PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        hearingDateSelect.addTo(pageBuilder);
        reasonSelect.addTo(pageBuilder);
        recordNotifyParties.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();
        DynamicList hearingDateDynamicList = hearingService.getListedHearingDynamicList(caseData);
        caseData.getCicCase().setHearingList(hearingDateDynamicList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker case cancel hearing callback invoked for Case Id: {}", details.getId());

        final CaseData caseData = details.getData();
        State state = details.getState();

        DynamicListElement selectedHearing = caseData.getCicCase().getHearingList().getValue();

        if (selectedHearing != null) {
            state = CaseManagement;
        }
        caseData.getListing().setHearingStatus(Cancelled);
        caseData.getListing().setCancelledDate(LocalDate.now());

        final String hearingName = caseData.getCicCase().getHearingList().getValue().getLabel();

        hearingService.updateHearingList(caseData, hearingName);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(state)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        try {
            sendHearingCancelledNotification(details.getData().getHyphenatedCaseRef(), details.getData());
        } catch (Exception notificationException) {
            log.error("Cancel hearing notification failed with exception : {}", notificationException.getMessage());
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Cancel hearing notification failed %n## Please resend the notification"))
                .build();
        }
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Hearing cancelled %n## %s",
                MessageUtil.generateSimpleMessage(details.getData().getCicCase().getHearingNotificationParties())))
            .build();
    }

    private void sendHearingCancelledNotification(String caseNumber, CaseData data) {
        CicCase cicCase = data.getCicCase();

        if (cicCase.getHearingNotificationParties().contains(NotificationParties.SUBJECT)) {
            cancelHearingNotification.sendToSubject(data, caseNumber);
        }
        if (cicCase.getHearingNotificationParties().contains(NotificationParties.REPRESENTATIVE)) {
            cancelHearingNotification.sendToRepresentative(data, caseNumber);
        }
        if (cicCase.getHearingNotificationParties().contains(NotificationParties.RESPONDENT)) {
            cancelHearingNotification.sendToRespondent(data, caseNumber);
        }
        if (cicCase.getHearingNotificationParties().contains(NotificationParties.APPLICANT)) {
            cancelHearingNotification.sendToApplicant(data, caseNumber);
        }
    }

}
