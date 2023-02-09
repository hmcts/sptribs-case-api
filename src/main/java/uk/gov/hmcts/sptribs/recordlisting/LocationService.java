package uk.gov.hmcts.sptribs.recordlisting;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.recordlisting.model.HearingVenue;
import uk.gov.hmcts.sptribs.recordlisting.model.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;
import static uk.gov.hmcts.sptribs.recordlisting.RecordListingConstants.REGION_ALL;

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

    public DynamicList getAllRegions() {
        final var regions = getRegions();
        return populateRegionDynamicList(regions);
    }

    private Region[] getRegions() {
        ResponseEntity<Region[]> regionResponseEntity = null;

        try {
            regionResponseEntity = locationClient.getRegions(
                authTokenGenerator.generate(),
                httpServletRequest.getHeader(AUTHORIZATION),
                REGION_ALL);
        } catch (FeignException exception) {
            log.error("Unable to get Region data from reference data with exception {}",
                exception.getMessage());
        }

        return Optional.ofNullable(regionResponseEntity)
            .map(response ->
                Optional.ofNullable(response.getBody())
                    .orElseGet(() -> null)
            )
            .orElseGet(() -> null);
    }

    private HearingVenue[] getCourtVenues(String regionId) {
        ResponseEntity<HearingVenue[]> hearingVenueResponseEntity = null;

        try {
            hearingVenueResponseEntity = locationClient.getHearingVenues(
                authTokenGenerator.generate(),
                httpServletRequest.getHeader(AUTHORIZATION),
                regionId,
                "Y");
        } catch (FeignException exception) {
            log.error("Unable to get Hearing venue data from reference data with exception {}",
                exception.getMessage());
        }

        return Optional.ofNullable(hearingVenueResponseEntity)
            .map(response ->
                Optional.ofNullable(response.getBody())
                    .orElseGet(() -> null)
            )
            .orElseGet(() -> null);
    }

    private DynamicList populateRegionDynamicList(Region... regions) {
        List<String> regionList = Objects.nonNull(regions)
            ? Arrays.asList(regions).stream().map(v -> v.getRegionId() + HYPHEN + v.getDescription()).collect(Collectors.toList())
            : new ArrayList<>();

        List<DynamicListElement> regionDynamicList = regionList
            .stream()
            .sorted()
            .map(region -> DynamicListElement.builder().label(region).code(UUID.randomUUID()).build())
            .collect(Collectors.toList());

        return DynamicList
            .builder()
            .listItems(regionDynamicList)
            .build();
    }

    private DynamicList populateVenueDynamicList(HearingVenue... hearingVenues) {
        List<String> venueList = Objects.nonNull(hearingVenues)
            ? Arrays.asList(hearingVenues).stream().map(v -> v.getCourtName() + HYPHEN + v.getCourtAddress()).collect(Collectors.toList())
            : new ArrayList<>();

        List<DynamicListElement> hearingVenueList = venueList
            .stream()
            .sorted()
            .map(venue -> DynamicListElement.builder().label(venue).code(UUID.randomUUID()).build())
            .collect(Collectors.toList());

        return DynamicList
            .builder()
            .listItems(hearingVenueList)
            .build();
    }

}
