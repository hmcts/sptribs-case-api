package model.rules;

public record PermissionRule(
        //input
        String taskType,
        String caseData,

        //output
        String caseAccessCategory,
        String name,
        String value,
        String roleCategory,
        String authorisations,
        String assignmentPriority,
        String autoAssignable
) {
}
