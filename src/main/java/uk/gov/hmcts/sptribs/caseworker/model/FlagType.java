package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@RequiredArgsConstructor
@Getter
public enum FlagType implements HasLabel {

    @JsonProperty("Other")
    OTHER("Other", "Other", "1");

    private final String reason;
    private final String label;
    private final String flagCode;

    public String getReason() {
        return this.reason;
    }

    public String getLabel() {
        return this.label;
    }

    public String getFlagCode() {
        return this.flagCode;
    }
}
