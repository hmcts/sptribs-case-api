package model;

import model.rules.CancellationRule;
import model.rules.CompletionRule;
import model.rules.InitiationRule;

import java.util.List;

public record RichTransition(
        String eventId,
        String fromState,
        String toState,
        List<InitiationRule> initiationRules,
        List<CompletionRule> completionRules,
        List<CancellationRule> cancellationRules
) {
}
