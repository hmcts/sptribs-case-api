package uk.gov.hmcts.sptribs.services.model.wa;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProcessCategoryIdentifier {
    PROCESSING("Processing"),
    HEARING("Hearing"),
    DECISION("Decision"),
    AMENDMENT("Amendment"),
    APPLICATION("Application"),
    ISSUE_CASE("IssueCase"),
    HEARING_COMPLETION("HearingCompletion"),
    HEARING_BUNDLE("HearingBundle");

    @JsonValue
    private final String value;

    @JsonCreator
    public static ProcessCategoryIdentifier fromId(String id) {
        for (ProcessCategoryIdentifier identifier : ProcessCategoryIdentifier.values()) {
            if (identifier.value.equalsIgnoreCase(id)) {
                return identifier;
            }
        }
        throw new IllegalArgumentException("Unknown ProcessCategoryIdentifier: " + id);
    }
}
