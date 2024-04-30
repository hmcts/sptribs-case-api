package uk.gov.hmcts.sptribs.testutil;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.BEARER;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_USER_EMAIL;

public final class IdamWireMock {

    public static final String ST_CIC_CASEWORKER = "caseworker-st_cic-caseworker";

    private static final WireMockServer IDAM_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    private IdamWireMock() {
    }

    public static void start() {
        if (!IDAM_SERVER.isRunning()) {
            IDAM_SERVER.start();
        }
    }

    public static void stopAndReset() {
        if (IDAM_SERVER.isRunning()) {
            IDAM_SERVER.stop();
            IDAM_SERVER.resetAll();
        }
    }

    public static void stubForIdamDetails(final String authorizationToken,
                                          final String userId,
                                          final String userRole) {
        IDAM_SERVER.stubFor(get("/details")
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(BEARER + authorizationToken))
            .willReturn(aResponse()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(getUserDetailsForRole(userId, userRole))));
    }


    public static void stubForIdamFailure() {
        IDAM_SERVER.stubFor(get("/details")
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(BEARER + TEST_AUTHORIZATION_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.UNAUTHORIZED.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody("Invalid idam credentials")));
    }

    public static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues
                .of("idam.api.url=" + "http://localhost:" + IDAM_SERVER.port())
                .applyTo(applicationContext.getEnvironment());
        }
    }

    private static String getUserDetailsForRole(final String userId, final String role) {
        return "{\"id\":\"" + userId
            + "\",\"email\":\"" + TEST_USER_EMAIL
            + "\",\"forename\":\"forename\",\"surname\":\"Surname\",\"roles\":[\"" + role + "\"]}";
    }

}
