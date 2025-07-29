package uk.gov.hmcts.sptribs.services.model.wa;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.sptribs.services.wa.TaskManagementClient;

import java.util.List;

public final class Search {
    private Search() {}

    public interface SearchParameter<T> {
        Search.SearchParameterKey getKey();
        Search.SearchOperator getOperator();
        T getValues();
    }

    @AllArgsConstructor @Getter @ToString
    public static class SearchParameterList implements Search.SearchParameter<List<String>> {
        private final Search.SearchParameterKey key;
        private final Search.SearchOperator operator;
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
        private final Search.SortField sortBy;
        private final Search.SortOrder sortOrder;
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
