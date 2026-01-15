package uk.gov.hmcts.sptribs.smoke;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class SmokeFT {

    private static final String TEST_URL_PROPERTY = "test.url";
    private static final String TEST_URL_ENV = "TEST_URL";
    private static final String DEFAULT_TEST_URL = "http://localhost:4013";

    private final String testUrl = Optional.ofNullable(System.getProperty(TEST_URL_PROPERTY))
        .orElseGet(() -> Optional.ofNullable(System.getenv(TEST_URL_ENV)).orElse(DEFAULT_TEST_URL));

    @Test
    public void testHealthEndpoint() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured
            .given()
            .baseUri(testUrl)
            .get("/health")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString();
    }
}
