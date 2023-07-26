package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ReferToJudgeAdditionalInfo;
import uk.gov.hmcts.sptribs.caseworker.event.page.ReferToJudgeReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_REFER_TO_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseWorkerReferToJudge implements CCDConfig<CaseData, State, UserRole> {

    private final ReferToJudgeReason referToJudgeReason = new ReferToJudgeReason();

    private final ReferToJudgeAdditionalInfo referToJudgeAdditionalInfo = new ReferToJudgeAdditionalInfo();

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_REFER_TO_JUDGE)
            .forStates(
                CaseManagement,
                AwaitingHearing,
                AwaitingOutcome,
                CaseClosed,
                CaseStayed)
            .name("Refer case to judge")
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::referred)
            .grant(CREATE_READ_UPDATE, CIC_SUPER_USER,
                CIC_CENTRE_ADMIN, CIC_CENTRE_TEAM_LEADER)
            .grantHistoryOnly(
                CIC_CASEWORKER,
                CIC_SENIOR_CASEWORKER,
                CIC_CENTRE_ADMIN,
                CIC_CENTRE_TEAM_LEADER,
                CIC_SENIOR_JUDGE,
                CIC_SUPER_USER,
                CIC_JUDGE));
        referToJudgeReason.addTo(pageBuilder);
        referToJudgeAdditionalInfo.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker refer case to judge for Case Id: {}", details.getId());

        var caseData = details.getData();
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse referred(CaseDetails<CaseData, State> details,
                                              CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Referral completed")
            .build();
    }


}
