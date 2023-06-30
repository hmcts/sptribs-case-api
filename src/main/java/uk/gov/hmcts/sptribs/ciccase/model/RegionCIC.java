package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum RegionCIC implements HasLabel {

    @JsonProperty("Scotland")
    SCOTLAND("Scotland", "Scotland"),
    @JsonProperty("London")
    LONDON("London", "London"),
    @JsonProperty("Midlands")
    MIDLANDS("Midlands", "Midlands"),
    @JsonProperty("North East")
    NORTH_EAST("North East", "North East"),
    @JsonProperty("North West")
    NORTH_WEST("North West", "North West"),
    @JsonProperty("Wales & South West")
    WALES_AND_SOUTH_WEST("Wales & South West", "Wales & South West");

    private String type;
    private final String label;
}
