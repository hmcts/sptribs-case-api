package uk.gov.hmcts.sptribs.taskmanagement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.taskmanagement.TaskOutboxService;
import uk.gov.hmcts.ccd.sdk.taskmanagement.delay.DelayUntilRequest;
import uk.gov.hmcts.ccd.sdk.taskmanagement.delay.DelayUntilResolver;
import uk.gov.hmcts.ccd.sdk.taskmanagement.model.TaskPayload;
import uk.gov.hmcts.ccd.sdk.taskmanagement.model.TaskPermission;
import uk.gov.hmcts.ccd.sdk.taskmanagement.model.outbox.TerminateTaskOutboxPayload;
import uk.gov.hmcts.ccd.sdk.taskmanagement.model.request.TaskCreateRequest;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.sptribs.caseworker.model.CaseManagementLocation;
import uk.gov.hmcts.sptribs.caseworker.model.SecurityClass;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.taskmanagement.model.TaskType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskManagementServiceTest {

    private static final long CASE_ID = 1234567890L;

    @Mock
    private TaskOutboxService taskOutboxService;

    @Mock
    private DelayUntilResolver delayUntilResolver;

    @InjectMocks
    private TaskManagementService taskManagementService;

    @Test
    void shouldIgnoreNullOrEmptyInitiationTasks() {
        CaseData caseData = CaseData.builder().build();

        taskManagementService.enqueueInitiationTasks(null, caseData, CASE_ID);
        taskManagementService.enqueueInitiationTasks(List.of(), caseData, CASE_ID);

        verifyNoInteractions(taskOutboxService);
    }

    @Test
    void shouldIgnoreNullOrEmptyDelayedInitiationTasks() {
        CaseData caseData = CaseData.builder().build();
        DelayUntilRequest delayUntilRequest = DelayUntilRequest.builder().delayUntilOrigin("2026-01-02").build();

        taskManagementService.enqueueInitiationTasksWithDelay(null, caseData, CASE_ID, delayUntilRequest);
        taskManagementService.enqueueInitiationTasksWithDelay(List.of(), caseData, CASE_ID, delayUntilRequest);

        verifyNoInteractions(taskOutboxService);
        verifyNoInteractions(delayUntilResolver);
    }

    @Test
    void shouldEnqueueDistinctInitiationTasks() {
        CaseData caseData = CaseData.builder()
            .caseNameHmctsInternal("Case name")
            .build();

        taskManagementService.enqueueInitiationTasks(
            List.of(TaskType.registerNewCase, TaskType.registerNewCase, TaskType.reviewSetAsideRequest),
            caseData,
            CASE_ID
        );

        ArgumentCaptor<TaskCreateRequest> captor = ArgumentCaptor.forClass(TaskCreateRequest.class);
        verify(taskOutboxService, times(2)).enqueueTaskCreateRequest(captor.capture());

        List<TaskCreateRequest> requests = captor.getAllValues();
        assertThat(requests).extracting(request -> request.task().getType())
            .containsExactly("registerNewCase", "reviewSetAsideRequest");
    }

    @Test
    void shouldEnqueueDistinctDelayedInitiationTasksUsingResolvedDelayDate() {
        CaseData caseData = CaseData.builder()
            .caseNameHmctsInternal("Case name")
            .build();
        DelayUntilRequest delayUntilRequest = DelayUntilRequest.builder()
            .delayUntilOrigin("2026-01-02")
            .delayUntilIntervalDays(1)
            .build();
        LocalDateTime delayUntilDateTime = LocalDateTime.of(2026, 1, 5, 9, 0);
        when(delayUntilResolver.resolve(delayUntilRequest)).thenReturn(delayUntilDateTime);

        taskManagementService.enqueueInitiationTasksWithDelay(
            List.of(TaskType.registerNewCase, TaskType.registerNewCase, TaskType.reviewSetAsideRequest),
            caseData,
            CASE_ID,
            delayUntilRequest
        );

        ArgumentCaptor<TaskCreateRequest> requestCaptor = ArgumentCaptor.forClass(TaskCreateRequest.class);
        ArgumentCaptor<LocalDateTime> delayCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(taskOutboxService, times(2)).enqueueTaskCreateRequest(requestCaptor.capture(), delayCaptor.capture());

        assertThat(requestCaptor.getAllValues()).extracting(request -> request.task().getType())
            .containsExactly("registerNewCase", "reviewSetAsideRequest");
        assertThat(delayCaptor.getAllValues()).containsOnly(delayUntilDateTime);
    }

    @Test
    void shouldMapTaskPayloadWithDefaultsWhenCaseDataFieldsMissing() {
        CaseData caseData = CaseData.builder()
            .caseNameHmctsInternal("Case name")
            .build();

        taskManagementService.enqueueInitiationTasks(List.of(TaskType.registerNewCase), caseData, CASE_ID);

        ArgumentCaptor<TaskCreateRequest> captor = ArgumentCaptor.forClass(TaskCreateRequest.class);
        verify(taskOutboxService).enqueueTaskCreateRequest(captor.capture());
        TaskPayload payload = captor.getValue().task();

        assertThat(payload.getCaseId()).isEqualTo(String.valueOf(CASE_ID));
        assertThat(payload.getCaseTypeId()).isEqualTo("CriminalInjuriesCompensation");
        assertThat(payload.getCaseName()).isEqualTo("Case name");
        assertThat(payload.getCaseCategory()).isEqualTo("Criminal Injuries Compensation");
        assertThat(payload.getRegion()).isEqualTo("1");
        assertThat(payload.getLocation()).isEqualTo("336559");
        assertThat(payload.getSecurityClassification()).isEqualTo("PUBLIC");
        assertThat(payload.getTaskSystem()).isEqualTo("SELF");
        assertThat(payload.getExecutionType()).isEqualTo("Case Management Task");
        assertThat(payload.getPermissions())
            .extracting(TaskPermission::getRoleName)
            .containsExactly(
                "regional-centre-admin",
                "regional-centre-team-leader",
                "hearing-centre-admin",
                "hearing-centre-team-leader",
                "ctsc",
                "ctsc-team-leader",
                "task-supervisor");

        OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);
        assertThat(payload.getPriorityDate()).isBetween(nowUtc.minusMinutes(1), nowUtc.plusMinutes(1));
        assertThat(payload.getDueDateTime())
            .isBetween(nowUtc.plusDays(4), nowUtc.plusDays(6));
    }

    @Test
    void shouldMapTaskPayloadWithCaseDataOverridesAndLegalOperationsPermissions() {
        LocalDate dueDate = LocalDate.of(2026, 2, 10);
        CaseData caseData = CaseData.builder()
            .caseNameHmctsInternal("Case name")
            .dueDate(dueDate)
            .securityClass(SecurityClass.PRIVATE)
            .caseManagementCategory(DynamicList.builder()
                .value(DynamicListElement.builder().code(UUID.randomUUID()).label("Custom Category").build())
                .build())
            .caseManagementLocation(CaseManagementLocation.builder()
                .region("11")
                .baseLocation("366559")
                .build())
            .build();

        taskManagementService.enqueueInitiationTasks(
            List.of(TaskType.reviewNewCaseAndProvideDirectionsLO),
            caseData,
            CASE_ID
        );

        ArgumentCaptor<TaskCreateRequest> captor = ArgumentCaptor.forClass(TaskCreateRequest.class);
        verify(taskOutboxService).enqueueTaskCreateRequest(captor.capture());
        TaskPayload payload = captor.getValue().task();

        assertThat(payload.getSecurityClassification()).isEqualTo("PRIVATE");
        assertThat(payload.getCaseCategory()).isEqualTo("Custom Category");
        assertThat(payload.getRegion()).isEqualTo("11");
        assertThat(payload.getLocation()).isEqualTo("366559");
        assertThat(payload.getPriorityDate()).isEqualTo(dueDate.atStartOfDay().atOffset(ZoneOffset.UTC));
        assertThat(payload.getPermissions()).extracting(TaskPermission::getRoleName)
            .containsExactly("senior-tribunal-caseworker", "tribunal-caseworker", "task-supervisor");
    }

    @Test
    void shouldMapJudicialPermissionsWhenJudicialTaskInitiated() {
        taskManagementService.enqueueInitiationTasks(
            List.of(TaskType.reviewNewCaseAndProvideDirectionsJudge),
            CaseData.builder().build(),
            CASE_ID
        );

        ArgumentCaptor<TaskCreateRequest> captor = ArgumentCaptor.forClass(TaskCreateRequest.class);
        verify(taskOutboxService).enqueueTaskCreateRequest(captor.capture());
        TaskPayload payload = captor.getValue().task();

        assertThat(payload.getPermissions()).extracting(TaskPermission::getRoleName)
            .containsExactly("senior-judge", "judge", "task-supervisor");
    }

    @Test
    void shouldEnqueueCompletionAndCancellationRequests() {
        List<TaskType> taskTypes = List.of(TaskType.reviewOrder, TaskType.processFurtherEvidence);

        taskManagementService.enqueueCompletionTasks(taskTypes, CASE_ID);
        taskManagementService.enqueueCancellationTasks(taskTypes, CASE_ID);

        ArgumentCaptor<TerminateTaskOutboxPayload> completeCaptor = ArgumentCaptor.forClass(TerminateTaskOutboxPayload.class);
        verify(taskOutboxService).enqueueTaskCompleteRequest(completeCaptor.capture());
        assertThat(completeCaptor.getValue())
            .isEqualTo(new TerminateTaskOutboxPayload(
                String.valueOf(CASE_ID),
                "CriminalInjuriesCompensation",
                List.of("reviewOrder", "processFurtherEvidence")
            ));

        ArgumentCaptor<TerminateTaskOutboxPayload> cancelCaptor = ArgumentCaptor.forClass(TerminateTaskOutboxPayload.class);
        verify(taskOutboxService).enqueueTaskCancelRequest(cancelCaptor.capture());
        assertThat(cancelCaptor.getValue())
            .isEqualTo(new TerminateTaskOutboxPayload(
                String.valueOf(CASE_ID),
                "CriminalInjuriesCompensation",
                List.of("reviewOrder", "processFurtherEvidence")
            ));
    }

    @Test
    void shouldIgnoreNullOrEmptyCompletionAndCancellationTasks() {
        taskManagementService.enqueueCompletionTasks(null, CASE_ID);
        taskManagementService.enqueueCompletionTasks(List.of(), CASE_ID);
        taskManagementService.enqueueCancellationTasks(null, CASE_ID);
        taskManagementService.enqueueCancellationTasks(List.of(), CASE_ID);

        verify(taskOutboxService, never()).enqueueTaskCompleteRequest(any());
        verify(taskOutboxService, never()).enqueueTaskCancelRequest(any());
        verify(taskOutboxService, never()).enqueueTaskCreateRequest(any());
    }
}
