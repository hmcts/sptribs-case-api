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
import uk.gov.hmcts.sptribs.caseworker.event.page.ReferToLegalOfficerAdditionalInfo;
import uk.gov.hmcts.sptribs.caseworker.event.page.ReferToLegalOfficerReason;
import uk.gov.hmcts.sptribs.caseworker.model.ReferToLegalOfficer;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.taskmanagement.TaskManagementService;
import uk.gov.hmcts.sptribs.taskmanagement.TaskType;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_REFER_TO_LEGAL_OFFICER;
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
import static uk.gov.hmcts.sptribs.taskmanagement.ProcessCategoryIdentifiers.IssueCase;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.followUpNoncomplianceOfDirections;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processFurtherEvidence;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewListCaseLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewListCaseWithin5DaysLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewListingDirectionsCaseListedLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewListingDirectionsLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewNewCaseAndProvideDirectionsLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewOtherRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewPostponementRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewReinstatementRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewRule27RequestCaseListedLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewRule27RequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewStayRequestCaseListedLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewStayRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewStrikeOutRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewTimeExtensionRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewWithdrawalRequestCaseListedLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewWithdrawalRequestLO;

@Component
@Slf4j
public class CaseWorkerReferToLegalOfficer implements CCDConfig<CaseData, State, UserRole> {
    private static final List<TaskType> CANCELLABLE_TASKS =
        TaskType.getTaskTypesFromProcessCategoryIdentifiers(List.of(IssueCase));
    private static final String NEW_CASE = "New case";
    private static final String TIME_EXTENSION_REQUEST = "Time extension request";
    private static final String STRIKE_OUT_REQUEST = "Strike out request";
    private static final String STAY_REQUEST = "Stay request";
    private static final String LISTING_DIRECTIONS = "Listing directions";
    private static final String WITHDRAWAL_REQUEST = "Withdrawal request";
    private static final String RULE_27_REQUEST = "Rule 27 request";
    private static final String LISTED_CASE = "Listed case";
    private static final String LISTED_CASE_WITHIN_5_DAYS = "Listed case (within 5 days)";
    private static final String POSTPONEMENT_REQUEST = "Postponement request";
    private static final String REINSTATEMENT_REQUEST = "Reinstatement request";
    private static final String OTHER = "Other";

    private final ReferToLegalOfficerReason referToLegalOfficerReason = new ReferToLegalOfficerReason();
    private final ReferToLegalOfficerAdditionalInfo referToLegalOfficerAdditionalInfo = new ReferToLegalOfficerAdditionalInfo();

    @Autowired
    private TaskManagementService taskManagementService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_REFER_TO_LEGAL_OFFICER)
                .forStates(CaseManagement, ReadyToList, AwaitingHearing, AwaitingOutcome, CaseClosed, CaseStayed)
                .name("Refer case to legal officer")
                .showSummary()
                .showEventNotes()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_HEARING_CENTRE_ADMIN, ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_JUDGE, ST_CIC_SENIOR_JUDGE, ST_CIC_WA_CONFIG_USER);

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        referToLegalOfficerReason.addTo(pageBuilder);
        referToLegalOfficerAdditionalInfo.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        CaseData caseData = details.getData();
        caseData.setReferToLegalOfficer(new ReferToLegalOfficer());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData caseData = details.getData();
        caseData.getReferToLegalOfficer().setReferralDate(LocalDate.now());
        if (caseData.getReferToLegalOfficer() != null
                && caseData.getReferToLegalOfficer().getReferralReason() != null) {
            caseData.getCicCase().setReferralTypeForWA(caseData.getReferToLegalOfficer().getReferralReason().getLabel());
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
        if (NEW_CASE.equals(referralType) && (state == CaseManagement || state == ReadyToList)) {
            return List.of(reviewNewCaseAndProvideDirectionsLO);
        }
        if (TIME_EXTENSION_REQUEST.equals(referralType) && (state == CaseManagement || state == ReadyToList)) {
            return List.of(reviewTimeExtensionRequestLO);
        }
        if (STRIKE_OUT_REQUEST.equals(referralType) && (state == CaseManagement || state == ReadyToList)) {
            return List.of(reviewStrikeOutRequestLO);
        }
        if (STAY_REQUEST.equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(reviewStayRequestCaseListedLO);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(reviewStayRequestLO);
            }
        }
        if (LISTING_DIRECTIONS.equals(referralType)) {
            if (state == CaseManagement) {
                return List.of(reviewListingDirectionsLO);
            }
            if (state == ReadyToList) {
                return List.of(reviewListingDirectionsCaseListedLO);
            }
        }
        if (WITHDRAWAL_REQUEST.equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(reviewWithdrawalRequestCaseListedLO);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(reviewWithdrawalRequestLO);
            }
        }
        if (RULE_27_REQUEST.equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(reviewRule27RequestCaseListedLO);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(reviewRule27RequestLO);
            }
        }
        if (LISTED_CASE.equals(referralType) && state == AwaitingHearing) {
            return List.of(reviewListCaseLO);
        }
        if (LISTED_CASE_WITHIN_5_DAYS.equals(referralType) && state == AwaitingHearing) {
            return List.of(reviewListCaseWithin5DaysLO);
        }
        if (POSTPONEMENT_REQUEST.equals(referralType) && state == AwaitingHearing) {
            return List.of(reviewPostponementRequestLO);
        }
        if (REINSTATEMENT_REQUEST.equals(referralType) && state == CaseClosed) {
            return List.of(reviewReinstatementRequestLO);
        }
        if (OTHER.equals(referralType)) {
            return List.of(reviewOtherRequestLO);
        }
        return List.of();
    }

}
