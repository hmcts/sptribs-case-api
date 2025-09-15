/**
 * @param eventId    e.g., "ApplicationReceived"
 * @param taskId     e.g., "TriageTask"
 * @param actionType e.g., CREATES, COMPLETES, or CANCELS
 */
public record LifecycleAction(String eventId, String taskId, LifecycleActionType actionType) {
}