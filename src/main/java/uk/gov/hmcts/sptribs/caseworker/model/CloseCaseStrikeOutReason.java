package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum CloseCaseStrikeOutReason implements HasLabel {

    @JsonProperty("noncomplianceWithDirections")
    NO_JURISDICTION("Non-compliance with Directions"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
