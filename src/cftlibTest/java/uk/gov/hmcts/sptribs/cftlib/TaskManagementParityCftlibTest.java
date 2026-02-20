package uk.gov.hmcts.sptribs.cftlib;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.rse.ccd.lib.test.CftlibTest;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.taskmanagement.model.ProcessCategoryIdentifiers;
import uk.gov.hmcts.sptribs.taskmanagement.TaskManagementService;
import uk.gov.hmcts.sptribs.taskmanagement.model.TaskType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
    "spring.jms.servicebus.enabled=false",
    "spring.autoconfigure.exclude=com.azure.spring.cloud.autoconfigure.implementation.jms.ServiceBusJmsAutoConfiguration",
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:init-task-outbox.sql"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskManagementParityCftlibTest extends CftlibTest {

    private static final String CASEWORKER_USER = "TEST_CASE_WORKER_USER@mailinator.com";
    private static final String BASE_URL = "http://localhost:4452";
    private static final String CASE_TYPE = "CriminalInjuriesCompensation";
    private static final String CREATE_CASE_EVENT = "caseworker-create-case";

    private static final String ACCEPT_CREATE_CASE =
        "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-case.v2+json;charset=UTF-8";
    private static final String ACCEPT_CREATE_EVENT =
        "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8";

    private static final List<TaskType> REVIEW_TASKS_EXCLUDING_REVIEW_ORDER = Stream.of(TaskType.values())
        .filter(taskType -> taskType.name().startsWith("review")
            && !taskType.name().startsWith("reviewSpecificAccess")
            && taskType != TaskType.reviewOrder)
        .toList();
    private static final List<TaskType> CREATE_DRAFT_ORDER_COMPLETABLE_TASKS =
        Stream.concat(REVIEW_TASKS_EXCLUDING_REVIEW_ORDER.stream(), Stream.of(TaskType.createDueDate)).toList();
    private static final List<TaskType> CREATE_AND_SEND_ORDER_COMPLETABLE_TASKS = REVIEW_TASKS_EXCLUDING_REVIEW_ORDER;
    private static final List<TaskType> SEND_ORDER_COMPLETABLE_TASKS = List.of(
        TaskType.processCaseWithdrawalDirections,
        TaskType.processCaseWithdrawalDirectionsListed,
        TaskType.processRule27Decision,
        TaskType.processRule27DecisionListed,
        TaskType.processListingDirections,
        TaskType.processListingDirectionsListed,
        TaskType.processDirectionsReListedCase,
        TaskType.processDirectionsReListedCaseWithin5Days,
        TaskType.processSetAsideDirections,
        TaskType.processCorrections,
        TaskType.processDirectionsReturned,
        TaskType.processPostponementDirections,
        TaskType.processTimeExtensionDirectionsReturned,
        TaskType.processReinstatementDecisionNotice,
        TaskType.processOtherDirectionsReturned,
        TaskType.processWrittenReasons,
        TaskType.processStrikeOutDirectionsReturned,
        TaskType.processStayDirections,
        TaskType.processStayDirectionsListed,
        TaskType.issueDueDate
    );
    private static final List<TaskType> REFERRAL_COMPLETABLE_TASKS =
        List.of(TaskType.followUpNoncomplianceOfDirections, TaskType.processFurtherEvidence);
    private static final List<TaskType> REFERRAL_CANCELLABLE_TASKS =
        TaskType.getTaskTypesFromProcessCategoryIdentifiers(List.of(ProcessCategoryIdentifiers.IssueCase));
    private static final List<TaskType> POSTPONE_HEARING_CANCELLABLE_TASKS =
        TaskType.getTaskTypesFromProcessCategoryIdentifiers(
            List.of(ProcessCategoryIdentifiers.HearingCompletion, ProcessCategoryIdentifiers.HearingBundle));
    private static final List<TaskType> CLOSE_CASE_CANCELLABLE_TASKS = List.of(TaskType.values());

    private static final Map<String, String> EVENT_FIXTURES = Map.ofEntries(
        Map.entry("create-draft-order", "src/functionalTest/resources/wa/caseworker-create-draft-order-submit-data.json"),
        Map.entry("create-and-send-order",
            "src/functionalTest/resources/request/casedata/ccd-callback-casedata-create-and-send-order-upload-callback-request.json"),
        Map.entry("caseworker-send-order",
            "src/functionalTest/resources/request/casedata/ccd-callback-casedata-send-order-callback-request.json"),
        Map.entry("create-hearing-summary", "src/functionalTest/resources/wa/caseworker-create-hearing-summary-submit-data.json"),
        Map.entry("caseworker-cancel-hearing", "src/functionalTest/resources/wa/caseworker-cancel-hearing-submit-data.json"),
        Map.entry("caseworker-postpone-hearing", "src/functionalTest/resources/wa/caseworker-postpone-hearing-submit-data.json"),
        Map.entry("caseworker-close-the-case",
            "src/functionalTest/resources/request/casedata/ccd-callback-casedata-caseworker-close-case-about-to-submit.json"),
        Map.entry("caseworker-issue-case",
            "src/functionalTest/resources/request/casedata/ccd-callback-casedata-caseworker-issue-case-about-to-submit.json"),
        Map.entry("caseworker-issue-decision",
            "src/functionalTest/resources/request/casedata/ccd-callback-casedata-caseworker-issue-decision-callback-request.json"),
        Map.entry("caseworker-issue-final-decision",
            "src/functionalTest/resources/request/casedata/ccd-callback-casedata-caseworker-issue-final-decision-callback-request.json"),
        Map.entry("refer-to-judge",
            "src/functionalTest/resources/request/casedata/ccd-callback-casedata-caseworker-refer-to-judge-callback-request.json"),
        Map.entry("refer-to-legal-officer",
            "src/functionalTest/resources/request/casedata/ccd-callback-casedata-caseworker-refer-to-legal-officer-callback-request.json"),
        Map.entry("edit-case",
            "src/functionalTest/resources/request/casedata/ccd-callback-casedata-caseworker-edit-case-request.json"),
        Map.entry("contact-parties",
            "src/functionalTest/resources/request/casedata/ccd-callback-casedata-caseworker-contact-parties-submitted.json"),
        Map.entry("caseworker-document-management",
            "src/functionalTest/resources/request/casedata/ccd-callback-casedata-caseworker-document-management-about-to-submit.json"),
        Map.entry("respondent-document-management",
            "src/functionalTest/resources/request/casedata/ccd-callback-casedata-caseworker-document-management-about-to-submit.json"),
        Map.entry("caseworker-amend-document",
            "src/functionalTest/resources/request/casedata/ccd-callback-casedata-caseworker-document-management-amend-request.json"),
        Map.entry("caseworker-amend-due-date",
            "src/functionalTest/resources/request/casedata/ccd-callback-casedata-caseworker-manage-order-due-date-callback-request.json"),
        Map.entry("createBundle",
            "src/functionalTest/resources/request/casedata/ccd-callback-casedata-caseworker-create-bundle-about-to-submit.json"),
        Map.entry("citizen-cic-submit-dss-application",
            "src/functionalTest/resources/request/casedata/ccd-callback-casedata-citizen-cic-create-case-about-to-submit.json"),
        Map.entry("citizen-cic-dss-update-case",
            "src/functionalTest/resources/request/casedata/ccd-callback-casedata-dss-update-case-submission-about-to-submit.json")
    );
    private static final Map<String, String> REFERRAL_REASON_CODES = Map.ofEntries(
        Map.entry("Corrections", "corrections"),
        Map.entry("Listed case", "listedCase"),
        Map.entry("Listed case (within 5 days)", "listedCaseWithin5Days"),
        Map.entry("Listing directions", "listingDirections"),
        Map.entry("New case", "newCase"),
        Map.entry("Postponement request", "postponementRequest"),
        Map.entry("Reinstatement request", "reinstatementRequest"),
        Map.entry("Rule 27 request", "rule27Request"),
        Map.entry("Set aside request", "setAsideRequest"),
        Map.entry("Stay request", "stayRequest"),
        Map.entry("Strike out request", "strikeOutRequest"),
        Map.entry("Time extension request", "timeExtensionRequest"),
        Map.entry("Withdrawal request", "withdrawalRequest"),
        Map.entry("Written reasons request", "writtenReasonsRequest"),
        Map.entry("Other", "other")
    );

    @Autowired
    private NamedParameterJdbcTemplate db;

    @Autowired
    private IdamClient idam;

    @Autowired
    private CoreCaseDataApi ccdApi;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TaskManagementService taskManagementService;

    private final Map<String, Map<String, Object>> fixtureCache = new HashMap<>();
    private Map<String, Object> createCaseSeedData;

    @BeforeAll
    void setUpSeedData() {
        createCaseSeedData = readJsonFile("src/main/resources/data/st_cic_test_case.json");
    }

    @Test
    void caseworkerCaseBuilt_shouldCompleteVetAndInitiateIssueCaseToRespondent() {
        runEventAndAssert(
            "caseworker-case-built",
            CASEWORKER_USER,
            State.Submitted,
            Map.of(),
            List.of(TaskType.issueCaseToRespondent),
            List.of(TaskType.vetNewCaseDocuments),
            List.of(),
            List.of(TaskType.vetNewCaseDocuments)
        );
    }

    @Test
    void caseworkerIssueCase_shouldCompleteIssueCaseTaskAndInitiateCreateDueDateTask() {
        runEventAndAssert(
            "caseworker-issue-case",
            CASEWORKER_USER,
            State.CaseManagement,
            Map.of(),
            List.of(TaskType.createDueDate),
            List.of(TaskType.issueCaseToRespondent),
            List.of(),
            List.of(TaskType.issueCaseToRespondent)
        );
    }

    @Test
    void caseworkerIssueDecision_shouldCompleteIssueDecisionNoticeTask() {
        runEventAndAssert(
            "caseworker-issue-decision",
            CASEWORKER_USER,
            State.AwaitingOutcome,
            Map.of(),
            List.of(),
            List.of(TaskType.issueDecisionNotice),
            List.of(),
            List.of(TaskType.issueDecisionNotice)
        );
    }

    @Test
    void caseworkerIssueFinalDecision_shouldCompleteIssueDecisionNoticeTask() {
        runEventAndAssert(
            "caseworker-issue-final-decision",
            CASEWORKER_USER,
            State.AwaitingOutcome,
            Map.of(),
            List.of(),
            List.of(TaskType.issueDecisionNotice),
            List.of(),
            List.of(TaskType.issueDecisionNotice)
        );
    }

    @Test
    void caseworkerCreateHearingSummary_shouldCompleteHearingOutcomeAndInitiateIssueDecisionNotice() {
        runEventAndAssert(
            "create-hearing-summary",
            CASEWORKER_USER,
            State.AwaitingHearing,
            Map.of(),
            List.of(TaskType.issueDecisionNotice),
            List.of(TaskType.completeHearingOutcome),
            List.of(),
            List.of(TaskType.completeHearingOutcome)
        );
    }

    @Test
    void caseworkerDocumentManagement_shouldCompleteFollowUpAndInitiateProcessFurtherEvidence() {
        runEventAndAssert(
            "caseworker-document-management",
            CASEWORKER_USER,
            State.CaseManagement,
            Map.of(),
            List.of(TaskType.processFurtherEvidence),
            List.of(TaskType.followUpNoncomplianceOfDirections),
            List.of(),
            List.of(TaskType.followUpNoncomplianceOfDirections)
        );
    }

    @Test
    void respondentDocumentManagement_shouldInitiateProcessFurtherEvidence() {
        runEventAndAssert(
            "respondent-document-management",
            CASEWORKER_USER,
            State.CaseManagement,
            Map.of(),
            List.of(TaskType.processFurtherEvidence),
            List.of(),
            List.of(),
            List.of()
        );
    }

    @Test
    void caseworkerDocumentManagementAmend_shouldCompleteProcessFurtherEvidence() {
        runEventAndAssert(
            "caseworker-amend-document",
            CASEWORKER_USER,
            State.CaseManagement,
            Map.of(),
            List.of(),
            List.of(TaskType.processFurtherEvidence),
            List.of(),
            List.of(TaskType.processFurtherEvidence)
        );
    }

    @Test
    void caseworkerContactParties_shouldCompleteFollowUpAndProcessFurtherEvidence() {
        runEventAndAssert(
            "contact-parties",
            CASEWORKER_USER,
            State.CaseManagement,
            Map.of(),
            List.of(),
            List.of(TaskType.followUpNoncomplianceOfDirections, TaskType.processFurtherEvidence),
            List.of(),
            List.of(TaskType.followUpNoncomplianceOfDirections, TaskType.processFurtherEvidence)
        );
    }

    @Test
    void caseworkerAmendDueDate_shouldCompleteFollowUpNoncomplianceTask() {
        runEventAndAssert(
            "caseworker-amend-due-date",
            CASEWORKER_USER,
            State.CaseManagement,
            Map.of(),
            List.of(),
            List.of(TaskType.followUpNoncomplianceOfDirections),
            List.of(),
            List.of(TaskType.followUpNoncomplianceOfDirections)
        );
    }

    @Test
    void createBundle_shouldCompleteStitchCollateHearingBundleTask() {
        runEventAndAssert(
            "createBundle",
            CASEWORKER_USER,
            State.CaseManagement,
            Map.of(),
            List.of(),
            List.of(TaskType.stitchCollateHearingBundle),
            List.of(),
            List.of(TaskType.stitchCollateHearingBundle)
        );
    }

    @Test
    void caseworkerCancelHearing_shouldCancelCompleteHearingOutcomeAndStitchBundleTasks() {
        runEventAndAssert(
            "caseworker-cancel-hearing",
            CASEWORKER_USER,
            State.AwaitingHearing,
            Map.of(),
            List.of(),
            List.of(),
            List.of(TaskType.completeHearingOutcome, TaskType.stitchCollateHearingBundle),
            List.of(TaskType.completeHearingOutcome, TaskType.stitchCollateHearingBundle)
        );
    }

    @Test
    void caseworkerPostponeHearing_shouldCancelHearingCompletionAndBundleTasks() {
        runEventAndAssert(
            "caseworker-postpone-hearing",
            CASEWORKER_USER,
            State.AwaitingHearing,
            Map.of(),
            List.of(),
            List.of(),
            POSTPONE_HEARING_CANCELLABLE_TASKS,
            POSTPONE_HEARING_CANCELLABLE_TASKS
        );
    }

    @Test
    void caseworkerCloseTheCase_shouldCancelAllKnownTaskTypes() {
        runEventAndAssert(
            "caseworker-close-the-case",
            CASEWORKER_USER,
            State.CaseManagement,
            Map.of(),
            List.of(),
            List.of(),
            CLOSE_CASE_CANCELLABLE_TASKS,
            List.of()
        );
    }

    @Test
    void systemTriggerCompleteHearingOutcome_shouldInitiateCompleteHearingOutcomeTask() {
        runEventAndAssert(
            "system-trigger-complete-hearing-outcome",
            CASEWORKER_USER,
            State.AwaitingHearing,
            Map.of(),
            List.of(TaskType.completeHearingOutcome),
            List.of(),
            List.of(),
            List.of()
        );
    }

    @Test
    void systemTriggerStitchCollateHearingBundle_shouldInitiateStitchCollateTask() {
        runEventAndAssert(
            "system-trigger-stitch-collate-hearing-bundle",
            CASEWORKER_USER,
            State.AwaitingHearing,
            Map.of(),
            List.of(TaskType.stitchCollateHearingBundle),
            List.of(),
            List.of(),
            List.of()
        );
    }

    @Test
    void citizenSubmitCase_shouldInitiateRegisterNewCaseTask() {
        runEventAndAssert(
            "citizen-cic-submit-dss-application",
            CASEWORKER_USER,
            State.DSS_Draft,
            Map.of(),
            List.of(TaskType.registerNewCase),
            List.of(),
            List.of(),
            List.of()
        );
    }

    @Test
    void citizenDssUpdateCase_shouldInitiateProcessFurtherEvidenceTask() {
        runEventAndAssert(
            "citizen-cic-dss-update-case",
            CASEWORKER_USER,
            State.DSS_Submitted,
            Map.of(),
            List.of(TaskType.processFurtherEvidence),
            List.of(),
            List.of(),
            List.of()
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("caseworkerSendOrderScenarios")
    void caseworkerSendOrder_shouldCompleteExpectedTasksAndInitiateExpectedTasks(
        String scenario,
        State preState,
        List<TaskType> expectedInitiation
    ) {
        runEventAndAssert(
            "caseworker-send-order",
            CASEWORKER_USER,
            preState,
            Map.of(),
            expectedInitiation,
            SEND_ORDER_COMPLETABLE_TASKS,
            List.of(),
            List.of()
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("createAndSendOrderScenarios")
    void createAndSendOrder_shouldCompleteExpectedTasksAndInitiateExpectedTasks(
        String scenario,
        State preState,
        Map<String, Object> overlay,
        List<TaskType> expectedInitiation
    ) {
        runEventAndAssert(
            "create-and-send-order",
            CASEWORKER_USER,
            preState,
            overlay,
            expectedInitiation,
            CREATE_AND_SEND_ORDER_COMPLETABLE_TASKS,
            List.of(),
            List.of()
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("editCaseScenarios")
    void editCase_shouldCompleteExpectedTasksAndInitiateExpectedTasks(
        String scenario,
        State preState,
        List<TaskType> expectedInitiation
    ) {
        runEventAndAssert(
            "edit-case",
            CASEWORKER_USER,
            preState,
            Map.of(),
            expectedInitiation,
            List.of(TaskType.registerNewCase, TaskType.processFurtherEvidence),
            List.of(),
            List.of(TaskType.registerNewCase, TaskType.processFurtherEvidence)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("createDraftOrderScenarios")
    void createDraftOrder_shouldCompleteExpectedTasksAndInitiateExpectedTasks(
        String scenario,
        State preState,
        Map<String, Object> overlay,
        List<TaskType> expectedInitiation
    ) {
        runEventAndAssert(
            "create-draft-order",
            CASEWORKER_USER,
            preState,
            overlay,
            expectedInitiation,
            CREATE_DRAFT_ORDER_COMPLETABLE_TASKS,
            List.of(),
            List.of()
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("referToJudgeScenarios")
    void referToJudge_shouldCancelAndCompleteExpectedTasksAndInitiateExpectedJudgeTask(
        String scenario,
        State preState,
        Map<String, Object> overlay,
        List<TaskType> expectedInitiation
    ) {
        runEventAndAssert(
            "refer-to-judge",
            CASEWORKER_USER,
            preState,
            overlay,
            expectedInitiation,
            REFERRAL_COMPLETABLE_TASKS,
            REFERRAL_CANCELLABLE_TASKS,
            combineDistinct(REFERRAL_COMPLETABLE_TASKS, REFERRAL_CANCELLABLE_TASKS)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("referToLegalOfficerScenarios")
    void referToLegalOfficer_shouldCancelAndCompleteExpectedTasksAndInitiateExpectedLoTask(
        String scenario,
        State preState,
        Map<String, Object> overlay,
        List<TaskType> expectedInitiation
    ) {
        runEventAndAssert(
            "refer-to-legal-officer",
            CASEWORKER_USER,
            preState,
            overlay,
            expectedInitiation,
            REFERRAL_COMPLETABLE_TASKS,
            REFERRAL_CANCELLABLE_TASKS,
            combineDistinct(REFERRAL_COMPLETABLE_TASKS, REFERRAL_CANCELLABLE_TASKS)
        );
    }

    private Stream<Arguments> caseworkerSendOrderScenarios() {
        return Stream.of(
            arguments(
                "caseworker-send-order from CaseManagement initiates followUpNoncomplianceOfDirections",
                State.CaseManagement,
                List.of(TaskType.followUpNoncomplianceOfDirections)
            ),
            arguments(
                "caseworker-send-order from AwaitingHearing does not initiate new tasks",
                State.AwaitingHearing,
                List.of()
            )
        );
    }

    private Stream<Arguments> createAndSendOrderScenarios() {
        return Stream.of(
            arguments(
                "create-and-send-order with first due date in CaseManagement initiates followUpNoncomplianceOfDirections",
                State.CaseManagement,
                Map.of("cicCaseFirstOrderDueDate", "2026-04-02"),
                List.of(TaskType.followUpNoncomplianceOfDirections)
            ),
            arguments(
                "create-and-send-order with AdminActionRequired in CaseManagement initiates reviewOrder",
                State.CaseManagement,
                Map.of("cicCaseAdminActionRequired", List.of("AdminActionRequired")),
                List.of(TaskType.reviewOrder)
            ),
            arguments(
                "create-and-send-order with due date and AdminActionRequired in CaseManagement initiates both tasks",
                State.CaseManagement,
                Map.of(
                    "cicCaseAdminActionRequired", List.of("AdminActionRequired"),
                    "cicCaseFirstOrderDueDate", "2026-04-02"
                ),
                List.of(TaskType.followUpNoncomplianceOfDirections, TaskType.reviewOrder)
            ),
            arguments(
                "create-and-send-order with AdminActionRequired in ReadyToList initiates reviewOrder",
                State.ReadyToList,
                Map.of("cicCaseAdminActionRequired", List.of("AdminActionRequired")),
                List.of(TaskType.reviewOrder)
            ),
            arguments(
                "create-and-send-order with AdminActionRequired in AwaitingHearing initiates reviewOrder",
                State.AwaitingHearing,
                Map.of("cicCaseAdminActionRequired", List.of("AdminActionRequired")),
                List.of(TaskType.reviewOrder)
            )
        );
    }

    private Stream<Arguments> editCaseScenarios() {
        return Stream.of(
            arguments(
                "edit-case from Submitted initiates vetNewCaseDocuments",
                State.Submitted,
                List.of(TaskType.vetNewCaseDocuments)
            ),
            arguments(
                "edit-case from CaseManagement does not initiate vet task",
                State.CaseManagement,
                List.of()
            )
        );
    }

    private Stream<Arguments> createDraftOrderScenarios() {
        return Stream.of(
            arguments(
                "create-draft-order CaseManagement with blank referral initiates issueDueDate",
                State.CaseManagement,
                referralOverlay(""),
                List.of(TaskType.issueDueDate)
            ),
            arguments(
                "create-draft-order AwaitingHearing withdrawal request initiates processCaseWithdrawalDirectionsListed",
                State.AwaitingHearing,
                referralOverlay("Withdrawal request"),
                List.of(TaskType.processCaseWithdrawalDirectionsListed)
            ),
            arguments(
                "create-draft-order CaseManagement listing directions initiates processListingDirections",
                State.CaseManagement,
                referralOverlay("Listing directions"),
                List.of(TaskType.processListingDirections)
            ),
            arguments(
                "create-draft-order CaseClosed set aside request initiates processSetAsideDirections",
                State.CaseClosed,
                referralOverlay("Set aside request"),
                List.of(TaskType.processSetAsideDirections)
            ),
            arguments(
                "create-draft-order CaseManagement stay request initiates processStayDirections",
                State.CaseManagement,
                referralOverlay("Stay request"),
                List.of(TaskType.processStayDirections)
            )
        );
    }

    private Stream<Arguments> referToJudgeScenarios() {
        return Stream.of(
            arguments("refer-to-judge new case in CaseManagement initiates reviewNewCaseAndProvideDirectionsJudge",
                State.CaseManagement, referralOverlay("New case"), List.of(TaskType.reviewNewCaseAndProvideDirectionsJudge)),
            arguments("refer-to-judge stay request in AwaitingHearing initiates reviewStayRequestCaseListedJudge",
                State.AwaitingHearing, referralOverlay("Stay request"), List.of(TaskType.reviewStayRequestCaseListedJudge)),
            arguments("refer-to-judge listing directions in ReadyToList initiates reviewListingDirectionsCaseListedJudge",
                State.ReadyToList, referralOverlay("Listing directions"),
                List.of(TaskType.reviewListingDirectionsCaseListedJudge)),
            arguments("refer-to-judge corrections in CaseClosed initiates reviewCorrectionsRequest",
                State.CaseClosed, referralOverlay("Corrections"), List.of(TaskType.reviewCorrectionsRequest)),
            arguments("refer-to-judge other request in CaseManagement initiates reviewOtherRequestJudge",
                State.CaseManagement, referralOverlay("Other"), List.of(TaskType.reviewOtherRequestJudge))
        );
    }

    private Stream<Arguments> referToLegalOfficerScenarios() {
        return Stream.of(
            arguments("refer-to-legal-officer new case in CaseManagement initiates reviewNewCaseAndProvideDirectionsLO",
                State.CaseManagement, referralOverlay("New case"), List.of(TaskType.reviewNewCaseAndProvideDirectionsLO)),
            arguments("refer-to-legal-officer stay request in AwaitingHearing initiates reviewStayRequestCaseListedLO",
                State.AwaitingHearing, referralOverlay("Stay request"), List.of(TaskType.reviewStayRequestCaseListedLO)),
            arguments("refer-to-legal-officer listing directions in ReadyToList initiates reviewListingDirectionsCaseListedLO",
                State.ReadyToList, referralOverlay("Listing directions"),
                List.of(TaskType.reviewListingDirectionsCaseListedLO)),
            arguments("refer-to-legal-officer reinstatement request in CaseClosed initiates reviewReinstatementRequestLO",
                State.CaseClosed, referralOverlay("Reinstatement request"), List.of(TaskType.reviewReinstatementRequestLO)),
            arguments("refer-to-legal-officer other request in CaseManagement initiates reviewOtherRequestLO",
                State.CaseManagement, referralOverlay("Other"), List.of(TaskType.reviewOtherRequestLO))
        );
    }

    private void runEventAndAssert(
        String eventId,
        String user,
        State preState,
        Map<String, Object> scenarioOverlay,
        List<TaskType> expectedInitiation,
        List<TaskType> expectedCompletion,
        List<TaskType> expectedCancellation,
        List<TaskType> tasksToSeedForTermination
    ) {
        long caseId = createCase();
        clearTaskOutbox(caseId);

        Map<String, Object> overlay = buildEventOverlay(eventId, scenarioOverlay);
        updateCaseStateAndData(caseId, preState, overlay);
        List<TaskType> tasksToSeed = tasksToSeedForTermination.isEmpty()
            ? combineDistinct(expectedCompletion, expectedCancellation)
            : tasksToSeedForTermination;
        seedTasksForTermination(caseId, tasksToSeed);

        submitEvent(caseId, user, eventId, overlay);

        assertInitiationOutbox(caseId, expectedInitiation, eventId);
        assertTerminationOutbox(caseId, "complete", expectedCompletion, eventId);
        assertTerminationOutbox(caseId, "cancel", expectedCancellation, eventId);
    }

    private long createCase() {
        StartEventResponse start = ccdApi.startCase(
            getAuthorisation(CASEWORKER_USER),
            getServiceAuth(),
            CASE_TYPE,
            CREATE_CASE_EVENT
        );

        Map<String, Object> submissionData = new HashMap<>(toMap(start.getCaseDetails().getData()));
        deepMerge(submissionData, cloneMap(createCaseSeedData));

        Map<String, Object> body = new HashMap<>();
        body.put("data", submissionData);
        body.put("event", Map.of("id", CREATE_CASE_EVENT, "summary", "", "description", ""));
        body.put("event_token", start.getToken());
        body.put("ignore_warning", false);

        HttpPost request = buildRequest(
            CASEWORKER_USER,
            BASE_URL + "/data/case-types/" + CASE_TYPE + "/cases?ignore-warning=false"
        );
        withCcdAccept(request, ACCEPT_CREATE_CASE);

        try {
            request.setEntity(new StringEntity(mapper.writeValueAsString(body), ContentType.APPLICATION_JSON));
            var response = HttpClientBuilder.create().build().execute(request);
            try {
                int status = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                assertThat(status).as("Case creation must return 201. body=%s", responseBody).isEqualTo(201);
                Map<String, Object> payload = mapper.readValue(responseBody, new TypeReference<>() {
                });
                return Long.parseLong(String.valueOf(payload.get("id")));
            } finally {
                response.close();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create CCD case", ex);
        }
    }

    private void submitEvent(long caseId, String user, String eventId, Map<String, Object> overlay) {
        StartEventResponse start = ccdApi.startEvent(
            getAuthorisation(user),
            getServiceAuth(),
            String.valueOf(caseId),
            eventId
        );

        Map<String, Object> submissionData = new HashMap<>(toMap(start.getCaseDetails().getData()));
        deepMerge(submissionData, cloneMap(overlay));

        Map<String, Object> body = new HashMap<>();
        body.put("data", submissionData);
        body.put("event", Map.of("id", eventId, "summary", "task-management-functional", "description", "task-management-functional"));
        body.put("event_token", start.getToken());
        body.put("ignore_warning", false);

        HttpPost request = buildRequest(user, BASE_URL + "/cases/" + caseId + "/events");
        withCcdAccept(request, ACCEPT_CREATE_EVENT);

        try {
            request.setEntity(new StringEntity(mapper.writeValueAsString(body), ContentType.APPLICATION_JSON));
            var response = HttpClientBuilder.create().build().execute(request);
            try {
                int status = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                assertThat(status).as("Event submit must return 201 for %s. body=%s", eventId, responseBody).isEqualTo(201);
            } finally {
                response.close();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to submit event " + eventId, ex);
        }
    }

    private void updateCaseStateAndData(long caseId, State state, Map<String, Object> overlay) {
        Map<String, Object> caseData = loadCaseData(caseId);
        deepMerge(caseData, cloneMap(overlay));
        caseData.put("caseStatus", state.name());

        try {
            db.update(
                "UPDATE ccd.case_data SET state = :state, data = CAST(:data AS jsonb) WHERE reference = :ref",
                Map.of(
                    "state", state.name(),
                    "data", mapper.writeValueAsString(caseData),
                    "ref", caseId
                )
            );
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to update case data for " + caseId, ex);
        }
    }

    private Map<String, Object> loadCaseData(long caseId) {
        String json = db.queryForObject(
            "SELECT data::text FROM ccd.case_data WHERE reference = :ref",
            Map.of("ref", caseId),
            String.class
        );
        return parseJsonObject(json);
    }

    private void clearTaskOutbox(long caseId) {
        db.update("DELETE FROM ccd.task_outbox WHERE case_id = :caseId", Map.of("caseId", String.valueOf(caseId)));
    }

    private void seedTasksForTermination(long caseId, List<TaskType> taskTypes) {
        List<TaskType> distinctTaskTypes = taskTypes.stream().distinct().toList();
        if (distinctTaskTypes.isEmpty()) {
            return;
        }

        CaseData caseData = mapper.convertValue(loadCaseData(caseId), CaseData.class);
        taskManagementService.enqueueInitiationTasks(distinctTaskTypes, caseData, caseId);

        await().atMost(Duration.ofSeconds(45)).untilAsserted(() -> {
            List<Map<String, Object>> rows = db.queryForList(
                "SELECT status, last_response_code FROM ccd.task_outbox "
                    + "WHERE case_id = :caseId AND action = :action::ccd.task_action",
                Map.of("caseId", String.valueOf(caseId), "action", "initiate")
            );
            assertThat(rows).hasSize(distinctTaskTypes.size());
            rows.forEach(this::assertProcessedRow);
        });

        clearTaskOutbox(caseId);
    }

    private void assertInitiationOutbox(long caseId, List<TaskType> expectedTaskTypes, String eventId) {
        List<String> expectedTypes = expectedTaskTypes.stream().map(Enum::name).distinct().toList();
        if (expectedTypes.isEmpty()) {
            assertNoTaskOutboxAction(caseId, "initiate");
            return;
        }

        await().atMost(Duration.ofSeconds(45)).untilAsserted(() -> {
            List<Map<String, Object>> rows = db.queryForList(
                "SELECT status, last_response_code, payload->'task'->>'type' AS task_type "
                    + "FROM ccd.task_outbox WHERE case_id = :caseId AND action = :action::ccd.task_action",
                Map.of("caseId", String.valueOf(caseId), "action", "initiate")
            );
            assertThat(rows).hasSize(expectedTypes.size());
            assertThat(rows.stream().map(row -> String.valueOf(row.get("task_type"))).toList())
                .as("Initiation outbox mismatch for event %s", eventId)
                .containsExactlyInAnyOrderElementsOf(expectedTypes);
            rows.forEach(this::assertProcessedRow);
        });
    }

    private void assertTerminationOutbox(long caseId, String action, List<TaskType> expectedTaskTypes, String eventId) {
        List<String> expectedTypes = expectedTaskTypes.stream().map(Enum::name).distinct().toList();
        if (expectedTypes.isEmpty()) {
            assertNoTaskOutboxAction(caseId, action);
            return;
        }

        await().atMost(Duration.ofSeconds(45)).untilAsserted(() -> {
            Map<String, Object> row = db.queryForMap(
                "SELECT status, last_response_code, "
                    + "COALESCE(payload->'task_types', payload->'task_type', payload->'taskType')::text AS task_types "
                    + "FROM ccd.task_outbox WHERE case_id = :caseId AND action = :action::ccd.task_action "
                    + "ORDER BY id DESC LIMIT 1",
                Map.of("caseId", String.valueOf(caseId), "action", action.toLowerCase(Locale.ROOT))
            );
            assertProcessedRow(row);
            List<String> actualTypes = parseJsonArray(row.get("task_types"));
            assertThat(new LinkedHashSet<>(actualTypes))
                .as("Termination outbox mismatch for event %s action %s", eventId, action)
                .containsExactlyInAnyOrderElementsOf(new LinkedHashSet<>(expectedTypes));
        });
    }

    private void assertNoTaskOutboxAction(long caseId, String action) {
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            Integer count = db.queryForObject(
                "SELECT count(*) FROM ccd.task_outbox WHERE case_id = :caseId AND action = :action::ccd.task_action",
                Map.of("caseId", String.valueOf(caseId), "action", action.toLowerCase(Locale.ROOT)),
                Integer.class
            );
            assertThat(count).isEqualTo(0);
        });
    }

    private void assertProcessedRow(Map<String, Object> row) {
        assertThat(row.get("status")).isEqualTo("PROCESSED");
        Object responseCode = row.get("last_response_code");
        assertThat(responseCode).isNotNull();
        assertThat(((Number) responseCode).intValue()).isIn(200, 201, 204);
    }

    private Map<String, Object> buildEventOverlay(String eventId, Map<String, Object> scenarioOverlay) {
        Map<String, Object> overlay = cloneMap(loadFixtureForEvent(eventId));
        deepMerge(overlay, cloneMap(scenarioOverlay));
        applyEventDefaults(eventId, overlay);
        return overlay;
    }

    private Map<String, Object> loadFixtureForEvent(String eventId) {
        String path = EVENT_FIXTURES.get(eventId);
        if (path == null) {
            return Collections.emptyMap();
        }
        return fixtureCache.computeIfAbsent(path, this::readJsonFile);
    }

    private void applyEventDefaults(String eventId, Map<String, Object> overlay) {
        if ("create-draft-order".equals(eventId)) {
            overlay.putIfAbsent("orderContentOrderTemplate", "CIC3_Rule_27");
            overlay.putIfAbsent("orderContentMainContent", "Order Main Content");
            overlay.putIfAbsent("orderContentOrderSignature", "Order Signatory");
            overlay.putIfAbsent("cicCaseOrderTemplateIssued", sampleDocument("DRAFT :Order--[Subject Name]--26-04-2024 10:09:12.pdf"));
        }

        if ("create-and-send-order".equals(eventId)) {
            overlay.putIfAbsent("cicCaseOrderIssuingType", "UploadOrder");
            overlay.putIfAbsent("cicCaseAdminActionRequired", null);
            overlay.putIfAbsent("cicCaseFirstOrderDueDate", null);
            overlay.putIfAbsent("cicCaseOrderDueDates", List.of());
            Object firstDueDate = overlay.get("cicCaseFirstOrderDueDate");
            if (firstDueDate instanceof String dueDate && !dueDate.isBlank()) {
                overlay.put(
                    "cicCaseOrderDueDates",
                    List.of(Map.of("id", "1", "value", Map.of("dueDate", dueDate)))
                );
            }
        }

        if ("contact-parties".equals(eventId)) {
            overlay.remove("contactPartiesDocumentsDocumentList");
            overlay.put(
                "cicCaseApplicantDocumentsUploaded",
                List.of(
                    Map.of(
                        "id", "1",
                        "value", Map.of(
                            "documentCategory", "ApplicationForm",
                            "documentEmailContent", "Party message",
                            "documentLink", sampleDocument("contact-parties.pdf")
                        )
                    )
                )
            );
        }

        if ("citizen-cic-submit-dss-application".equals(eventId)) {
            overlay.putIfAbsent("dssCaseDataSubjectFullName", "DSS Subject");
            overlay.putIfAbsent("dssCaseDataSubjectDateOfBirth", "1990-01-01");
            overlay.putIfAbsent("dssCaseDataSubjectEmailAddress", "subject@mailinator.com");
            overlay.putIfAbsent("dssCaseDataSubjectContactNumber", "07000000000");
            overlay.putIfAbsent("dssCaseDataSubjectAgreeContact", "Yes");
            overlay.putIfAbsent("dssCaseDataRepresentation", "No");
            overlay.putIfAbsent("dssCaseDataRepresentationQualified", "No");
        }

        if ("citizen-cic-dss-update-case".equals(eventId)) {
            overlay.putIfAbsent("dssCaseDataAdditionalInformation", "Additional info");
            overlay.putIfAbsent("dssCaseDataOtherInfoDocuments", List.of());
        }

        if ("caseworker-issue-decision".equals(eventId)) {
            Object draftDocument = overlay.get("caseIssueDecisionIssueDecisionDraft");
            if (draftDocument instanceof Map<?, ?> map && map.isEmpty()) {
                overlay.remove("caseIssueDecisionIssueDecisionDraft");
            }
        }

        if ("refer-to-judge".equals(eventId)) {
            applyReferralReasonCode(overlay, "referToJudgeReferralReason");
        }

        if ("refer-to-legal-officer".equals(eventId)) {
            applyReferralReasonCode(overlay, "referToLegalOfficerReferralReason");
        }
    }

    private void applyReferralReasonCode(Map<String, Object> overlay, String referralReasonField) {
        Object referralLabel = overlay.get("cicCaseReferralTypeForWA");
        if (referralLabel == null) {
            return;
        }

        String reasonCode = REFERRAL_REASON_CODES.get(String.valueOf(referralLabel));
        if (reasonCode != null) {
            overlay.put(referralReasonField, reasonCode);
        }
    }

    private Map<String, Object> sampleDocument(String filename) {
        String base = "http://localhost/documents/11111111-1111-1111-1111-111111111111";
        return Map.of(
            "document_url", base,
            "document_binary_url", base + "/binary",
            "document_filename", filename,
            "category_id", "TD"
        );
    }

    private String getAuthorisation(String user) {
        return idam.getAccessToken(user, "");
    }

    private String getServiceAuth() {
        return cftlib().generateDummyS2SToken("ccd_gw");
    }

    private HttpPost buildRequest(String user, String url) {
        HttpPost request = new HttpPost(url);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("ServiceAuthorization", getServiceAuth());
        request.addHeader("Authorization", getAuthorisation(user));
        return request;
    }

    private void withCcdAccept(HttpRequestBase request, String accept) {
        request.addHeader("experimental", "true");
        request.addHeader("Accept", accept);
    }

    @SuppressWarnings("unchecked")
    private void deepMerge(Map<String, Object> target, Map<String, Object> updates) {
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            Object existing = target.get(entry.getKey());
            Object update = entry.getValue();
            if (existing instanceof Map<?, ?> existingMap && update instanceof Map<?, ?> updateMap) {
                deepMerge((Map<String, Object>) existingMap, (Map<String, Object>) updateMap);
            } else {
                target.put(entry.getKey(), update);
            }
        }
    }

    private Map<String, Object> readJsonFile(String filePath) {
        try {
            return parseJsonObject(Files.readString(Path.of(filePath)));
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read JSON file: " + filePath, ex);
        }
    }

    private Map<String, Object> parseJsonObject(String json) {
        try {
            return mapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to parse JSON object", ex);
        }
    }

    private List<String> parseJsonArray(Object jsonValue) {
        if (jsonValue == null) {
            return List.of();
        }
        try {
            return mapper.readValue(String.valueOf(jsonValue), new TypeReference<>() {
            });
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to parse JSON array: " + jsonValue, ex);
        }
    }

    private Map<String, Object> cloneMap(Map<String, Object> map) {
        return mapper.convertValue(map, new TypeReference<>() {
        });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object data) {
        return mapper.convertValue(data, new TypeReference<>() {
        });
    }

    private Map<String, Object> referralOverlay(String referralType) {
        return Map.of("cicCaseReferralTypeForWA", referralType);
    }

    private List<TaskType> combineDistinct(List<TaskType> first, List<TaskType> second) {
        LinkedHashSet<TaskType> result = new LinkedHashSet<>(first);
        result.addAll(second);
        return List.copyOf(result);
    }
}
