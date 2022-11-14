package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum VenueNotListed implements HasLabel {

    @JsonProperty("VenueNotListed")
    VENUE_NOT_LISTED("Venue not listed");

    private final String label;
}
