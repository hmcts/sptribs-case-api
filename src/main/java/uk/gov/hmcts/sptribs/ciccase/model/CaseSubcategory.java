package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum CaseSubcategory implements HasLabel {
    @JsonProperty("Fatal")
    FATAL("Fatal"),

    @JsonProperty("sexualAbuse")
    SEXUAL_ABUSE("Sexual Abuse"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
