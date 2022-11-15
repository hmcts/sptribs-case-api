package uk.gov.hmcts.sptribs.recordlisting.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HearingVenue {
    @JsonProperty("court_venue_id")
    private String courtVenueId;

    @JsonProperty("court_name")
    private String courtName;

    @JsonProperty("region_id")
    private String regionId;

    @JsonProperty("region")
    private String region;

    @JsonProperty("venue_name")
    private String venueName;

    @JsonProperty("court_address")
    private String courtAddress;

}
