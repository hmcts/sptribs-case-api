package model.rules;

public record InitiationRule(
    //inputs
    String eventId,
    String postEventState,
    String referralType,

    //outputs
    String taskId,
    String taskName,
    String delayDuration,
    String workingDaysAllowed,
    String processCategory,
    String workType,
    String roleCategory
) {
}

