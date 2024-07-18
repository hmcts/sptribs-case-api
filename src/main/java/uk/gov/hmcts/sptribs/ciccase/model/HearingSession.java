package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HearingSession {

    @JsonProperty("morning")
    MORNING("Morning"),

    @JsonProperty("afternoon")
    AFTERNOON("Afternoon"),

    @JsonProperty("allDay")
    ALL_DAY("All day");

    private final String label;
}
