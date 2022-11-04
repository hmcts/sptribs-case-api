package uk.gov.hmcts.sptribs.hearingvenue;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.hmcts.sptribs.hearingvenue.model.HearingVenue;
import uk.gov.hmcts.sptribs.hearingvenue.model.HearingVenueResponse;
import uk.gov.hmcts.sptribs.hearingvenue.model.Region;
import uk.gov.hmcts.sptribs.hearingvenue.model.RegionResponse;

import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.hearingvenue.HearingVenueConstants.IS_CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.sptribs.hearingvenue.HearingVenueConstants.IS_HEARING_LOCATION;
import static uk.gov.hmcts.sptribs.hearingvenue.HearingVenueConstants.IS_TEMPORARY_LOCATION;
import static uk.gov.hmcts.sptribs.hearingvenue.HearingVenueConstants.LOCATION_TYPE;
import static uk.gov.hmcts.sptribs.hearingvenue.HearingVenueConstants.REGION_ID;
import static uk.gov.hmcts.sptribs.hearingvenue.HearingVenueConstants.REGION_ID_1;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "location-client", url = "${location.api.baseUrl}")
public interface LocationClient {

    @GetMapping(value = "/refdata/location/court-venues",
               headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    ResponseEntity<HearingVenue[]> getHearingVenues(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestHeader(AUTHORIZATION) final String authorisation,
        @RequestParam(REGION_ID) final String regionId,
        @RequestParam(IS_CASE_MANAGEMENT_LOCATION) final String isCaseManagementLocation,
        @RequestParam(IS_HEARING_LOCATION) final String isHearingLocation,
        @RequestParam(LOCATION_TYPE) final String locationType,
        @RequestParam(IS_TEMPORARY_LOCATION) final String isTemporaryLocation
    );

    @GetMapping(value = "/refdata/location/regions",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    ResponseEntity<Region[]> getRegions(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestParam(REGION_ID_1) final String regionId
    );
}
