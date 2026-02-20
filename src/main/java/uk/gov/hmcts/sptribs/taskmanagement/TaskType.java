package uk.gov.hmcts.sptribs.taskmanagement;

import lombok.Getter;

import java.util.List;
import java.util.stream.Stream;

import static uk.gov.hmcts.sptribs.taskmanagement.ProcessCategoryIdentifiers.Amendment;
import static uk.gov.hmcts.sptribs.taskmanagement.ProcessCategoryIdentifiers.Application;
import static uk.gov.hmcts.sptribs.taskmanagement.ProcessCategoryIdentifiers.Decision;
import static uk.gov.hmcts.sptribs.taskmanagement.ProcessCategoryIdentifiers.Hearing;
import static uk.gov.hmcts.sptribs.taskmanagement.ProcessCategoryIdentifiers.HearingBundle;
import static uk.gov.hmcts.sptribs.taskmanagement.ProcessCategoryIdentifiers.HearingCompletion;
import static uk.gov.hmcts.sptribs.taskmanagement.ProcessCategoryIdentifiers.IssueCase;
import static uk.gov.hmcts.sptribs.taskmanagement.ProcessCategoryIdentifiers.None;
import static uk.gov.hmcts.sptribs.taskmanagement.ProcessCategoryIdentifiers.Processing;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.COMPLETE_HEARING_OUTCOME_DESCRIPTION;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.CREATE_DUE_DATE_DESCRIPTION;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.EMPTY_DESCRIPTION;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.FOLLOW_UP_NONCOMPLIANCE_DESCRIPTION;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.ISSUE_CASE_TO_RESPONDENT_DESCRIPTION;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.ISSUE_DECISION_DESCRIPTION;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.PROCESS_FURTHER_EVIDENCE_DESCRIPTION;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.REGISTER_NEW_CASE_DESCRIPTION;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.REVIEW_REQUESTS_DESCRIPTION;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.SEND_ORDER_DESCRIPTION;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.STITCH_COLLATE_BUNDLE_DESCRIPTION;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.VET_NEW_CASE_DOCUMENTS_DESCRIPTION;

@Getter
public enum TaskType {
    processCaseWithdrawalDirections("Process case withdrawal directions", Processing, 7, SEND_ORDER_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processCaseWithdrawalDirectionsListed(
        "Process case withdrawal directions listed",
        Processing,
        7,
        SEND_ORDER_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processRule27Decision("Process Rule 27 decision", Processing, 7, SEND_ORDER_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processRule27DecisionListed("Process Rule 27 decision listed", Processing, 7, SEND_ORDER_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processListingDirections("Process listing directions", Processing, 3, SEND_ORDER_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processListingDirectionsListed("Process listing directions listed", Processing, 3, SEND_ORDER_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processDirectionsReListedCase("Process directions re. listed case", Hearing, 1, SEND_ORDER_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processDirectionsReListedCaseWithin5Days(
        "Process directions re. listed case (within 5 days)",
        Hearing,
        1,
        SEND_ORDER_DESCRIPTION,
        WorkType.PRIORITY.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processSetAsideDirections("Process set aside directions", Decision, 1, SEND_ORDER_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processCorrections("Process corrections", Amendment, 3, SEND_ORDER_DESCRIPTION,
        WorkType.HEARING_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processDirectionsReturned("Process directions returned", Processing, 7, SEND_ORDER_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processPostponementDirections("Process postponement directions", Hearing, 1, SEND_ORDER_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processTimeExtensionDirectionsReturned(
        "Process time extension directions returned",
        Processing,
        1,
        SEND_ORDER_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processReinstatementDecisionNotice("Process reinstatement decision notice", Application, 5, SEND_ORDER_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processOtherDirectionsReturned("Process other directions returned", Processing, 7, SEND_ORDER_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processWrittenReasons("Process written reasons", Decision, 3, SEND_ORDER_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processStrikeOutDirectionsReturned("Process strike out directions returned", Processing, 7, SEND_ORDER_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processStayDirections("Process stay directions", Processing, 7, SEND_ORDER_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processStayDirectionsListed("Process stay directions listed", Processing, 7, SEND_ORDER_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    issueDecisionNotice("Issue decision notice", None, 1, ISSUE_DECISION_DESCRIPTION,
        WorkType.HEARING_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    completeHearingOutcome("Complete hearing outcome", HearingCompletion, 5, COMPLETE_HEARING_OUTCOME_DESCRIPTION,
        WorkType.HEARING_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    issueCaseToRespondent("Issue case to respondent", IssueCase, 2, ISSUE_CASE_TO_RESPONDENT_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    vetNewCaseDocuments("Vet new case documents", None, 5, VET_NEW_CASE_DOCUMENTS_DESCRIPTION,
        WorkType.APPLICATIONS.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    reviewNewCaseAndProvideDirectionsLO("Review new case and provide directions - Legal Officer", Processing, 5,
                                        REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.LEGAL_OPERATIONS.name()),
    reviewTimeExtensionRequestLO("Review time extension request - Legal Officer", Processing, 5,
                                 REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.LEGAL_OPERATIONS.name()),
    reviewStrikeOutRequestLO("Review strike out request - Legal Officer", Processing, 5, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.LEGAL_OPERATIONS.name()),
    reviewStayRequestLO("Review stay request - Legal Officer", Processing, 5, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.LEGAL_OPERATIONS.name()),
    reviewStayRequestCaseListedLO("Review stay request case listed - Legal Officer", Processing, 5,
                                  REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.LEGAL_OPERATIONS.name()),
    reviewListingDirectionsLO("Review listing directions - Legal Officer", Processing, 5, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.LEGAL_OPERATIONS.name()),
    reviewListingDirectionsCaseListedLO("Review listing directions case listed - Legal Officer", Processing, 5,
                                        REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.LEGAL_OPERATIONS.name()),
    reviewWithdrawalRequestLO("Review withdrawal request - Legal Officer", Processing, 5, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.LEGAL_OPERATIONS.name()),
    reviewWithdrawalRequestCaseListedLO("Review withdrawal request case listed - Legal Officer", Processing, 5,
                                        REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.LEGAL_OPERATIONS.name()),
    reviewRule27RequestLO("Review Rule 27 request - Legal Officer", Processing, 5, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.LEGAL_OPERATIONS.name()),
    reviewRule27RequestCaseListedLO("Review Rule 27 request case listed - Legal Officer", Processing, 5,
                                    REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.LEGAL_OPERATIONS.name()),
    reviewListCaseLO("Review list case - Legal Officer", Hearing, 1, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.LEGAL_OPERATIONS.name()),
    reviewOtherRequestLO("Review other request - Legal Officer", Processing, 5, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.LEGAL_OPERATIONS.name()),
    reviewListCaseWithin5DaysLO("Review list case (within 5 days) - Legal Officer", Hearing, 1,
                                REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.LEGAL_OPERATIONS.name()),
    reviewPostponementRequestLO("Review postponement request - Legal Officer", Hearing, 1, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.LEGAL_OPERATIONS.name()),
    reviewReinstatementRequestLO("Review reinstatement request - Legal Officer", Application, 5,
                                 REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.LEGAL_OPERATIONS.name()),
    reviewListCaseWithin5DaysJudge("Review list case (within 5 days) - Judge", Hearing, 1, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewPostponementRequestJudge("Review postponement request - Judge", Hearing, 1, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewCorrectionsRequest("Review corrections request", Amendment, 5, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewWrittenReasonsRequest("Review written reasons request", Decision, 28, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewReinstatementRequestJudge(
        "Review reinstatement request - Judge",
        Application,
        5,
        REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewSetAsideRequest("Review set aside request", Decision, 2, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewStayRequestJudge("Review stay request - Judge", Processing, 5, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewStayRequestCaseListedJudge("Review stay request case listed - Judge", Processing, 5,
                                     REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewNewCaseAndProvideDirectionsJudge("Review new case and provide directions - Judge", Processing, 5,
                                           REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewOtherRequestJudge("Review other request - Judge", Processing, 5, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewWithdrawalRequestJudge("Review withdrawal request - Judge", Processing, 5, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewWithdrawalRequestCaseListedJudge("Review withdrawal request case listed - Judge", Processing, 5,
                                           REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewRule27RequestJudge("Review Rule 27 request - Judge", Processing, 5, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewRule27RequestCaseListedJudge("Review Rule 27 request case listed - Judge", Processing, 5,
                                       REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewListingDirectionsJudge("Review listing directions - Judge", Processing, 5, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewListingDirectionsCaseListedJudge("Review listing directions case listed - Judge", Processing, 5,
                                           REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewListCaseJudge("Review list case - Judge", Hearing, 1, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewStrikeOutRequestJudge("Review strike out request - Judge", Processing, 5, REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    reviewTimeExtensionRequestJudge(
        "Review time extension request - Judge",
        Processing,
        5,
        REVIEW_REQUESTS_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.JUDICIAL.name()),
    followUpNoncomplianceOfDirections("Follow up noncompliance of directions", Processing, 1,
                                      FOLLOW_UP_NONCOMPLIANCE_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    registerNewCase("Register new case", None, 5, REGISTER_NEW_CASE_DESCRIPTION,
        WorkType.APPLICATIONS.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    processFurtherEvidence("Process further evidence", Processing, 7, PROCESS_FURTHER_EVIDENCE_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    stitchCollateHearingBundle("Stitch/collate hearing bundle", HearingBundle, 1, STITCH_COLLATE_BUNDLE_DESCRIPTION,
        WorkType.HEARING_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    createDueDate("Create due date", IssueCase, 2, CREATE_DUE_DATE_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    issueDueDate("Issue due date", IssueCase, 2, SEND_ORDER_DESCRIPTION,
        WorkType.ROUTINE_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name()),
    reviewOrder("Review Order", Decision, 5, EMPTY_DESCRIPTION,
        WorkType.DECISION_MAKING_WORK.getLowerCaseName(),
        RoleCategory.ADMIN.name());

    private final String name;
    private final ProcessCategoryIdentifiers processCategoryIdentifier;
    /*dueDateIntervalDays is not always an intrinsic prop of the task as it is set in the initiation dmn,
    just happens to be the case for sp tribs*/
    private final int dueDateIntervalDays;
    private final String description;
    private final String workType;
    private final String roleCategory;

    TaskType(
        String name,
        ProcessCategoryIdentifiers processCategoryIdentifier,
        int dueDateIntervalDays,
        String description,
        String workType,
        String roleCategory
    ) {
        this.name = name;
        this.processCategoryIdentifier = processCategoryIdentifier;
        this.dueDateIntervalDays = dueDateIntervalDays;
        this.description = description;
        this.workType = workType;
        this.roleCategory = roleCategory;
    }

    public static List<TaskType> getTaskTypesFromProcessCategoryIdentifiers(
        List<ProcessCategoryIdentifiers> processCategoryIdentifiers
    ) {
        return Stream.of(TaskType.values())
            .filter(taskType -> processCategoryIdentifiers.contains(taskType.getProcessCategoryIdentifier()))
            .toList();
    }
}

