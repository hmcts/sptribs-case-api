package uk.gov.hmcts.divorce.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum SubjectCIC implements HasLabel {
    @JsonProperty("Subject")
    SUBJECT("Subject");

    private final String label;
    public boolean isSubject() {
        return true;
    }
}
