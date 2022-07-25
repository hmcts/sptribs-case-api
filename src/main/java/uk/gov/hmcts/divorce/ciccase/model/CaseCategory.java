package uk.gov.hmcts.divorce.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum CaseCategory implements HasLabel {
    @JsonProperty("Assessment")
    ASSESSMENT("Assessment"),

    @JsonProperty("Legibility")
    LEGIBILITY("Legibility");

    private final String label;

    public boolean isAssessment() {
        return ASSESSMENT.name().equalsIgnoreCase(this.name());
    }
}
