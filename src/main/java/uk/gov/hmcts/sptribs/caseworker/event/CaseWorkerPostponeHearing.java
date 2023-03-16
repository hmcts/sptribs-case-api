package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.PostponeHaringNotifyParties;
import uk.gov.hmcts.sptribs.caseworker.event.page.PostponeHearingSelectHearing;
import uk.gov.hmcts.sptribs.caseworker.event.page.PostponeHearingSelectReason;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.notification.HearingPostponedNotification;

import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_POSTPONE_HEARING;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Postponed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseWorkerPostponeHearing implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration createHearingSummary = new PostponeHearingSelectHearing();
    private static final CcdPageConfiguration selectReason = new PostponeHearingSelectReason();
    private static final CcdPageConfiguration notifyParties = new PostponeHaringNotifyParties();

    @Autowired
    private HearingService hearingService;

    @Autowired
    private HearingPostponedNotification hearingPostponedNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(
            configBuilder
                .event(CASEWORKER_POSTPONE_HEARING)
                .forStates(AwaitingHearing)
                .name("Hearings: Postpone hearing")
                .showSummary()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
                .grantHistoryOnly(SOLICITOR));
        createHearingSummary.addTo(pageBuilder);
        selectReason.addTo(pageBuilder);
        notifyParties.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
        DynamicList hearingDateDynamicList = hearingService.getHearingDateDynamicList(details);
        caseData.getCicCase().setHearingList(hearingDateDynamicList);
        caseData.setCurrentEvent(CASEWORKER_POSTPONE_HEARING);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker postpone hearing callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();

        Set<NotificationParties> partiesSet = new HashSet<>();
        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartySubject())) {
            partiesSet.add(NotificationParties.SUBJECT);
        }
        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartyRepresentative())) {
            partiesSet.add(NotificationParties.REPRESENTATIVE);
        }
        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartyRespondent())) {
            partiesSet.add(NotificationParties.RESPONDENT);
        }
        caseData.getCicCase().setHearingNotificationParties(partiesSet);
        caseData.setCurrentEvent("");
        caseData.getListing().setHearingStatus(Postponed);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseManagement)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        sendHearingPostponedNotification(details.getData().getHyphenatedCaseRef(), details.getData());

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Hearing Postponed %n## The hearing has been postponed, the case has been updated %n## %s",
                MessageUtil.generateSimpleMessage(details.getData().getCicCase().getHearingNotificationParties())))
            .build();
    }

    private void sendHearingPostponedNotification(String caseNumber, CaseData caseData) {

        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartySubject())) {
            hearingPostponedNotification.sendToSubject(caseData, caseNumber);
        }

        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartyRepresentative())) {
            hearingPostponedNotification.sendToRespondent(caseData, caseNumber);
        }

        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartyRespondent())) {
            hearingPostponedNotification.sendToRepresentative(caseData, caseNumber);
        }
    }

}
