package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ConfidentialAddress implements HasLabel {

    @JsonProperty("share")
    SHARE("No"),

    @JsonProperty("keep")
    KEEP("Yes");

    private final String label;
}
