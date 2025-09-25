package model;

import java.util.List;

public record Transition(
        String eventId,
        String fromState,
        String toState,
        List<TaskDefinition> tasksCreated,
        List<TaskDefinition> tasksCompleted,
        List<TaskDefinition> tasksCancelled
) {
}
