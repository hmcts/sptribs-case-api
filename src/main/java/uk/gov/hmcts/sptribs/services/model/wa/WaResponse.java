package uk.gov.hmcts.sptribs.services.model.wa;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

public final class WaResponse {
    private WaResponse() {}

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class GetTaskResponse<T extends WaTask> {
        private final T task;
    }

    @Getter @AllArgsConstructor @EqualsAndHashCode @ToString
    public static class GetTasksResponse<T extends WaTask> {
        private final List<T> tasks;
        private final long totalRecords;
    }

    @Getter @AllArgsConstructor @EqualsAndHashCode @ToString
    public static class GetTasksCompletableResponse<T extends WaTask> {
        private final boolean taskRequiredForEvent;
        private final List<T> tasks;
    }
}
