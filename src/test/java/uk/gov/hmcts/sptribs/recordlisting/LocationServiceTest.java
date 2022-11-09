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
        //Given
        var regionResponse = Region
            .builder()
            .regionId("1")
            .description("region")
            .build();

        //When
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(locationClient.getRegions(TEST_SERVICE_AUTH_TOKEN, TEST_AUTHORIZATION_TOKEN,"ALL"))
            .thenReturn(regionResponseEntity);
        when(regionResponseEntity.getBody()).thenReturn(new Region[]{regionResponse});
        DynamicList regionList = locationService.getAllRegions();

        //Then
        assertThat(regionList).isNotNull();
        assertThat(regionList.getListItems()).extracting("label").containsExactlyInAnyOrder("1-region");
    }

    @Test
    void shouldReturnEmptyDynamicListWhenExceptionFromRegionRefdataCall() {
        //When
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(locationClient.getRegions(TEST_SERVICE_AUTH_TOKEN, TEST_AUTHORIZATION_TOKEN,"ALL"))
            .thenReturn(null);
        DynamicList regionList = locationService.getAllRegions();

        //Then
        assertThat(regionList.getListItems()).isEmpty();
    }

    @Test
    void shouldPopulateHearingVenueDynamicList() {
        //Given
        var hearingVenueResponse = HearingVenue
            .builder()
            .regionId("1")
            .courtName("courtName")
            .courtAddress("courtAddress")
            .build();

        //When
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(locationClient.getHearingVenues(TEST_SERVICE_AUTH_TOKEN, TEST_AUTHORIZATION_TOKEN,"1", "Y"))
            .thenReturn(hearingVenueResponseEntity);
        when(hearingVenueResponseEntity.getBody()).thenReturn(new HearingVenue[]{hearingVenueResponse});
        DynamicList hearingVenueList = locationService.getHearingVenuesByRegion("1");

        //Then
        assertThat(hearingVenueList).isNotNull();
        assertThat(hearingVenueList.getListItems()).extracting("label").containsExactlyInAnyOrder("courtName-courtAddress");

    }

    @Test
    void shouldPopulateHearingVenueDynamicListWithMissingCourtAddress() {
        //Given
        var hearingVenueResponse = HearingVenue
            .builder()
            .regionId("1")
            .courtName("courtName")
            .build();

        //When
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(locationClient.getHearingVenues(TEST_SERVICE_AUTH_TOKEN, TEST_AUTHORIZATION_TOKEN,"1", "Y"))
            .thenReturn(hearingVenueResponseEntity);
        when(hearingVenueResponseEntity.getBody()).thenReturn(new HearingVenue[]{hearingVenueResponse});
        DynamicList hearingVenueList = locationService.getHearingVenuesByRegion("1");

        //Then
        assertThat(hearingVenueList).isNotNull();
        assertThat(hearingVenueList.getListItems()).extracting("label").containsExactlyInAnyOrder("courtName-null");

    }

    @Test
    void shouldReturnEmptyDynamicListWhenExceptionFromHearingVenueRefdataCall() {
        //When
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(locationClient.getHearingVenues(TEST_SERVICE_AUTH_TOKEN, TEST_AUTHORIZATION_TOKEN,"1", "Y"))
            .thenReturn(null);
        DynamicList regionList = locationService.getHearingVenuesByRegion("1");

        //Then
        assertThat(regionList.getListItems()).isEmpty();
    }
}
