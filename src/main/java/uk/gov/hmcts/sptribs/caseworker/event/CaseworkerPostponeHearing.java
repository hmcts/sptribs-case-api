package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.PostponeHearingNotifyParties;
import uk.gov.hmcts.sptribs.caseworker.event.page.PostponeHearingSelectReason;
import uk.gov.hmcts.sptribs.caseworker.event.page.SelectHearing;
import uk.gov.hmcts.sptribs.caseworker.helper.RecordListHelper;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.notification.dispatcher.HearingPostponedNotification;

import java.time.LocalDate;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_POSTPONE_HEARING;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Postponed;
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
public class CaseworkerPostponeHearing implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration selectHearing = new SelectHearing();
    private static final CcdPageConfiguration selectReason = new PostponeHearingSelectReason();
    private static final CcdPageConfiguration notifyParties = new PostponeHearingNotifyParties();

    private final HearingService hearingService;

    private final RecordListHelper recordListHelper;

    private final HearingPostponedNotification hearingPostponedNotification;

    @Autowired
    public CaseworkerPostponeHearing(HearingService hearingService,
                                     RecordListHelper recordListHelper,
                                     HearingPostponedNotification hearingPostponedNotification) {
        this.hearingService = hearingService;
        this.recordListHelper = recordListHelper;
        this.hearingPostponedNotification = hearingPostponedNotification;
    }

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_POSTPONE_HEARING)
                .forStates(AwaitingHearing)
                .name("Hearings: Postpone hearing")
                .showSummary()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_WA_CONFIG_USER)
                .grantHistoryOnly(ST_CIC_JUDGE)
                .publishToCamunda();

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        selectHearing.addTo(pageBuilder);
        selectReason.addTo(pageBuilder);
        notifyParties.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();
        final DynamicList hearingDateDynamicList = hearingService.getListedHearingDynamicList(caseData);
        caseData.getCicCase().setHearingList(hearingDateDynamicList);
        caseData.setCurrentEvent(CASEWORKER_POSTPONE_HEARING);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();

        recordListHelper.getNotificationParties(caseData);
        caseData.setCurrentEvent("");
        caseData.getListing().setHearingStatus(Postponed);
        caseData.getListing().setPostponeDate(LocalDate.now());

        final String hearingName = caseData.getCicCase().getHearingList().getValue().getLabel();

        hearingService.updateHearingList(caseData, hearingName);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseManagement)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        try {
            sendHearingPostponedNotification(details.getData().getHyphenatedCaseRef(), details.getData());
        } catch (Exception notificationException) {
            log.error("Postpone hearing notification failed with exception : {}", notificationException.getMessage());
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Postpone hearing notification failed %n## Please resend the notification"))
                .build();
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Hearing Postponed %n## The hearing has been postponed, the case has been updated %n## %s",
                MessageUtil.generateSimpleMessage(details.getData().getCicCase().getHearingNotificationParties())))
            .build();
    }

    private void sendHearingPostponedNotification(String caseNumber, CaseData caseData) {
        CicCase cicCase = caseData.getCicCase();

        if (CollectionUtils.isNotEmpty(cicCase.getNotifyPartySubject())) {
            hearingPostponedNotification.sendToSubject(caseData, caseNumber);
        }

        if (CollectionUtils.isNotEmpty(cicCase.getNotifyPartyRepresentative())) {
            hearingPostponedNotification.sendToRepresentative(caseData, caseNumber);
        }

        if (CollectionUtils.isNotEmpty(cicCase.getNotifyPartyRespondent())) {
            hearingPostponedNotification.sendToRespondent(caseData, caseNumber);
        }
    }

}
