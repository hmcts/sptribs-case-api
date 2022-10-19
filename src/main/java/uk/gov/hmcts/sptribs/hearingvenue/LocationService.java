package uk.gov.hmcts.sptribs.hearingvenue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.hearingvenue.model.HearingVenue;
import uk.gov.hmcts.sptribs.hearingvenue.model.HearingVenueResponse;
import uk.gov.hmcts.sptribs.hearingvenue.model.Region;
import uk.gov.hmcts.sptribs.hearingvenue.model.RegionResponse;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

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
        final var hearingVenueResponse = getCourtVenues(regionId);
        DynamicList hearingVenues = populateVenueDynamicList(hearingVenueResponse.getHearingVenues());

        return hearingVenues;
    }

    public DynamicList getRegionList() {
        final var regionResponse = getRegions();
        DynamicList regionList = populateRegionDynamicList(regionResponse.getRegions());

        return regionList;
    }

    private HearingVenueResponse getCourtVenues(String region) {
        final var hearingVenueResponse = locationClient.getHearingVenues(
            authTokenGenerator.generate(),
            httpServletRequest.getHeader(AUTHORIZATION),
            region,
            Optional.of("Y"),
            Optional.of("Y"),
            Optional.of("Court"),
            Optional.of("N")
        );

        return hearingVenueResponse;
    }

    private RegionResponse getRegions() {
        final var regionResponse = locationClient.getRegions(
            authTokenGenerator.generate(),
            httpServletRequest.getHeader(AUTHORIZATION),
            "ALL"
        );

        return regionResponse;
    }

    private DynamicList populateVenueDynamicList(List<HearingVenue> hearingVenues) {

        List<String> venueList = hearingVenues.stream().map(v -> v.getVenue_name()).collect(Collectors.toList());

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

    private DynamicList populateRegionDynamicList(List<Region> regions) {

        List<String> regionList = regions.stream().map(v -> v.getRegion()).collect(Collectors.toList());

        List<DynamicListElement> hearingVenueList = regionList
            .stream()
            .map(region -> DynamicListElement.builder().label(region).code(UUID.randomUUID()).build())
            .collect(Collectors.toList());

        return DynamicList
            .builder()
            .value(DynamicListElement.builder().label("region").code(UUID.randomUUID()).build())
            .listItems(hearingVenueList)
            .build();
    }

}
