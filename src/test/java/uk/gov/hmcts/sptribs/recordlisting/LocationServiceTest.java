package uk.gov.hmcts.sptribs.recordlisting;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.recordlisting.model.HearingVenue;
import uk.gov.hmcts.sptribs.recordlisting.model.Region;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.COURT_TYPE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @InjectMocks
    private LocationService locationService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private LocationClient locationClient;

    @Test
    void shouldPopulateRegionDynamicList() {
        //Given
        final Region region = Region.builder().regionId("1").description("region").build();

        //When
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(locationClient.getRegions(TEST_SERVICE_AUTH_TOKEN, TEST_AUTHORIZATION_TOKEN, "ALL"))
            .thenReturn(List.of(region));
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
        when(locationClient.getRegions(TEST_SERVICE_AUTH_TOKEN, TEST_AUTHORIZATION_TOKEN, "ALL"))
            .thenReturn(null);
        DynamicList regionList = locationService.getAllRegions();

        //Then
        assertThat(regionList.getListItems()).isEmpty();
    }

    @Test
    void shouldPopulateHearingVenueDynamicList() {
        //Given
        final HearingVenue hearingVenue = HearingVenue
            .builder()
            .regionId("1")
            .courtName("courtName")
            .courtAddress("courtAddress")
            .courtTypeId(COURT_TYPE_ID)
            .build();

        //When
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(locationClient.getHearingVenues(TEST_SERVICE_AUTH_TOKEN, TEST_AUTHORIZATION_TOKEN, "1", "Y"))
            .thenReturn(List.of(hearingVenue));
        DynamicList hearingVenueList = locationService.getHearingVenuesByRegion("1");

        //Then
        assertThat(hearingVenueList).isNotNull();
        assertThat(hearingVenueList.getListItems()).extracting("label").containsExactlyInAnyOrder("courtName-courtAddress");

    }

    @Test
    void shouldPopulateHearingVenueDynamicListWithMissingCourtAddress() {
        //Given
        final HearingVenue hearingVenueResponse = HearingVenue
            .builder()
            .regionId("1")
            .courtName("courtName")
            .courtTypeId(COURT_TYPE_ID)
            .build();

        //When
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(locationClient.getHearingVenues(TEST_SERVICE_AUTH_TOKEN, TEST_AUTHORIZATION_TOKEN, "1", "Y"))
            .thenReturn(List.of(hearingVenueResponse));

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
        when(locationClient.getHearingVenues(TEST_SERVICE_AUTH_TOKEN, TEST_AUTHORIZATION_TOKEN, "1", "Y"))
            .thenReturn(null);
        DynamicList regionList = locationService.getHearingVenuesByRegion("1");

        //Then
        assertThat(regionList.getListItems()).isEmpty();
    }

    @Test
    void shouldReturnRegionIdIfSelectedRegionIsValid() {
        assertThat(locationService.getRegionId("1-region")).isEqualTo("1");
    }

    @Test
    void shouldReturnNullIfSelectedRegionIsNull() {
        assertThat(locationService.getRegionId(null)).isNull();
    }
}
