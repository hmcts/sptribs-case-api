package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ReferToJudgeAdditionalInfo;
import uk.gov.hmcts.sptribs.caseworker.event.page.ReferToJudgeReason;
import uk.gov.hmcts.sptribs.caseworker.model.ReferToJudge;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.time.LocalDate;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_REFER_TO_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
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
public class CaseWorkerReferToJudge implements CCDConfig<CriminalInjuriesCompensationData, State, UserRole> {

    private final ReferToJudgeReason referToJudgeReason = new ReferToJudgeReason();
    private final ReferToJudgeAdditionalInfo referToJudgeAdditionalInfo = new ReferToJudgeAdditionalInfo();

    @Override
    public void configure(ConfigBuilder<CriminalInjuriesCompensationData, State, UserRole> configBuilder) {
        Event.EventBuilder<CriminalInjuriesCompensationData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_REFER_TO_JUDGE)
                .forStates(
                    CaseManagement,
                    ReadyToList,
                    AwaitingHearing,
                    AwaitingOutcome,
                    CaseClosed,
                    CaseStayed)
                .name("Refer case to judge")
                .showSummary()
                .showEventNotes()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_HEARING_CENTRE_ADMIN, ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_WA_CONFIG_USER)
                .grantHistoryOnly(
                    ST_CIC_SENIOR_JUDGE,
                    ST_CIC_JUDGE)
                .publishToCamunda();

        PageBuilder<CriminalInjuriesCompensationData> pageBuilder = new PageBuilder<>(eventBuilder);
        referToJudgeReason.addTo(pageBuilder);
        referToJudgeAdditionalInfo.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData, State> aboutToStart(CaseDetails<CriminalInjuriesCompensationData,
        State> details) {
        CriminalInjuriesCompensationData caseData = details.getData();
        caseData.setReferToJudge(new ReferToJudge());

        return AboutToStartOrSubmitResponse.<CriminalInjuriesCompensationData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData,
        State> aboutToSubmit(CaseDetails<CriminalInjuriesCompensationData, State> details,
                                                                       CaseDetails<CriminalInjuriesCompensationData, State> beforeDetails) {

        CriminalInjuriesCompensationData caseData = details.getData();
        caseData.getReferToJudge().setReferralDate(LocalDate.now());
        if (caseData.getReferToJudge() != null
                && caseData.getReferToJudge().getReferralReason() != null) {
            caseData.getCicCase().setReferralTypeForWA(caseData.getReferToJudge().getReferralReason().getLabel());
        }

        return AboutToStartOrSubmitResponse.<CriminalInjuriesCompensationData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CriminalInjuriesCompensationData, State> details,
                                              CaseDetails<CriminalInjuriesCompensationData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Referral completed")
            .build();
    }

}
