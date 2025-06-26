package uk.gov.hmcts.sptribs.services.wa;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.sptribs.services.wa.config.WaFeignClientSnakeCaseConfiguration;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Feign client for the Work Allocation Task Management API.
 * Includes all required DTOs as nested static classes for convenience.
 */
@FeignClient(
    name = "wa-task-management-api",
    url = "${wa-task-management-api.url}",
    configuration = WaFeignClientSnakeCaseConfiguration.class
)
public interface TaskManagementClient {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String AUTHORIZATION = "Authorization";

    @PostMapping(path = "/task", consumes = APPLICATION_JSON_VALUE)
    Response.GetTasksResponse<Domain.Task> searchTask(
        @RequestHeader(AUTHORIZATION) String authToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @RequestParam(name = "first_result", required = false, defaultValue = "0") int firstResult,
        @RequestParam(name = "max_results", required = false, defaultValue = "50") int maxResults,
        @RequestBody Request.SearchTaskRequest searchTaskRequest
    );

    @GetMapping(path = "/task/{task-id}")
    Response.GetTaskResponse<Domain.Task> getTask(
        @RequestHeader(AUTHORIZATION) String authToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("task-id") String taskId
    );

    @PostMapping(path = "/task/{task-id}/claim")
    ResponseEntity<Void> claimTask(
        @RequestHeader(AUTHORIZATION) String authToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("task-id") String taskId
    );

    @PostMapping(path = "/task/{task-id}/unclaim")
    ResponseEntity<Void> unclaimTask(
        @RequestHeader(AUTHORIZATION) String authToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("task-id") String taskId
    );

    @PostMapping(path = "/task/{task-id}/assign")
    ResponseEntity<Void> assignTask(
        @RequestHeader(AUTHORIZATION) String authToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("task-id") String taskId,
        @RequestBody Request.AssignTaskRequest assignTaskRequest
    );

    @PostMapping(path = "/task/{task-id}/complete")
    ResponseEntity<Void> completeTask(
        @RequestHeader(AUTHORIZATION) String authToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("task-id") String taskId,
        @RequestBody(required = false) Request.CompleteTaskRequest completeTaskRequest
    );

    @PostMapping(path = "/task/{task-id}/cancel")
    ResponseEntity<Void> cancelTask(
        @RequestHeader(AUTHORIZATION) String authToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("task-id") String taskId
    );

    @PostMapping(path = "/task/{task-id}/initiation", consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<Domain.Task> initiateTask(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("task-id") String taskId,
        @RequestBody Request.InitiateTaskRequestMap initiateTaskRequest
    );

    @DeleteMapping(path = "/task/{task-id}")
    ResponseEntity<Void> terminateTask(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("task-id") String taskId,
        @RequestBody Request.TerminateTaskRequest terminateTaskRequest
    );

//    @PostMapping(path = "/task/operation", consumes = APPLICATION_JSON_VALUE)
//    ResponseEntity<Void> performOperation(
//        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
//        @RequestBody Request.TaskOperationRequest taskOperationRequest
//    );

    // --- DTO Classes ---

    final class Request {
        private Request() {}

        @Builder @Getter @EqualsAndHashCode @ToString
        public static class SearchTaskRequest {
            private final Domain.Search.RequestContext requestContext;
            private final List<Domain.Search.SearchParameter<?>> searchParameters;
            private final List<Domain.Search.SortingParameter> sortingParameters;
        }

        @Value
        public static class AssignTaskRequest {
            String userId;
        }

        @Value
        public static class CompleteTaskRequest {
            Options.CompletionOptions completionOptions;
        }

        @Value
        public static class TerminateTaskRequest {
            Options.TerminateInfo terminateInfo;
        }

        @Value
        public static class InitiateTaskRequestMap {
            Map<String, Object> taskAttributes;
            Enums.InitiateTaskOperation operation;

            @JsonCreator
            public InitiateTaskRequestMap(Enums.InitiateTaskOperation operation, Map<String, Object> taskAttributes) {
                this.operation = operation;
                this.taskAttributes = taskAttributes;
            }
        }

        @Value
        public static class SearchEventAndCase {
            String caseId;
            String eventId;
            String caseJurisdiction;
            String caseType;
        }

        public final class Options {
            private Options() {}
            @Value public static class CompletionOptions { boolean assignAndComplete; }
            @Value public static class TerminateInfo { String terminateReason; }
        }

        public final class Enums {
            private Enums() {}
            public enum InitiateTaskOperation { INITIATION }
        }

    }

    final class Response {
        private Response() {}

        @Getter @AllArgsConstructor @EqualsAndHashCode @ToString
        public static class GetTaskResponse<T extends Domain.Task> {
            private final T task;
        }

        @Getter @AllArgsConstructor @EqualsAndHashCode @ToString
        public static class GetTasksResponse<T extends Domain.Task> {
            private final List<T> tasks;
            private final long totalRecords;
        }

        @Getter @AllArgsConstructor @EqualsAndHashCode @ToString
        public static class GetTasksCompletableResponse<T extends Domain.Task> {
            private final boolean taskRequiredForEvent;
            private final List<T> tasks;
        }
    }

    final class Domain {
        private Domain() {}

        @Getter @Builder @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Task {
            private String id;
            private String name;
            private String type;
            private String taskState;
            private String taskSystem;
            private String securityClassification;
            private String taskTitle;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ")
            private ZonedDateTime createdDate;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ")
            private ZonedDateTime dueDate;
            private String assignee;
            private boolean autoAssigned;
            private String executionType;
            private String jurisdiction;
            private String region;
            private String location;
            private String locationName;
            private String caseTypeId;
            private String caseId;
            private String caseCategory;
            private String caseName;
            private Boolean warnings;
            private WarningValues warningList;
            private String caseManagementCategory;
            private String workTypeId;
            private TaskPermissions permissions;
            private String roleCategory;
            private String description;
            private Map<String, String> additionalProperties;
            private String nextHearingId;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ")
            private ZonedDateTime nextHearingDate;
            private Integer minorPriority;
            private Integer majorPriority;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ")
            private ZonedDateTime priorityDate;
        }

        @Getter @AllArgsConstructor
        public static class TaskPermissions {
            private Set<PermissionTypes> values;
        }

        public enum PermissionTypes {
            READ("Read"), REFER("Refer"), OWN("Own"), MANAGE("Manage"), EXECUTE("Execute"),
            CANCEL("Cancel"), COMPLETE("Complete"), COMPLETE_OWN("CompleteOwn"),
            CANCEL_OWN("CancelOwn"), CLAIM("Claim"), UNCLAIM("Unclaim"), ASSIGN("Assign"),
            UNASSIGN("Unassign"), UNCLAIM_ASSIGN("UnclaimAssign"),
            UNASSIGN_CLAIM("UnassignClaim"), UNASSIGN_ASSIGN("UnassignAssign");

            @JsonValue private final String value;
            PermissionTypes(String value) { this.value = value; }
        }

        @Getter
        public static class WarningValues {
            private final List<Warning> values;
            @JsonCreator public WarningValues(List<Warning> values) { this.values = values; }
        }

        @Getter
        public static class Warning {
            private final String warningCode;
            private final String warningText;
            @JsonCreator public Warning(String warningCode, String warningText) {
                this.warningCode = warningCode; this.warningText = warningText;
            }
        }

        public final class Search {
            private Search() {}

            public interface SearchParameter<T> {
                SearchParameterKey getKey();
                SearchOperator getOperator();
                T getValues();
            }

            @AllArgsConstructor @Getter @ToString
            public static class SearchParameterList implements SearchParameter<List<String>> {
                private final SearchParameterKey key;
                private final SearchOperator operator;
                @JsonProperty("values")
                private final List<String> values;
            }

            public enum SearchParameterKey {
                LOCATION("location"), USER("user"), JURISDICTION("jurisdiction"), STATE("state"),
                TASK_TYPE("task_type"), CASE_ID("case_id"), WORK_TYPE("work_type"), ROLE_CATEGORY("role_category");
                @JsonValue private final String id;
                SearchParameterKey(String id) { this.id = id; }
            }

            public enum SearchOperator {
                IN, BOOLEAN, BETWEEN, BEFORE, AFTER;
            }

            @AllArgsConstructor @Getter @ToString
            public static class SortingParameter {
                private final SortField sortBy;
                private final SortOrder sortOrder;
            }

            public enum SortField {
                DUE_DATE("due_date"), TASK_TITLE("task_title"), LOCATION_NAME("location_name"),
                CASE_CATEGORY("case_category"), CASE_ID("case_id"), CASE_NAME("case_name"),
                NEXT_HEARING_DATE("next_hearing_date"), MAJOR_PRIORITY("major_priority"),
                PRIORITY_DATE("priority_date"), MINOR_PRIORITY("minor_priority");
                @JsonValue private final String id;
                SortField(String id) { this.id = id; }
            }

            public enum SortOrder {
                ASCENDANT("asc"), DESCENDANT("desc");
                @JsonValue private final String id;
                SortOrder(String id) { this.id = id; }
            }

            public enum RequestContext {
                ALL_WORK, AVAILABLE_TASKS;
            }
        }
    }
}
