package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum HearingSession implements HasLabel {

    @JsonProperty("morning")
    MORNING("Morning"),

    @JsonProperty("afternoon")
    AFTERNOON("Afternoon"),

    @JsonProperty("allDay")
    ALL_DAY("All day");

    private final String label;
}
