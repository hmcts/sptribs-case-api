package uk.gov.hmcts.sptribs.hearingvenue.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HearingVenue {
    @JsonProperty("court_venue_id")
    private String court_venue_id;

    @JsonProperty("court_name")
    private String court_name;

    @JsonProperty("site_name")
    private String site_name;

    @JsonProperty("epims_id")
    private Double epims_id;

    @JsonProperty("open_for_public")
    private String open_for_public;

    @JsonProperty("court_type_id")
    private Double court_type_id;

    @JsonProperty("court_type")
    private String court_type;

    @JsonProperty("region_id")
    private Double region_id;

    @JsonProperty("region")
    private String region;

    @JsonProperty("venue_name")
    private String venue_name;

    @JsonProperty("cluster_id")
    private Double cluster_id;

    @JsonProperty("cluster_name")
    private String cluster_name;

    @JsonProperty("court_status")
    private String court_status;

    @JsonProperty("court_open_date")
    private LocalDate court_open_date;

    @JsonProperty("closed_date")
    private LocalDate closed_date;

    @JsonProperty("postcode")
    private String postcode;

    @JsonProperty("court_address")
    private String court_address;

    @JsonProperty("phone_number")
    private String phone_number;

    @JsonProperty("court_location_code")
    private String court_location_code;

    @JsonProperty("dx_address")
    private String dx_address;

    @JsonProperty("welsh_site_name")
    private String welsh_site_name;

    @JsonProperty("welsh_court_address")
    private String welsh_court_address;

    @JsonProperty("is_case_management_location")
    private String is_case_management_location;

    @JsonProperty("is_hearing_location")
    private String is_hearing_location;

    @JsonProperty("welsh_venue_name")
    private String welsh_venue_name;

    @JsonProperty("is_temporary_location")
    private String is_temporary_location;

    @JsonProperty("is_nightingale_court")
    private String is_nightingale_court;

    @JsonProperty("location_type")
    private String location_type;

    @JsonProperty("parent_location")
    private String parent_location;

    @JsonProperty("welsh_court_name")
    private String welsh_court_name;

    @JsonProperty("venue_ou_code")
    private String venue_ou_code;

    @JsonProperty("mrd_building_location_id")
    private String mrd_building_location_id;

    @JsonProperty("mrd_venue_id")
    private String mrd_venue_id;

    @JsonProperty("service_url")
    private String service_url;

    @JsonProperty("fact_url")
    private String fact_url;

}
