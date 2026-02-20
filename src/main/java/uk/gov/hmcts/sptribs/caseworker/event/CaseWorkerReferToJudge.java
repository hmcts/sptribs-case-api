package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.taskmanagement.TaskManagementService;
import uk.gov.hmcts.sptribs.taskmanagement.model.TaskType;

import java.time.LocalDate;
import java.util.List;

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
import static uk.gov.hmcts.sptribs.taskmanagement.model.ProcessCategoryIdentifiers.IssueCase;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.followUpNoncomplianceOfDirections;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processFurtherEvidence;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewCorrectionsRequest;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewListCaseJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewListCaseWithin5DaysJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewListingDirectionsCaseListedJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewListingDirectionsJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewNewCaseAndProvideDirectionsJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewOtherRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewPostponementRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewReinstatementRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewRule27RequestCaseListedJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewRule27RequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewSetAsideRequest;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewStayRequestCaseListedJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewStayRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewStrikeOutRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewTimeExtensionRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewWithdrawalRequestCaseListedJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewWithdrawalRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewWrittenReasonsRequest;

@Component
@Slf4j
public class CaseWorkerReferToJudge implements CCDConfig<CaseData, State, UserRole> {
    private static final List<TaskType> CANCELLABLE_TASKS =
        TaskType.getTaskTypesFromProcessCategoryIdentifiers(List.of(IssueCase));
    private static final String LISTED_CASE_WITHIN_5_DAYS = "Listed case (within 5 days)";
    private static final String POSTPONEMENT_REQUEST = "Postponement request";
    private static final String CORRECTIONS = "Corrections";
    private static final String WRITTEN_REASONS_REQUEST = "Written reasons request";
    private static final String REINSTATEMENT_REQUEST = "Reinstatement request";
    private static final String SET_ASIDE_REQUEST = "Set aside request";
    private static final String STAY_REQUEST = "Stay request";
    private static final String NEW_CASE = "New case";
    private static final String OTHER = "Other";
    private static final String WITHDRAWAL_REQUEST = "Withdrawal request";
    private static final String RULE_27_REQUEST = "Rule 27 request";
    private static final String LISTING_DIRECTIONS = "Listing directions";
    private static final String LISTED_CASE = "Listed case";
    private static final String STRIKE_OUT_REQUEST = "Strike out request";
    private static final String TIME_EXTENSION_REQUEST = "Time extension request";

    private final ReferToJudgeReason referToJudgeReason = new ReferToJudgeReason();
    private final ReferToJudgeAdditionalInfo referToJudgeAdditionalInfo = new ReferToJudgeAdditionalInfo();

    @Autowired
    private TaskManagementService taskManagementService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
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
                    ST_CIC_JUDGE);

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        referToJudgeReason.addTo(pageBuilder);
        referToJudgeAdditionalInfo.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        CaseData caseData = details.getData();
        caseData.setReferToJudge(new ReferToJudge());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData caseData = details.getData();
        caseData.getReferToJudge().setReferralDate(LocalDate.now());
        if (caseData.getReferToJudge() != null
                && caseData.getReferToJudge().getReferralReason() != null) {
            caseData.getCicCase().setReferralTypeForWA(caseData.getReferToJudge().getReferralReason().getLabel());
        }

        taskManagementService.enqueueCancellationTasks(CANCELLABLE_TASKS, details.getId());
        taskManagementService.enqueueCompletionTasks(
            List.of(followUpNoncomplianceOfDirections, processFurtherEvidence),
            details.getId()
        );
        taskManagementService.enqueueInitiationTasks(
            getInitiationTaskTypes(details.getState(), caseData),
            caseData,
            details.getId()
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                              CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Referral completed")
            .build();
    }

    private List<TaskType> getInitiationTaskTypes(State state, CaseData caseData) {
        String referralType = caseData.getCicCase().getReferralTypeForWA();
        if (LISTED_CASE_WITHIN_5_DAYS.equals(referralType) && state == AwaitingHearing) {
            return List.of(reviewListCaseWithin5DaysJudge);
        }
        if (POSTPONEMENT_REQUEST.equals(referralType) && state == AwaitingHearing) {
            return List.of(reviewPostponementRequestJudge);
        }
        if (CORRECTIONS.equals(referralType) && state == CaseClosed) {
            return List.of(reviewCorrectionsRequest);
        }
        if (WRITTEN_REASONS_REQUEST.equals(referralType) && state == CaseClosed) {
            return List.of(reviewWrittenReasonsRequest);
        }
        if (REINSTATEMENT_REQUEST.equals(referralType) && state == CaseClosed) {
            return List.of(reviewReinstatementRequestJudge);
        }
        if (SET_ASIDE_REQUEST.equals(referralType) && state == CaseClosed) {
            return List.of(reviewSetAsideRequest);
        }
        if (STAY_REQUEST.equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(reviewStayRequestCaseListedJudge);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(reviewStayRequestJudge);
            }
        }
        if (NEW_CASE.equals(referralType) && (state == CaseManagement || state == ReadyToList)) {
            return List.of(reviewNewCaseAndProvideDirectionsJudge);
        }
        if (OTHER.equals(referralType)) {
            return List.of(reviewOtherRequestJudge);
        }
        if (WITHDRAWAL_REQUEST.equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(reviewWithdrawalRequestCaseListedJudge);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(reviewWithdrawalRequestJudge);
            }
        }
        if (RULE_27_REQUEST.equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(reviewRule27RequestCaseListedJudge);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(reviewRule27RequestJudge);
            }
        }
        if (LISTING_DIRECTIONS.equals(referralType)) {
            if (state == CaseManagement) {
                return List.of(reviewListingDirectionsJudge);
            }
            if (state == ReadyToList) {
                return List.of(reviewListingDirectionsCaseListedJudge);
            }
        }
        if (LISTED_CASE.equals(referralType) && state == AwaitingHearing) {
            return List.of(reviewListCaseJudge);
        }
        if (STRIKE_OUT_REQUEST.equals(referralType) && (state == CaseManagement || state == ReadyToList)) {
            return List.of(reviewStrikeOutRequestJudge);
        }
        if (TIME_EXTENSION_REQUEST.equals(referralType) && (state == CaseManagement || state == ReadyToList)) {
            return List.of(reviewTimeExtensionRequestJudge);
        }
        return List.of();
    }

}
