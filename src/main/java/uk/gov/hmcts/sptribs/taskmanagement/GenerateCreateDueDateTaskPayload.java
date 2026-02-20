package uk.gov.hmcts.sptribs.taskmanagement;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.taskmanagement.model.TaskPayload;
import uk.gov.hmcts.ccd.sdk.taskmanagement.model.TaskPermission;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static uk.gov.hmcts.sptribs.taskmanagement.GenerateProcessRule27DecisionTaskPayload.CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.taskmanagement.GenerateProcessRule27DecisionTaskPayload.GLASGOW_TRIBUNALS_CENTRE;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskAccess.CTSC;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskAccess.CTSC_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskAccess.HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskAccess.HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskAccess.REGIONAL_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskAccess.REGIONAL_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.DEFAULT_EXECUTION_TYPE;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.DEFAULT_SECURITY_CLASSIFICATION;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskConstants.DEFAULT_TASK_SYSTEM;

@Component
public class GenerateCreateDueDateTaskPayload implements TaskPayloadGenerator {

    public static final String CREATE_DUE_DATE = "createDueDate";
    public static final String CREATE_DUE_DATE_DESCRIPTION =
        "[Case: Amend due date](/cases/case-details/${[CASE_REFERENCE]}/trigger/caseworker-amend-due-date)";
    private static final String CRIMINAL_INJURIES_COMPENSATION = "Criminal Injuries Compensation";
    private static final String JURISDICTION = "ST_CIC";
    private static final String DEFAULT_REGION = "1";
    private static final String DEFAULT_LOCATION = "336559";
    private static final int DUE_DATE_WORKING_DAYS = 2;
    private static final int MAJOR_PRIORITY = 5000;
    private static final int MINOR_PRIORITY = 500;

    @Override
    public TaskPayload getTaskPayload(CaseData caseData, long caseId) {
        List<TaskPermission> permissions = Stream.of(
            REGIONAL_CENTRE_ADMIN,
            REGIONAL_CENTRE_TEAM_LEADER,
            HEARING_CENTRE_ADMIN,
            HEARING_CENTRE_TEAM_LEADER,
            CTSC,
            CTSC_TEAM_LEADER
        ).map(TaskAccess::toTaskPermission).toList();

        final String caseCategory = caseData.getCaseManagementCategory() != null
            && caseData.getCaseManagementCategory().getValueLabel() != null
            ? caseData.getCaseManagementCategory().getValueLabel()
            : CRIMINAL_INJURIES_COMPENSATION;

        final String region = caseData.getCaseManagementLocation() != null
            && caseData.getCaseManagementLocation().getRegion() != null
            ? caseData.getCaseManagementLocation().getRegion()
            : DEFAULT_REGION;

        final String location = caseData.getCaseManagementLocation() != null
            && caseData.getCaseManagementLocation().getBaseLocation() != null
            ? caseData.getCaseManagementLocation().getBaseLocation()
            : DEFAULT_LOCATION;

        final OffsetDateTime priorityDate = caseData.getDueDate() != null
            ? caseData.getDueDate().atStartOfDay().atOffset(ZoneOffset.UTC)
            : OffsetDateTime.now();

        return TaskPayload.builder()
            .externalTaskId(UUID.randomUUID().toString())
            .name("Create due date")
            .type(CREATE_DUE_DATE)
            .title("")
            .created(OffsetDateTime.now())
            .executionType(DEFAULT_EXECUTION_TYPE)
            .taskSystem(DEFAULT_TASK_SYSTEM)
            .caseId(String.valueOf(caseId))
            .jurisdiction(JURISDICTION)
            .caseTypeId(CIC_CASE_TYPE)
            .securityClassification(getSecurityClassification(caseData))
            .permissions(permissions)
            .priorityDate(priorityDate)
            .caseName(caseData.getCaseNameHmctsInternal())
            .caseCategory(caseCategory)
            .region(region)
            .location(location)
            .locationName(GLASGOW_TRIBUNALS_CENTRE)
            .majorPriority(MAJOR_PRIORITY)
            .minorPriority(MINOR_PRIORITY)
            .dueDateTime(OffsetDateTime.now().plusDays(DUE_DATE_WORKING_DAYS))
            .workType(WorkType.ROUTINE_WORK.getLowerCaseName())
            .roleCategory(RoleCategory.ADMIN.name())
            .description(CREATE_DUE_DATE_DESCRIPTION)
            .build();
    }

    private String getSecurityClassification(CaseData caseData) {
        if (caseData.getSecurityClass() == null || caseData.getSecurityClass().getLabel() == null) {
            return DEFAULT_SECURITY_CLASSIFICATION;
        }
        return caseData.getSecurityClass().getLabel();
    }
}
