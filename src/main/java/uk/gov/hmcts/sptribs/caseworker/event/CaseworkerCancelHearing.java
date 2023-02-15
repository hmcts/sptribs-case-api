package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.CancelHearingDateSelect;
import uk.gov.hmcts.sptribs.caseworker.event.page.CancelHearingReasonSelect;
import uk.gov.hmcts.sptribs.caseworker.event.page.RecordNotifyParties;
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

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CANCEL_HEARING;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerCancelHearing implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration hearingDateSelect = new CancelHearingDateSelect();
    private static final CcdPageConfiguration reasonSelect = new CancelHearingReasonSelect();

    private static final CcdPageConfiguration recordNotifyParties = new RecordNotifyParties();

    @Autowired
    private HearingService hearingService;

    @Autowired
    private CancelHearingNotification cancelHearingNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var pageBuilder = cancelStart(configBuilder);
        hearingDateSelect.addTo(pageBuilder);
        reasonSelect.addTo(pageBuilder);
        recordNotifyParties.addTo(pageBuilder);
    }

    public PageBuilder cancelStart(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_CANCEL_HEARING)
            .forStates(AwaitingHearing)
            .name("Hearings: Cancel hearing")
            .description("Hearings: Cancel hearing")
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::hearingCancelled)
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));

    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
        DynamicList hearingDateDynamicList = hearingService.getHearingDateDynamicList(details);
        caseData.getCicCase().setHearingList(hearingDateDynamicList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker case cancel hearing callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();
        var state = details.getState();

        DynamicListElement selectedHearing = caseData.getCicCase().getHearingList().getValue();

        if (null != selectedHearing) {
            state = CaseManagement;
            caseData.setRecordListing(null);
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(state)
            .build();
    }

    public SubmittedCallbackResponse hearingCancelled(CaseDetails<CaseData, State> details,
                                                      CaseDetails<CaseData, State> beforeDetails) {

        sendHearingCancelledNotification(details.getData().getHyphenatedCaseRef(), details.getData());
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

    }

}
