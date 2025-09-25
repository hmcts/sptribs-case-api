package model;

import java.util.List;
import java.util.Map;

// This record holds the entire aggregated model of your process.
public record ProcessModel(
        Map<String, FullTaskDefinition> tasks,
        List<String> states,
        List<String> events
) {
}