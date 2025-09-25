package model.rules;

public record CompletionRule(
        //inputs
        String eventId,

        //outputs
        String taskType,
        String completionMode
) {
}
