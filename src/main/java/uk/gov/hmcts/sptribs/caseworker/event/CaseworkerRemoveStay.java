package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.RemoveStay;
import uk.gov.hmcts.sptribs.caseworker.util.EventUtil;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.notification.dispatcher.CaseUnstayedNotification;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_REMOVE_STAY;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerRemoveStay implements CCDConfig<CriminalInjuriesCompensationData, State, UserRole> {

    private static final CcdPageConfiguration removeStay = new RemoveStay();

    @Autowired
    private CaseUnstayedNotification caseUnstayedNotification;

    @Override
    public void configure(final ConfigBuilder<CriminalInjuriesCompensationData, State, UserRole> configBuilder) {
        final PageBuilder<CriminalInjuriesCompensationData> pageBuilder = remove(configBuilder);
        removeStay.addTo(pageBuilder);
    }

    public PageBuilder<CriminalInjuriesCompensationData> remove(final ConfigBuilder<CriminalInjuriesCompensationData, State,
        UserRole> configBuilder) {
        return new PageBuilder<>(configBuilder
            .event(CASEWORKER_REMOVE_STAY)
            .forStates(CaseStayed, ReadyToList)
            .name("Stays: Remove stay")
            .showSummary(true)
            .description("Stays: Remove stay")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE)
            .grantHistoryOnly(
                ST_CIC_JUDGE));
    }

    public AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData, State> aboutToStart(CaseDetails<CriminalInjuriesCompensationData,
        State> details) {
        final CriminalInjuriesCompensationData caseData = details.getData();
        if (details.getState() == CaseStayed) {
            caseData.getRemoveCaseStay().setStayRemoveReason(null);
            caseData.getRemoveCaseStay().setStayRemoveOtherDescription(null);
            caseData.getRemoveCaseStay().setAdditionalDetail(null);
        }

        return AboutToStartOrSubmitResponse.<CriminalInjuriesCompensationData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData,
        State> aboutToSubmit(final CaseDetails<CriminalInjuriesCompensationData, State> details,
                                                                       final CaseDetails<CriminalInjuriesCompensationData,
                                                                           State> beforeDetails) {

        final CriminalInjuriesCompensationData caseData = details.getData();
        caseData.getCaseStay().setIsCaseStayed(YesOrNo.NO);

        return AboutToStartOrSubmitResponse.<CriminalInjuriesCompensationData, State>builder()
            .data(caseData)
            .state(CaseManagement)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CriminalInjuriesCompensationData, State> details,
                                                 CaseDetails<CriminalInjuriesCompensationData, State> beforeDetails) {
        final CriminalInjuriesCompensationData caseData = details.getData();
        try {
            sendCaseUnStayedNotification(caseData.getHyphenatedCaseRef(), caseData);
        } catch (Exception notificationException) {
            log.error("Remove case stay notification failed with exception : {}", notificationException.getMessage());
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Remove case stay notification failed %n## Please resend the notification"))
                .build();
        }
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Stay Removed from Case %n## %s",
                MessageUtil.generateSimpleMessage(EventUtil.getNotificationParties(caseData.getCicCase()))))
            .build();
    }

    private void sendCaseUnStayedNotification(String caseNumber, CaseData data) {
        CicCase cicCase = data.getCicCase();

        if (!isEmpty(cicCase.getSubjectCIC())) {
            caseUnstayedNotification.sendToSubject(data, caseNumber);
        }

        if (!isEmpty(cicCase.getApplicantCIC())) {
            caseUnstayedNotification.sendToApplicant(data, caseNumber);
        }

        if (!isEmpty(cicCase.getRepresentativeCIC())) {
            caseUnstayedNotification.sendToRepresentative(data, caseNumber);
        }
    }
}
