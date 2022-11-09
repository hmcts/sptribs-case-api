package uk.gov.hmcts.sptribs.recordlisting;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.sptribs.recordlisting.model.HearingVenue;
import uk.gov.hmcts.sptribs.recordlisting.model.Region;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.recordlisting.RecordListingConstants.IS_HEARING_LOCATION;
import static uk.gov.hmcts.sptribs.recordlisting.RecordListingConstants.REGION_ID;
import static uk.gov.hmcts.sptribs.recordlisting.RecordListingConstants.REGION_ID_1;

@FeignClient(name = "location-client", url = "${location.api.baseUrl}")
public interface LocationClient {

    @GetMapping(value = "/refdata/location/court-venues",
               headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    ResponseEntity<HearingVenue[]> getHearingVenues(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestHeader(AUTHORIZATION) final String authorisation,
        @RequestParam(REGION_ID) final String regionId,
        @RequestParam(IS_HEARING_LOCATION) final String isHearingLocation
    );

    @GetMapping(value = "/refdata/location/regions",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    ResponseEntity<Region[]> getRegions(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestParam(REGION_ID_1) final String regionId
    );
}
