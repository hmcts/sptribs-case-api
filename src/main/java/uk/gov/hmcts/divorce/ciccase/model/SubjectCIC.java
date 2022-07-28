package uk.gov.hmcts.divorce.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum SubjectCIC implements HasLabel {
    @JsonProperty("SubjectCIC")
    SUBJECT_CIC("Subject");

//    @JsonProperty("Legibility")
//    LEGIBILITY("Legibility");

    private final String label;

    public boolean isSubject_CIC() {
        return SUBJECT_CIC.name().equalsIgnoreCase(this.name());
    }
}
