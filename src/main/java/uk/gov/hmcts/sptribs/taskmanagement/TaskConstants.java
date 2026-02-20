package uk.gov.hmcts.sptribs.taskmanagement;

import java.util.Set;

import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewCorrectionsRequest;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewListCaseJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewListCaseLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewListCaseWithin5DaysJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewListCaseWithin5DaysLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewListingDirectionsCaseListedJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewListingDirectionsCaseListedLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewListingDirectionsJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewListingDirectionsLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewNewCaseAndProvideDirectionsJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewNewCaseAndProvideDirectionsLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewOtherRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewOtherRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewPostponementRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewPostponementRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewReinstatementRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewReinstatementRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewRule27RequestCaseListedJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewRule27RequestCaseListedLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewRule27RequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewRule27RequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewSetAsideRequest;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewStayRequestCaseListedJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewStayRequestCaseListedLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewStayRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewStayRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewStrikeOutRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewStrikeOutRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewTimeExtensionRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewTimeExtensionRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewWithdrawalRequestCaseListedJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewWithdrawalRequestCaseListedLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewWithdrawalRequestJudge;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewWithdrawalRequestLO;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewWrittenReasonsRequest;

public final class TaskConstants {

    public static final String JURISDICTION = "ST_CIC";
    public static final String DEFAULT_REGION = "1";
    public static final String DEFAULT_LOCATION = "336559";
    public static final String DEFAULT_EXECUTION_TYPE = "Case Management Task";
    public static final String DEFAULT_SECURITY_CLASSIFICATION = "PUBLIC";
    public static final String DEFAULT_TASK_SYSTEM = "SELF";
    public static final int MAJOR_PRIORITY = 5000;
    public static final int MINOR_PRIORITY = 500;

    public static final String EMPTY_DESCRIPTION = "";
    public static final String SEND_ORDER_DESCRIPTION =
        "[Orders: Send order](/cases/case-details/${[CASE_REFERENCE]}/trigger/caseworker-send-order)";
    public static final String ISSUE_DECISION_DESCRIPTION =
        "[Decision: Issue a decision](/cases/case-details/${[CASE_REFERENCE]}/trigger/caseworker-issue-decision)"
            + "<br/>"
            + "[Decision: Issue final decision]"
            + "(/cases/case-details/${[CASE_REFERENCE]}/trigger/caseworker-issue-final-decision)";
    public static final String COMPLETE_HEARING_OUTCOME_DESCRIPTION =
        "[Hearings:Create summary](/cases/case-details/${[CASE_REFERENCE]}/trigger/create-hearing-summary)";
    public static final String ISSUE_CASE_TO_RESPONDENT_DESCRIPTION =
        "[Case: Issue to respondent](/cases/case-details/${[CASE_REFERENCE]}/trigger/caseworker-issue-case)";
    public static final String VET_NEW_CASE_DOCUMENTS_DESCRIPTION =
        "[Case: Build case](/cases/case-details/${[CASE_REFERENCE]}/trigger/caseworker-case-built)";
    public static final String CREATE_DUE_DATE_DESCRIPTION =
        "[Orders: Create draft](/cases/case-details/${[CASE_REFERENCE]}/trigger/create-draft-order)";
    public static final String REVIEW_REQUESTS_DESCRIPTION =
        "[Orders: Create and send order](/cases/case-details/${[CASE_REFERENCE]}/trigger/create-and-send-order)";
    public static final String FOLLOW_UP_NONCOMPLIANCE_DESCRIPTION =
        "[Document management: Upload](/cases/case-details/${[CASE_REFERENCE]}/trigger/caseworker-document-management)"
            + "<br/>"
            + "[Orders: Manage due date](/cases/case-details/${[CASE_REFERENCE]}/trigger/caseworker-amend-due-date)"
            + "<br/>"
            + "[Refer case to judge](/cases/case-details/${[CASE_REFERENCE]}/trigger/refer-to-judge)"
            + "<br/>"
            + "[Refer case to legal officer]"
            + "(/cases/case-details/${[CASE_REFERENCE]}/trigger/refer-to-legal-officer)"
            + "<br/>"
            + "[Case: Contact parties](/cases/case-details/${[CASE_REFERENCE]}/trigger/contact-parties)";
    public static final String REGISTER_NEW_CASE_DESCRIPTION =
        "[Case: Edit case](/cases/case-details/${[CASE_REFERENCE]}/trigger/edit-case)";
    public static final String STITCH_COLLATE_BUNDLE_DESCRIPTION =
        "[Bundle: Create a bundle](/cases/case-details/${[CASE_REFERENCE]}/trigger/createBundle)";
    public static final String PROCESS_FURTHER_EVIDENCE_DESCRIPTION =
        "[Document management: Amend](/cases/case-details/${[CASE_REFERENCE]}/trigger/caseworker-amend-document)"
            + "<br/>"
            + "[Case: Edit case](/cases/case-details/${[CASE_REFERENCE]}/trigger/edit-case)"
            + "<br/>"
            + "[Refer case to judge](/cases/case-details/${[CASE_REFERENCE]}/trigger/refer-to-judge)"
            + "<br/>"
            + "[Refer case to legal officer]"
            + "(/cases/case-details/${[CASE_REFERENCE]}/trigger/refer-to-legal-officer)"
            + "<br/>"
            + "[Case: Contact parties](/cases/case-details/${[CASE_REFERENCE]}/trigger/contact-parties)";


    public static final Set<TaskType> REVIEW_REQUESTS_TASKS = Set.of(
        reviewNewCaseAndProvideDirectionsLO,
        reviewTimeExtensionRequestLO,
        reviewStrikeOutRequestLO,
        reviewStayRequestLO,
        reviewStayRequestCaseListedLO,
        reviewListingDirectionsLO,
        reviewListingDirectionsCaseListedLO,
        reviewWithdrawalRequestLO,
        reviewRule27RequestLO,
        reviewListCaseLO,
        reviewOtherRequestLO,
        reviewListCaseWithin5DaysLO,
        reviewPostponementRequestLO,
        reviewReinstatementRequestLO,
        reviewListCaseWithin5DaysJudge,
        reviewPostponementRequestJudge,
        reviewCorrectionsRequest,
        reviewWrittenReasonsRequest,
        reviewReinstatementRequestJudge,
        reviewSetAsideRequest,
        reviewStayRequestJudge,
        reviewNewCaseAndProvideDirectionsJudge,
        reviewOtherRequestJudge,
        reviewWithdrawalRequestJudge,
        reviewRule27RequestJudge,
        reviewListingDirectionsJudge,
        reviewListingDirectionsCaseListedJudge,
        reviewListCaseJudge,
        reviewStrikeOutRequestJudge,
        reviewTimeExtensionRequestJudge,
        reviewRule27RequestCaseListedLO,
        reviewWithdrawalRequestCaseListedLO,
        reviewWithdrawalRequestCaseListedJudge,
        reviewStayRequestCaseListedJudge,
        reviewRule27RequestCaseListedJudge
    );

    public static final Set<TaskType> LEGAL_OPERATIONS_TASKS = Set.of(
        reviewNewCaseAndProvideDirectionsLO,
        reviewTimeExtensionRequestLO,
        reviewStrikeOutRequestLO,
        reviewStayRequestLO,
        reviewStayRequestCaseListedLO,
        reviewListingDirectionsLO,
        reviewListingDirectionsCaseListedLO,
        reviewWithdrawalRequestLO,
        reviewRule27RequestLO,
        reviewListCaseLO,
        reviewOtherRequestLO,
        reviewListCaseWithin5DaysLO,
        reviewPostponementRequestLO,
        reviewReinstatementRequestLO,
        reviewRule27RequestCaseListedLO,
        reviewWithdrawalRequestCaseListedLO
    );

    public static final Set<TaskType> JUDICIAL_TASKS = Set.of(
        reviewListCaseWithin5DaysJudge,
        reviewPostponementRequestJudge,
        reviewCorrectionsRequest,
        reviewWrittenReasonsRequest,
        reviewReinstatementRequestJudge,
        reviewSetAsideRequest,
        reviewStayRequestJudge,
        reviewNewCaseAndProvideDirectionsJudge,
        reviewOtherRequestJudge,
        reviewWithdrawalRequestJudge,
        reviewRule27RequestJudge,
        reviewListingDirectionsJudge,
        reviewListingDirectionsCaseListedJudge,
        reviewListCaseJudge,
        reviewStrikeOutRequestJudge,
        reviewTimeExtensionRequestJudge,
        reviewWithdrawalRequestCaseListedJudge,
        reviewStayRequestCaseListedJudge,
        reviewRule27RequestCaseListedJudge
    );

    private TaskConstants() {
    }
}


