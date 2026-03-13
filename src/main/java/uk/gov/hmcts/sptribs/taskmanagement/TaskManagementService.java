package uk.gov.hmcts.sptribs.taskmanagement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.taskmanagement.TaskOutboxService;
import uk.gov.hmcts.ccd.sdk.taskmanagement.model.TaskPayload;
import uk.gov.hmcts.ccd.sdk.taskmanagement.model.TaskPermission;
import uk.gov.hmcts.ccd.sdk.taskmanagement.model.outbox.TerminateTaskOutboxPayload;
import uk.gov.hmcts.ccd.sdk.taskmanagement.model.request.TaskCreateRequest;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.taskmanagement.model.TaskAccess;
import uk.gov.hmcts.sptribs.taskmanagement.model.TaskType;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.CRIMINAL_INJURIES_COMPENSATION;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.DEFAULT_EXECUTION_TYPE;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.DEFAULT_LOCATION;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.DEFAULT_REGION;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.DEFAULT_SECURITY_CLASSIFICATION;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.DEFAULT_TASK_SYSTEM;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.GLASGOW_TRIBUNALS_CENTRE;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.JUDICIAL_TASKS;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.JURISDICTION;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.LEGAL_OPERATIONS_TASKS;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.MAJOR_PRIORITY;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.MINOR_PRIORITY;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskAccess.CTSC;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskAccess.CTSC_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskAccess.HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskAccess.HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskAccess.JUDGE;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskAccess.REGIONAL_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskAccess.REGIONAL_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskAccess.SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskAccess.SENIOR_TRIBUNAL_CASEWORKER;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskAccess.TRIBUNAL_CASEWORKER;

@Service
@RequiredArgsConstructor
public class TaskManagementService {

    private final TaskOutboxService taskOutboxService;

    public void enqueueInitiationTasks(List<TaskType> taskTypes, CaseData caseData, long caseId) {
        if (taskTypes == null || taskTypes.isEmpty()) {
            return;
        }

        taskTypes.stream()
            .distinct()
            .map(taskType -> new TaskCreateRequest(getTaskPayload(taskType, caseData, caseId)))
            .forEach(taskOutboxService::enqueueTaskCreateRequest);
    }

    public void enqueueCompletionTasks(List<TaskType> taskTypes, long caseId) {
        enqueueTaskTermination(taskTypes, caseId, true);
    }

    public void enqueueCancellationTasks(List<TaskType> taskTypes, long caseId) {
        enqueueTaskTermination(taskTypes, caseId, false);
    }

    private void enqueueTaskTermination(List<TaskType> taskTypes, long caseId, boolean completion) {
        if (taskTypes == null || taskTypes.isEmpty()) {
            return;
        }

        TerminateTaskOutboxPayload taskOutboxPayload = new TerminateTaskOutboxPayload(
            String.valueOf(caseId),
            CIC_CASE_TYPE,
            taskTypes.stream().map(Enum::name).toList()
        );

        if (completion) {
            taskOutboxService.enqueueTaskCompleteRequest(taskOutboxPayload);
        } else {
            taskOutboxService.enqueueTaskCancelRequest(taskOutboxPayload);
        }
    }

    private TaskPayload getTaskPayload(TaskType taskType, CaseData caseData, long caseId) {
        return TaskPayload.builder()
            .externalTaskId(UUID.randomUUID().toString())
            .name(taskType.getName())
            .type(taskType.name())
            .title(taskType.getName())
            .created(OffsetDateTime.now())
            .executionType(DEFAULT_EXECUTION_TYPE)
            .taskSystem(DEFAULT_TASK_SYSTEM)
            .caseId(String.valueOf(caseId))
            .jurisdiction(JURISDICTION)
            .caseTypeId(CIC_CASE_TYPE)
            .securityClassification(getSecurityClassification(caseData))
            .permissions(getTaskPermissions(taskType))
            .priorityDate(getPriorityDate(caseData))
            .caseName(caseData.getCaseNameHmctsInternal())
            .caseCategory(getCaseCategory(caseData))
            .region(getRegion(caseData))
            .location(getLocation(caseData))
            .locationName(GLASGOW_TRIBUNALS_CENTRE)
            .majorPriority(MAJOR_PRIORITY)
            .minorPriority(MINOR_PRIORITY)
            .dueDateTime(OffsetDateTime.now().plusDays(taskType.getDueDateIntervalDays()))
            .workType(taskType.getWorkType())
            .roleCategory(taskType.getRoleCategory())
            .description(taskType.getDescription())
            .build();
    }

    private String getSecurityClassification(CaseData caseData) {
        if (caseData.getSecurityClass() == null || caseData.getSecurityClass().getLabel() == null) {
            return DEFAULT_SECURITY_CLASSIFICATION;
        }
        return caseData.getSecurityClass().getLabel();
    }

    private List<TaskPermission> getTaskPermissions(TaskType taskType) {
        return getTaskAccess(taskType).stream().map(TaskAccess::toTaskPermission).toList();
    }

    private List<TaskAccess> getTaskAccess(TaskType taskType) {
        if (LEGAL_OPERATIONS_TASKS.contains(taskType)) {
            return List.of(SENIOR_TRIBUNAL_CASEWORKER, TRIBUNAL_CASEWORKER);
        }

        if (JUDICIAL_TASKS.contains(taskType)) {
            return List.of(SENIOR_JUDGE, JUDGE);
        }

        return List.of(
            REGIONAL_CENTRE_ADMIN,
            REGIONAL_CENTRE_TEAM_LEADER,
            HEARING_CENTRE_ADMIN,
            HEARING_CENTRE_TEAM_LEADER,
            CTSC,
            CTSC_TEAM_LEADER
        );
    }

    private OffsetDateTime getPriorityDate(CaseData caseData) {
        if (caseData.getDueDate() == null) {
            return OffsetDateTime.now();
        }
        return caseData.getDueDate().atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    private String getCaseCategory(CaseData caseData) {
        if (caseData.getCaseManagementCategory() == null
            || caseData.getCaseManagementCategory().getValueLabel() == null) {
            return CRIMINAL_INJURIES_COMPENSATION;
        }
        return caseData.getCaseManagementCategory().getValueLabel();
    }

    private String getRegion(CaseData caseData) {
        if (caseData.getCaseManagementLocation() == null || caseData.getCaseManagementLocation().getRegion() == null) {
            return DEFAULT_REGION;
        }
        return caseData.getCaseManagementLocation().getRegion();
    }

    private String getLocation(CaseData caseData) {
        if (caseData.getCaseManagementLocation() == null
            || caseData.getCaseManagementLocation().getBaseLocation() == null) {
            return DEFAULT_LOCATION;
        }
        return caseData.getCaseManagementLocation().getBaseLocation();
    }
}
