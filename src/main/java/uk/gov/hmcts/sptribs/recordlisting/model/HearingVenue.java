package uk.gov.hmcts.sptribs.recordlisting.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @JsonProperty("site_name")
    private String siteName;

    @JsonProperty("epims_id")
    private Double epimsId;

    @JsonProperty("open_for_public")
    private String openForPublic;

    @JsonProperty("court_type_id")
    private Double courtTypeId;

    @JsonProperty("court_type")
    private String courtType;

    @JsonProperty("region_id")
    private String regionId;

    @JsonProperty("region")
    private String region;

    @JsonProperty("venue_name")
    private String venueName;

    @JsonProperty("cluster_id")
    private Double clusterId;

    @JsonProperty("cluster_name")
    private String clusterName;

    @JsonProperty("court_status")
    private String courtStatus;

    @JsonProperty("court_open_date")
    private LocalDate courtOpenDate;

    @JsonProperty("closed_date")
    private LocalDate closedDate;

    @JsonProperty("postcode")
    private String postcode;

    @JsonProperty("court_address")
    private String courtAddress;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("court_location_code")
    private String courtLocationCode;

    @JsonProperty("dx_address")
    private String dxAddress;

    @JsonProperty("welsh_site_name")
    private String welshSiteName;

    @JsonProperty("welsh_court_address")
    private String welshCourtAddress;

    @JsonProperty("is_case_management_location")
    private String isCaseManagementLocation;

    @JsonProperty("is_hearing_location")
    private String isHearingLocation;

    @JsonProperty("welsh_venue_name")
    private String welshVenueName;

    @JsonProperty("is_temporary_location")
    private String isTemporaryLocation;

    @JsonProperty("is_nightingale_court")
    private String isNightingaleCourt;

    @JsonProperty("location_type")
    private String locationType;

    @JsonProperty("parent_location")
    private String parentLocation;

    @JsonProperty("welsh_court_name")
    private String welshCourtName;

    @JsonProperty("venue_ou_code")
    private String venueOUCode;

    @JsonProperty("mrd_building_location_id")
    private String mrdBuildingLocationId;

    @JsonProperty("mrd_venue_id")
    private String mrdVenueId;

    @JsonProperty("service_url")
    private String serviceUrl;

    @JsonProperty("fact_url")
    private String factUrl;

}
