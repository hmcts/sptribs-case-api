package uk.gov.hmcts.sptribs.hearingvenue;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.hmcts.sptribs.hearingvenue.model.HearingVenueResponse;
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
    HearingVenueResponse getHearingVenues(
        @RequestParam(SERVICE_AUTHORIZATION) final String serviceAuthorization,
        @RequestParam(AUTHORIZATION) final String authorization,
        @RequestParam(REGION_ID) final String regionId,
        @RequestParam(IS_CASE_MANAGEMENT_LOCATION) final Optional<String> isCaseManagementLocation,
        @RequestParam(IS_HEARING_LOCATION) final Optional<String> isHearingLocation,
        @RequestParam(LOCATION_TYPE) final Optional<String> locationType,
        @RequestParam(IS_TEMPORARY_LOCATION) final Optional<String> isTemporaryLocation
    );

    @GetMapping(value = "/refdata/location/regions",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    RegionResponse getRegions(
        @RequestParam(SERVICE_AUTHORIZATION) final String serviceAuthorization,
        @RequestParam(AUTHORIZATION) final String authorization,
        @RequestParam(REGION_ID_1) final String regionId
    );
}
