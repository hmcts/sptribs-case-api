package uk.gov.hmcts.sptribs.recordlisting;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.recordlisting.model.HearingVenue;
import uk.gov.hmcts.sptribs.recordlisting.model.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.COURT_TYPE_ID;
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
        final HearingVenue[] hearingVenues = getCourtVenues(regionId);
        HearingVenue[] filteredHearingVenues = Arrays.stream(hearingVenues)
                    .filter(v -> COURT_TYPE_ID.equals(v.getCourtTypeId())).toArray(HearingVenue[]::new);
        return populateVenueDynamicList(filteredHearingVenues);
    }

    public DynamicList getAllRegions() {
        final Region[] regions = getRegions();
        return populateRegionDynamicList(regions);
    }

    private Region[] getRegions() {

        try {
            List<Region> list = locationClient.getRegions(
                authTokenGenerator.generate(),
                httpServletRequest.getHeader(AUTHORIZATION),
                REGION_ALL);
            if (CollectionUtils.isEmpty(list)) {
                return new Region[0];
            }
            return list.toArray(new Region[0]);
        } catch (FeignException exception) {
            log.error("Unable to get Region data from reference data with exception {}",
                exception.getMessage());
        }

        return new Region[0];
    }

    public String getRegionId(String selectedRegion) {
        String[] values = selectedRegion != null
            ? Arrays.stream(selectedRegion.split(HYPHEN)).map(String::trim).toArray(String[]::new)
            : null;
        return values != null && values.length > 0 ? values[0] : null;
    }

    private HearingVenue[] getCourtVenues(String regionId) {

        try {
            List<HearingVenue> list = locationClient.getHearingVenues(
                authTokenGenerator.generate(),
                httpServletRequest.getHeader(AUTHORIZATION),
                regionId,
                "Y");
            if (CollectionUtils.isEmpty(list)) {
                return new HearingVenue[0];
            }
            return list.toArray(new HearingVenue[0]);
        } catch (FeignException exception) {
            log.error("Unable to get Hearing venue data from reference data with exception {}",
                exception.getMessage());
        }

        return new HearingVenue[0];
    }

    private DynamicList populateRegionDynamicList(Region... regions) {
        List<String> regionList = Objects.nonNull(regions)
            ? Arrays.stream(regions).map(v -> v.getRegionId() + HYPHEN + v.getDescription()).toList()
            : new ArrayList<>();

        List<DynamicListElement> regionDynamicList = regionList
            .stream()
            .sorted()
            .map(region -> DynamicListElement.builder().label(region).code(UUID.randomUUID()).build())
            .toList();

        return DynamicList
            .builder()
            .listItems(regionDynamicList)
            .build();
    }

    private DynamicList populateVenueDynamicList(HearingVenue... hearingVenues) {
        List<String> venueList = Objects.nonNull(hearingVenues)
            ? Arrays.stream(hearingVenues).map(v -> v.getCourtName() + HYPHEN + v.getCourtAddress()).toList()
            : new ArrayList<>();

        List<DynamicListElement> hearingVenueList = venueList
            .stream()
            .sorted()
            .map(venue -> DynamicListElement.builder().label(venue).code(UUID.randomUUID()).build())
            .toList();

        return DynamicList
            .builder()
            .listItems(hearingVenueList)
            .build();
    }

}
