package uk.gov.hmcts.sptribs.recordlisting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.recordlisting.model.HearingVenue;
import uk.gov.hmcts.sptribs.recordlisting.model.Region;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @InjectMocks
    private LocationService locationService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private ResponseEntity<Region[]> regionResponseEntity;

    @Mock
    private ResponseEntity<HearingVenue[]> hearingVenueResponseEntity;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private LocationClient locationClient;

    @Test
    void shouldPopulateRegionDynamicList() {
        //When
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(locationClient.getRegions(TEST_SERVICE_AUTH_TOKEN, TEST_AUTHORIZATION_TOKEN,"ALL"))
            .thenReturn(regionResponseEntity);

        var regionResponse = Region
            .builder()
            .regionId("1")
            .description("region")
            .build();
        when(regionResponseEntity.getBody()).thenReturn(new Region[]{regionResponse});

        DynamicList regionList = locationService.getAllRegions();

        //Then
        assertThat(regionList).isNotNull();
        assertThat(regionList.getListItems()).extracting("label").containsExactlyInAnyOrder("1-region");

    }

    @Test
    void shouldReturnEmptyDynamicListWhenExceptionFromRegionRefdataCall() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(locationClient.getRegions(TEST_SERVICE_AUTH_TOKEN, TEST_AUTHORIZATION_TOKEN,"ALL"))
            .thenReturn(null);

        DynamicList regionList = locationService.getAllRegions();

        assertThat(regionList.getListItems()).hasSize(0);
    }

    @Test
    void shouldPopulateHearingVenueDynamicList() {
        //When
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(locationClient.getHearingVenues(TEST_SERVICE_AUTH_TOKEN, TEST_AUTHORIZATION_TOKEN,"1", "Y", "Y", "Court", "N"))
            .thenReturn(hearingVenueResponseEntity);

        var hearingVenueResponse = HearingVenue
            .builder()
            .regionId("1")
            .venueName("venue")
            .build();
        when(hearingVenueResponseEntity.getBody()).thenReturn(new HearingVenue[]{hearingVenueResponse});

        DynamicList hearingVenueList = locationService.getHearingVenuesByRegion("1");

        //Then
        assertThat(hearingVenueList).isNotNull();
        assertThat(hearingVenueList.getListItems()).extracting("label").containsExactlyInAnyOrder("venue");

    }

    @Test
    void shouldReturnEmptyDynamicListWhenExceptionFromHearingVenueRefdataCall() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(locationClient.getHearingVenues(TEST_SERVICE_AUTH_TOKEN, TEST_AUTHORIZATION_TOKEN,"1", "Y", "Y", "Court", "N"))
            .thenReturn(null);

        DynamicList regionList = locationService.getHearingVenuesByRegion("1");

        assertThat(regionList.getListItems()).hasSize(0);
    }
}
