package uk.gov.hmcts.sptribs.taskmanagement;

import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.taskmanagement.model.TaskType;

import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.CORRECTIONS;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.LISTED_CASE;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.LISTED_CASE_WITHIN_5_DAYS;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.LISTING_DIRECTIONS;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.NEW_CASE;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.OTHER;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.POSTPONEMENT_REQUEST;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.REINSTATEMENT_REQUEST;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.RULE_27_REQUEST;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.SET_ASIDE_REQUEST;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.STAY_REQUEST;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.STRIKE_OUT_REQUEST;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.TIME_EXTENSION_REQUEST;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.WITHDRAWAL_REQUEST;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.WRITTEN_REASONS_REQUEST;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processCaseWithdrawalDirections;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processCaseWithdrawalDirectionsListed;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processCorrections;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processDirectionsReListedCase;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processDirectionsReListedCaseWithin5Days;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processDirectionsReturned;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processListingDirections;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processListingDirectionsListed;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processOtherDirectionsReturned;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processPostponementDirections;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processReinstatementDecisionNotice;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processRule27Decision;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processRule27DecisionListed;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processSetAsideDirections;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processStayDirections;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processStayDirectionsListed;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processStrikeOutDirectionsReturned;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processTimeExtensionDirectionsReturned;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processWrittenReasons;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewCorrectionsRequest;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewListCaseJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewListCaseLO;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewListCaseWithin5DaysJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewListCaseWithin5DaysLO;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewListingDirectionsCaseListedJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewListingDirectionsCaseListedLO;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewListingDirectionsJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewListingDirectionsLO;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewNewCaseAndProvideDirectionsJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewNewCaseAndProvideDirectionsLO;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewOtherRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewOtherRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewPostponementRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewPostponementRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewReinstatementRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewReinstatementRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewRule27RequestCaseListedJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewRule27RequestCaseListedLO;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewRule27RequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewRule27RequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewSetAsideRequest;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewStayRequestCaseListedJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewStayRequestCaseListedLO;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewStayRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewStayRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewStrikeOutRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewStrikeOutRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewTimeExtensionRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewTimeExtensionRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewWithdrawalRequestCaseListedJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewWithdrawalRequestCaseListedLO;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewWithdrawalRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewWithdrawalRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewWrittenReasonsRequest;

public final class TaskInitiationResolver {

    private TaskInitiationResolver() {
    }

    public static List<TaskType> createDraftOrderInitiationTasks(State state, String referralType) {
        if (WITHDRAWAL_REQUEST.getLabel().equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(processCaseWithdrawalDirectionsListed);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(processCaseWithdrawalDirections);
            }
        }

        if (RULE_27_REQUEST.getLabel().equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(processRule27DecisionListed);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(processRule27Decision);
            }
        }

        if (LISTING_DIRECTIONS.getLabel().equals(referralType)) {
            if (state == CaseManagement) {
                return List.of(processListingDirections);
            }
            if (state == ReadyToList) {
                return List.of(processListingDirectionsListed);
            }
        }

        if (LISTED_CASE.getLabel().equals(referralType) && state == AwaitingHearing) {
            return List.of(processDirectionsReListedCase);
        }

        if (LISTED_CASE_WITHIN_5_DAYS.getLabel().equals(referralType) && state == AwaitingHearing) {
            return List.of(processDirectionsReListedCaseWithin5Days);
        }

        if (SET_ASIDE_REQUEST.getLabel().equals(referralType) && state == CaseClosed) {
            return List.of(processSetAsideDirections);
        }

        if (CORRECTIONS.getLabel().equals(referralType) && state == CaseClosed) {
            return List.of(processCorrections);
        }

        if (NEW_CASE.getLabel().equals(referralType) && (state == CaseManagement || state == ReadyToList)) {
            return List.of(processDirectionsReturned);
        }

        if (POSTPONEMENT_REQUEST.getLabel().equals(referralType) && state == AwaitingHearing) {
            return List.of(processPostponementDirections);
        }

        if (TIME_EXTENSION_REQUEST.getLabel().equals(referralType)
            && (state == CaseManagement || state == ReadyToList)) {
            return List.of(processTimeExtensionDirectionsReturned);
        }

        if (REINSTATEMENT_REQUEST.getLabel().equals(referralType) && state == CaseClosed) {
            return List.of(processReinstatementDecisionNotice);
        }

        if (OTHER.getLabel().equals(referralType)) {
            return List.of(processOtherDirectionsReturned);
        }

        if (WRITTEN_REASONS_REQUEST.getLabel().equals(referralType) && state == CaseClosed) {
            return List.of(processWrittenReasons);
        }

        if (STRIKE_OUT_REQUEST.getLabel().equals(referralType)
            && (state == CaseManagement || state == ReadyToList)) {
            return List.of(processStrikeOutDirectionsReturned);
        }

        if (STAY_REQUEST.getLabel().equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(processStayDirectionsListed);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(processStayDirections);
            }
        }

        return List.of();
    }

    public static List<TaskType> referToJudgeInitiationTasks(State state, String referralType) {
        if (LISTED_CASE_WITHIN_5_DAYS.getLabel().equals(referralType) && state == AwaitingHearing) {
            return List.of(reviewListCaseWithin5DaysJudge);
        }
        if (POSTPONEMENT_REQUEST.getLabel().equals(referralType) && state == AwaitingHearing) {
            return List.of(reviewPostponementRequestJudge);
        }
        if (CORRECTIONS.getLabel().equals(referralType) && state == CaseClosed) {
            return List.of(reviewCorrectionsRequest);
        }
        if (WRITTEN_REASONS_REQUEST.getLabel().equals(referralType) && state == CaseClosed) {
            return List.of(reviewWrittenReasonsRequest);
        }
        if (REINSTATEMENT_REQUEST.getLabel().equals(referralType) && state == CaseClosed) {
            return List.of(reviewReinstatementRequestJudge);
        }
        if (SET_ASIDE_REQUEST.getLabel().equals(referralType) && state == CaseClosed) {
            return List.of(reviewSetAsideRequest);
        }
        if (STAY_REQUEST.getLabel().equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(reviewStayRequestCaseListedJudge);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(reviewStayRequestJudge);
            }
        }
        if (NEW_CASE.getLabel().equals(referralType) && (state == CaseManagement || state == ReadyToList)) {
            return List.of(reviewNewCaseAndProvideDirectionsJudge);
        }
        if (OTHER.getLabel().equals(referralType)) {
            return List.of(reviewOtherRequestJudge);
        }
        if (WITHDRAWAL_REQUEST.getLabel().equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(reviewWithdrawalRequestCaseListedJudge);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(reviewWithdrawalRequestJudge);
            }
        }
        if (RULE_27_REQUEST.getLabel().equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(reviewRule27RequestCaseListedJudge);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(reviewRule27RequestJudge);
            }
        }
        if (LISTING_DIRECTIONS.getLabel().equals(referralType)) {
            if (state == CaseManagement) {
                return List.of(reviewListingDirectionsJudge);
            }
            if (state == ReadyToList) {
                return List.of(reviewListingDirectionsCaseListedJudge);
            }
        }
        if (LISTED_CASE.getLabel().equals(referralType) && state == AwaitingHearing) {
            return List.of(reviewListCaseJudge);
        }
        if (STRIKE_OUT_REQUEST.getLabel().equals(referralType)
            && (state == CaseManagement || state == ReadyToList)) {
            return List.of(reviewStrikeOutRequestJudge);
        }
        if (TIME_EXTENSION_REQUEST.getLabel().equals(referralType)
            && (state == CaseManagement || state == ReadyToList)) {
            return List.of(reviewTimeExtensionRequestJudge);
        }
        return List.of();
    }

    public static List<TaskType> referToLegalOfficerInitiationTasks(State state, String referralType) {
        if (NEW_CASE.getLabel().equals(referralType) && (state == CaseManagement || state == ReadyToList)) {
            return List.of(reviewNewCaseAndProvideDirectionsLO);
        }
        if (TIME_EXTENSION_REQUEST.getLabel().equals(referralType)
            && (state == CaseManagement || state == ReadyToList)) {
            return List.of(reviewTimeExtensionRequestLO);
        }
        if (STRIKE_OUT_REQUEST.getLabel().equals(referralType)
            && (state == CaseManagement || state == ReadyToList)) {
            return List.of(reviewStrikeOutRequestLO);
        }
        if (STAY_REQUEST.getLabel().equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(reviewStayRequestCaseListedLO);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(reviewStayRequestLO);
            }
        }
        if (LISTING_DIRECTIONS.getLabel().equals(referralType)) {
            if (state == CaseManagement) {
                return List.of(reviewListingDirectionsLO);
            }
            if (state == ReadyToList) {
                return List.of(reviewListingDirectionsCaseListedLO);
            }
        }
        if (WITHDRAWAL_REQUEST.getLabel().equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(reviewWithdrawalRequestCaseListedLO);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(reviewWithdrawalRequestLO);
            }
        }
        if (RULE_27_REQUEST.getLabel().equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(reviewRule27RequestCaseListedLO);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(reviewRule27RequestLO);
            }
        }
        if (LISTED_CASE.getLabel().equals(referralType) && state == AwaitingHearing) {
            return List.of(reviewListCaseLO);
        }
        if (LISTED_CASE_WITHIN_5_DAYS.getLabel().equals(referralType) && state == AwaitingHearing) {
            return List.of(reviewListCaseWithin5DaysLO);
        }
        if (POSTPONEMENT_REQUEST.getLabel().equals(referralType) && state == AwaitingHearing) {
            return List.of(reviewPostponementRequestLO);
        }
        if (REINSTATEMENT_REQUEST.getLabel().equals(referralType) && state == CaseClosed) {
            return List.of(reviewReinstatementRequestLO);
        }
        if (OTHER.getLabel().equals(referralType)) {
            return List.of(reviewOtherRequestLO);
        }
        return List.of();
    }
}
