package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum PanelMembersRole implements HasLabel {

    @JsonProperty("fullMember")
    FULL_MEMBER("Full member"),

    @JsonProperty("observer")
    OBSERVER("Observer"),

    @JsonProperty("appraiser")
    APPRAISER("Appraiser");

    private final String label;
}
