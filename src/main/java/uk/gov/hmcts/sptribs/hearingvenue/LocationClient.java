package uk.gov.hmcts.sptribs.hearingvenue;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.hmcts.sptribs.hearingvenue.model.HearingVenueResponse;
import uk.gov.hmcts.sptribs.payment.model.FeeResponse;

import static uk.gov.hmcts.sptribs.hearingvenue.HearingVenueConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.hearingvenue.HearingVenueConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.hearingvenue.HearingVenueConstants.IS_CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.sptribs.hearingvenue.HearingVenueConstants.IS_HEARING_LOCATION;
import static uk.gov.hmcts.sptribs.hearingvenue.HearingVenueConstants.IS_TEMPORARY_LOCATION;
import static uk.gov.hmcts.sptribs.hearingvenue.HearingVenueConstants.LOCATION_TYPE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "location-client", url = "${location.api.baseUrl}")
public interface LocationClient {

    @GetMapping(value = "/location/court-venues",
               headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    HearingVenueResponse getHearingVenues(
        @RequestParam(SERVICE_AUTHORIZATION) final String serviceAuthorization,
        @RequestParam(AUTHORIZATION) final String authorization,
        @RequestParam(IS_CASE_MANAGEMENT_LOCATION) final String isCaseManagementLocation,
        @RequestParam(IS_HEARING_LOCATION) final String isHearingLocation,
        @RequestParam(LOCATION_TYPE) final String locationType,
        @RequestParam(IS_TEMPORARY_LOCATION) final String isTemporaryLocation
    );
}
