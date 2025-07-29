package uk.gov.hmcts.sptribs.services.model.wa;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;

import java.util.List;
import java.util.Map;

public final class WaRequest {
    private WaRequest() {}

    @Builder
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class SearchTaskRequest {
        private final Search.RequestContext requestContext;
        private final List<Search.SearchParameter<?>> searchParameters;
        private final List<Search.SortingParameter> sortingParameters;
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


