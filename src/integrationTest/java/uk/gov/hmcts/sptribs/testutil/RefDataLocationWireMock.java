package uk.gov.hmcts.sptribs.testutil;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.sptribs.recordlisting.RecordListingConstants.IS_HEARING_LOCATION;
import static uk.gov.hmcts.sptribs.recordlisting.RecordListingConstants.REGION_ALL;
import static uk.gov.hmcts.sptribs.recordlisting.RecordListingConstants.REGION_ID;
import static uk.gov.hmcts.sptribs.recordlisting.RecordListingConstants.REGION_ID_1;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

public final class RefDataLocationWireMock {

    private static final WireMockServer PRD_LOCATION_SERVER = new WireMockServer(wireMockConfig().dynamicPort());
    private static final String IS_HEARING_LOCATION_VALUE = "Y";

    private RefDataLocationWireMock() {
    }

    public static void start() {
        if (!PRD_LOCATION_SERVER.isRunning()) {
            PRD_LOCATION_SERVER.start();
        }
    }

    public static void stopAndReset() {
        if (PRD_LOCATION_SERVER.isRunning()) {
            PRD_LOCATION_SERVER.stop();
            PRD_LOCATION_SERVER.resetAll();
        }
    }

    public static void stubGetHearingVenues(String selectedRegionId, String hearingVenuesResponse) {
        PRD_LOCATION_SERVER.stubFor(WireMock.get(urlPathEqualTo("/refdata/location/court-venues"))
            .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
            .withQueryParam(REGION_ID, new EqualToPattern(selectedRegionId))
            .withQueryParam(IS_HEARING_LOCATION, new EqualToPattern(IS_HEARING_LOCATION_VALUE))
            .willReturn(aResponse()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(hearingVenuesResponse)
            )
        );
    }

    public static void stubGetHearingVenuesForFailure(String selectedRegionId) {
        PRD_LOCATION_SERVER.stubFor(WireMock.get(urlPathEqualTo("/refdata/location/court-venues"))
            .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
            .withQueryParam(REGION_ID, new EqualToPattern(selectedRegionId))
            .withQueryParam(IS_HEARING_LOCATION, new EqualToPattern(IS_HEARING_LOCATION_VALUE))
            .willReturn(aResponse()
                .withStatus(HttpStatus.FORBIDDEN.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            )
        );
    }

    public static void stubGetRegions(String regionResponse) {
        PRD_LOCATION_SERVER.stubFor(WireMock.get(urlPathEqualTo("/refdata/location/regions"))
            .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
            .withQueryParam(REGION_ID_1, new EqualToPattern(REGION_ALL))
            .willReturn(aResponse()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(regionResponse)
            )
        );
    }

    public static void stubGetRegionsForFailure() {
        PRD_LOCATION_SERVER.stubFor(WireMock.get(urlPathEqualTo("/refdata/location/regions"))
            .withHeader(AUTHORIZATION, new EqualToPattern(TEST_AUTHORIZATION_TOKEN))
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern(TEST_SERVICE_AUTH_TOKEN))
            .withQueryParam(REGION_ID_1, new EqualToPattern(REGION_ALL))
            .willReturn(aResponse()
                .withStatus(HttpStatus.FORBIDDEN.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            )
        );
    }

    public static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues
                .of("location.api.baseUrl=" + "http://localhost:" + PRD_LOCATION_SERVER.port())
                .applyTo(applicationContext.getEnvironment());
        }
    }
}
