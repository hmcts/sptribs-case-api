package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HearingOutcome {

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
