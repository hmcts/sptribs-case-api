package uk.gov.hmcts.sptribs.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum Status implements HasLabel {

    @JsonProperty("ACTIVE")
    ACTIVE("ACTIVE","ACTIVE"),

    @JsonProperty("INACTIVE")
    INACTIVE("INACTIVE","INACTIVE");

    private String type;
    private final String label;

    public boolean isActive() {
        return ACTIVE.name().equalsIgnoreCase(this.name());
    }
}
