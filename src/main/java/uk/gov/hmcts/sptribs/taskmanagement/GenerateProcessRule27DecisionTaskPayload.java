package uk.gov.hmcts.sptribs.taskmanagement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.taskmanagement.model.TaskPayload;
import uk.gov.hmcts.ccd.sdk.taskmanagement.model.TaskPermission;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

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
@RequiredArgsConstructor
public class GenerateProcessRule27DecisionTaskPayload implements TaskPayloadGenerator {

    public static final String CRIMINAL_INJURIES_COMPENSATION = "Criminal Injuries Compensation";
    public static final String GLASGOW_TRIBUNALS_CENTRE = "Glasgow Tribunals Centre";
    public static final String RULE_27_DESCRIPTION =
        "[Orders: Send order](/cases/case-details/${[CASE_REFERENCE]}/trigger/caseworker-send-order)";
    public static final String CIC_CASE_TYPE = "CriminalInjuriesCompensation";
    private static final String JURISDICTION = "ST_CIC";
    public static final String PROCESS_RULE_27_DECISION = "processRule27Decision";

    @Override
    public TaskPayload getTaskPayload(CaseData caseData, long caseId) {

        List<TaskPermission> permissions = Stream.of(REGIONAL_CENTRE_ADMIN, REGIONAL_CENTRE_TEAM_LEADER,
            HEARING_CENTRE_ADMIN, HEARING_CENTRE_TEAM_LEADER,
            CTSC, CTSC_TEAM_LEADER).map(TaskAccess::toTaskPermission).toList();

        final String caseCategory = caseData.getCaseManagementCategory().getValueLabel() != null
            ? caseData.getCaseManagementCategory().getValueLabel()
            : CRIMINAL_INJURIES_COMPENSATION;

        return TaskPayload.builder()
            .externalTaskId(UUID.randomUUID().toString())
            .name("Process Rule 27 decision")
            .type(PROCESS_RULE_27_DECISION)
            .title("")
            .created(OffsetDateTime.now())
            .executionType(DEFAULT_EXECUTION_TYPE)
            .taskSystem(DEFAULT_TASK_SYSTEM)
            .caseId(String.valueOf(caseId))
            .jurisdiction(JURISDICTION)
            .caseTypeId(CIC_CASE_TYPE)
            .securityClassification(getSecurityClassification(caseData))
            .permissions(permissions)
            .priorityDate(getPriorityDate(caseData))
            .caseName(caseData.getCaseNameHmctsInternal())
            .caseCategory(caseCategory)
            .region(caseData.getCaseManagementLocation().getRegion())
            .location(caseData.getCaseManagementLocation().getBaseLocation())
            .locationName(GLASGOW_TRIBUNALS_CENTRE)
            .majorPriority(5000)
            .minorPriority(500)
            .dueDateTime(OffsetDateTime.now().plusDays(7))
            .workType(WorkType.ROUTINE_WORK.getLowerCaseName())
            .roleCategory(RoleCategory.ADMIN.name())
            .description(RULE_27_DESCRIPTION)
            .build();
    }

    private OffsetDateTime getPriorityDate(CaseData caseData) {
        if (caseData.getDueDate() == null) {
            return OffsetDateTime.now();
        }
        return caseData.getDueDate().atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    private String getSecurityClassification(CaseData caseData) {
        if (caseData.getSecurityClass() == null || caseData.getSecurityClass().getLabel() == null) {
            return DEFAULT_SECURITY_CLASSIFICATION;
        }
        return caseData.getSecurityClass().getLabel();
    }
}
