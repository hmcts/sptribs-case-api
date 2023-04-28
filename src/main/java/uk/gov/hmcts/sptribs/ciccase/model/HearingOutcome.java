package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum HearingOutcome implements HasLabel {

    @JsonProperty("Adjourned")
    ADJOURNED("Adjourned"),

    @JsonProperty("Allowed")
    ALLOWED("Allowed"),

    @JsonProperty("Refused")
    REFUSED("Refused"),

    @JsonProperty("Withdrawn at Hearing")
    WITHDRAWN_AT_HEARING("Withdrawn at Hearing");

    private final String label;
}
