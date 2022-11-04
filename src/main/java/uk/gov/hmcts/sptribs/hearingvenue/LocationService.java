package uk.gov.hmcts.sptribs.hearingvenue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.hearingvenue.model.HearingVenue;
import uk.gov.hmcts.sptribs.hearingvenue.model.HearingVenueResponse;
import uk.gov.hmcts.sptribs.hearingvenue.model.Region;
import uk.gov.hmcts.sptribs.hearingvenue.model.RegionResponse;
import uk.gov.hmcts.sptribs.payment.model.CreditAccountPaymentResponse;

import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.hearingvenue.HearingVenueConstants.HYPHEN;

@Service
@Slf4j
public class LocationService {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private LocationClient locationClient;

    public DynamicList getHearingVenuesByRegion(String regionId) {
        final var hearingVenues = getCourtVenues(regionId);
        return populateVenueDynamicList(hearingVenues);
    }

    public DynamicList populateRegionDynamicList() {
        Region[] regions = getRegionList();

        List<String> regionList = Arrays.asList(regions).stream().map(v-> v.getRegion_id() + HYPHEN + v.getDescription()).collect(Collectors.toList());

        List<DynamicListElement> regionDynamicList = regionList
            .stream()
            .map(region -> DynamicListElement.builder().label(region).code(UUID.randomUUID()).build())
            .collect(Collectors.toList());

        return DynamicList
            .builder()
            .value(DynamicListElement.builder().label("region").code(UUID.randomUUID()).build())
            .listItems(regionDynamicList)
            .build();
    }

    private Region[] getRegionList() {
        ResponseEntity<Region[]> regionResponseEntity = locationClient.getRegions(
            authTokenGenerator.generate(),
            httpServletRequest.getHeader(AUTHORIZATION),
            "ALL"
        );

        Region[] regions = Optional.ofNullable(regionResponseEntity)
            .map(response ->
                Optional.ofNullable(response.getBody())
                    .orElseGet(() -> null)
            )
            .orElseGet(() -> null);

        return regions;
    }

    private HearingVenue[] getCourtVenues(String region) {
        ResponseEntity<HearingVenue[]> hearingVenueResponseEntity =  locationClient.getHearingVenues(
            authTokenGenerator.generate(),
            httpServletRequest.getHeader(AUTHORIZATION),
            "1",
            "Y",
            "Y",
            "Court",
            "N"
        );

        HearingVenue[] hearingVenuws = Optional.ofNullable(hearingVenueResponseEntity)
            .map(response ->
                Optional.ofNullable(response.getBody())
                    .orElseGet(() -> null)
            )
            .orElseGet(() -> null);

        return hearingVenuws;
    }

    private DynamicList populateVenueDynamicList(HearingVenue[] hearingVenues) {

        List<String> venueList = Arrays.asList(hearingVenues).stream().map(v -> v.getVenue_name()).collect(Collectors.toList());

        List<DynamicListElement> hearingVenueList = venueList
            .stream()
            .map(venue -> DynamicListElement.builder().label(venue).code(UUID.randomUUID()).build())
            .collect(Collectors.toList());

        return DynamicList
            .builder()
            .value(DynamicListElement.builder().label("venue").code(UUID.randomUUID()).build())
            .listItems(hearingVenueList)
            .build();
    }



}
