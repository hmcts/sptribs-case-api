package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@RequiredArgsConstructor
@Getter
public enum SecurityClass implements HasLabel {

    @JsonProperty("PUBLIC")
    PUBLIC("PUBLIC", "PUBLIC"),

    @JsonProperty("PRIVATE")
    PRIVATE("PRIVATE", "PRIVATE"),

    @JsonProperty("Restricted")
    RESTRICTED("RESTRICTED", "RESTRICTED");

    private final String reason;
    private final String label;

    public String getReason() {
        return this.reason;
    }

    public String getLabel() {
        return this.label;
    }
}
