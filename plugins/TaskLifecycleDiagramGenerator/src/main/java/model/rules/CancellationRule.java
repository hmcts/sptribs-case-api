package model.rules;

public record CancellationRule(
        //inputs
        String fromState,
        String eventId,
        String toState,

        //outputs
        String action,
        String warningCode,
        String warningText,
        String processCategories
) {
}
