package model;

import model.rules.CancellationRule;
import model.rules.CompletionRule;
import model.rules.ConfigurationRule;
import model.rules.InitiationRule;
import model.rules.PermissionRule;

import java.util.ArrayList;
import java.util.List;

public record FullTaskDefinition(
        String id,                     // The unique ID, e.g., "reviewNewCaseAndProvideDirectionsLO"
        String name,
        List<InitiationRule> initiationRules,  // How is this task created?
        List<CompletionRule> completionRules,  // How is this task completed?
        List<CancellationRule> cancellationRules,  // How is this task completed?
        List<PermissionRule> permissionRules,  // Who can do this task?
        List<ConfigurationRule> configurationRules // What are its properties?
) {
    public FullTaskDefinition(String id, String name) {
        this(id, name, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }
}
