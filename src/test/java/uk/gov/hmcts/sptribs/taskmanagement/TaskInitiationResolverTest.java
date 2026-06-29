package uk.gov.hmcts.sptribs.taskmanagement;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.taskmanagement.model.TaskType;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
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

class TaskInitiationResolverTest {

    @ParameterizedTest
    @MethodSource("createDraftOrderInitiationCases")
    void shouldResolveCreateDraftOrderInitiationTasks(State state, String referralType, List<TaskType> expected) {
        assertThat(TaskInitiationResolver.createDraftOrderInitiationTasks(state, referralType))
            .containsExactlyElementsOf(expected);
    }

    @ParameterizedTest
    @MethodSource("referToJudgeCases")
    void shouldResolveReferToJudgeInitiationTasks(State state, String referralType, List<TaskType> expected) {
        assertThat(TaskInitiationResolver.referToJudgeInitiationTasks(state, referralType))
            .containsExactlyElementsOf(expected);
    }

    @ParameterizedTest
    @MethodSource("referToLegalOfficerCases")
    void shouldResolveReferToLegalOfficerInitiationTasks(State state, String referralType, List<TaskType> expected) {
        assertThat(TaskInitiationResolver.referToLegalOfficerInitiationTasks(state, referralType))
            .containsExactlyElementsOf(expected);
    }

    private static Stream<Arguments> createDraftOrderInitiationCases() {
        return Stream.of(
            Arguments.of(CaseManagement, null, List.of()),
            Arguments.of(CaseManagement, " ", List.of()),
            Arguments.of(AwaitingHearing, WITHDRAWAL_REQUEST.getLabel(), List.of(processCaseWithdrawalDirectionsListed)),
            Arguments.of(ReadyToList, WITHDRAWAL_REQUEST.getLabel(), List.of(processCaseWithdrawalDirections)),
            Arguments.of(AwaitingHearing, RULE_27_REQUEST.getLabel(), List.of(processRule27DecisionListed)),
            Arguments.of(CaseManagement, LISTING_DIRECTIONS.getLabel(), List.of(processListingDirections)),
            Arguments.of(ReadyToList, LISTING_DIRECTIONS.getLabel(), List.of(processListingDirectionsListed)),
            Arguments.of(AwaitingHearing, LISTED_CASE.getLabel(), List.of(processDirectionsReListedCase)),
            Arguments.of(AwaitingHearing, LISTED_CASE_WITHIN_5_DAYS.getLabel(), List.of(processDirectionsReListedCaseWithin5Days)),
            Arguments.of(CaseClosed, SET_ASIDE_REQUEST.getLabel(), List.of(processSetAsideDirections)),
            Arguments.of(CaseClosed, CORRECTIONS.getLabel(), List.of(processCorrections)),
            Arguments.of(CaseManagement, NEW_CASE.getLabel(), List.of(processDirectionsReturned)),
            Arguments.of(AwaitingHearing, POSTPONEMENT_REQUEST.getLabel(), List.of(processPostponementDirections)),
            Arguments.of(ReadyToList, TIME_EXTENSION_REQUEST.getLabel(), List.of(processTimeExtensionDirectionsReturned)),
            Arguments.of(CaseClosed, REINSTATEMENT_REQUEST.getLabel(), List.of(processReinstatementDecisionNotice)),
            Arguments.of(CaseManagement, OTHER.getLabel(), List.of(processOtherDirectionsReturned)),
            Arguments.of(CaseClosed, WRITTEN_REASONS_REQUEST.getLabel(), List.of(processWrittenReasons)),
            Arguments.of(CaseManagement, STRIKE_OUT_REQUEST.getLabel(), List.of(processStrikeOutDirectionsReturned)),
            Arguments.of(AwaitingHearing, STAY_REQUEST.getLabel(), List.of(processStayDirectionsListed)),
            Arguments.of(CaseManagement, STAY_REQUEST.getLabel(), List.of(processStayDirections)),
            Arguments.of(CaseManagement, "Unmapped", List.of())
        );
    }

    private static Stream<Arguments> referToJudgeCases() {
        return Stream.of(
            Arguments.of(AwaitingHearing, LISTED_CASE_WITHIN_5_DAYS.getLabel(), List.of(reviewListCaseWithin5DaysJudge)),
            Arguments.of(AwaitingHearing, POSTPONEMENT_REQUEST.getLabel(), List.of(reviewPostponementRequestJudge)),
            Arguments.of(CaseClosed, CORRECTIONS.getLabel(), List.of(reviewCorrectionsRequest)),
            Arguments.of(CaseClosed, WRITTEN_REASONS_REQUEST.getLabel(), List.of(reviewWrittenReasonsRequest)),
            Arguments.of(CaseClosed, REINSTATEMENT_REQUEST.getLabel(), List.of(reviewReinstatementRequestJudge)),
            Arguments.of(CaseClosed, SET_ASIDE_REQUEST.getLabel(), List.of(reviewSetAsideRequest)),
            Arguments.of(AwaitingHearing, STAY_REQUEST.getLabel(), List.of(reviewStayRequestCaseListedJudge)),
            Arguments.of(CaseManagement, STAY_REQUEST.getLabel(), List.of(reviewStayRequestJudge)),
            Arguments.of(ReadyToList, NEW_CASE.getLabel(), List.of(reviewNewCaseAndProvideDirectionsJudge)),
            Arguments.of(CaseManagement, OTHER.getLabel(), List.of(reviewOtherRequestJudge)),
            Arguments.of(AwaitingHearing, WITHDRAWAL_REQUEST.getLabel(), List.of(reviewWithdrawalRequestCaseListedJudge)),
            Arguments.of(CaseManagement, WITHDRAWAL_REQUEST.getLabel(), List.of(reviewWithdrawalRequestJudge)),
            Arguments.of(AwaitingHearing, RULE_27_REQUEST.getLabel(), List.of(reviewRule27RequestCaseListedJudge)),
            Arguments.of(ReadyToList, RULE_27_REQUEST.getLabel(), List.of(reviewRule27RequestJudge)),
            Arguments.of(CaseManagement, LISTING_DIRECTIONS.getLabel(), List.of(reviewListingDirectionsJudge)),
            Arguments.of(ReadyToList, LISTING_DIRECTIONS.getLabel(), List.of(reviewListingDirectionsCaseListedJudge)),
            Arguments.of(AwaitingHearing, LISTED_CASE.getLabel(), List.of(reviewListCaseJudge)),
            Arguments.of(CaseManagement, STRIKE_OUT_REQUEST.getLabel(), List.of(reviewStrikeOutRequestJudge)),
            Arguments.of(ReadyToList, TIME_EXTENSION_REQUEST.getLabel(), List.of(reviewTimeExtensionRequestJudge)),
            Arguments.of(CaseManagement, "Unmapped", List.of())
        );
    }

    private static Stream<Arguments> referToLegalOfficerCases() {
        return Stream.of(
            Arguments.of(CaseManagement, NEW_CASE.getLabel(), List.of(reviewNewCaseAndProvideDirectionsLO)),
            Arguments.of(ReadyToList, TIME_EXTENSION_REQUEST.getLabel(), List.of(reviewTimeExtensionRequestLO)),
            Arguments.of(CaseManagement, STRIKE_OUT_REQUEST.getLabel(), List.of(reviewStrikeOutRequestLO)),
            Arguments.of(AwaitingHearing, STAY_REQUEST.getLabel(), List.of(reviewStayRequestCaseListedLO)),
            Arguments.of(CaseManagement, STAY_REQUEST.getLabel(), List.of(reviewStayRequestLO)),
            Arguments.of(CaseManagement, LISTING_DIRECTIONS.getLabel(), List.of(reviewListingDirectionsLO)),
            Arguments.of(ReadyToList, LISTING_DIRECTIONS.getLabel(), List.of(reviewListingDirectionsCaseListedLO)),
            Arguments.of(AwaitingHearing, WITHDRAWAL_REQUEST.getLabel(), List.of(reviewWithdrawalRequestCaseListedLO)),
            Arguments.of(ReadyToList, WITHDRAWAL_REQUEST.getLabel(), List.of(reviewWithdrawalRequestLO)),
            Arguments.of(AwaitingHearing, RULE_27_REQUEST.getLabel(), List.of(reviewRule27RequestCaseListedLO)),
            Arguments.of(CaseManagement, RULE_27_REQUEST.getLabel(), List.of(reviewRule27RequestLO)),
            Arguments.of(AwaitingHearing, LISTED_CASE.getLabel(), List.of(reviewListCaseLO)),
            Arguments.of(AwaitingHearing, LISTED_CASE_WITHIN_5_DAYS.getLabel(), List.of(reviewListCaseWithin5DaysLO)),
            Arguments.of(AwaitingHearing, POSTPONEMENT_REQUEST.getLabel(), List.of(reviewPostponementRequestLO)),
            Arguments.of(CaseClosed, REINSTATEMENT_REQUEST.getLabel(), List.of(reviewReinstatementRequestLO)),
            Arguments.of(CaseManagement, OTHER.getLabel(), List.of(reviewOtherRequestLO)),
            Arguments.of(CaseManagement, "Unmapped", List.of())
        );
    }
}
