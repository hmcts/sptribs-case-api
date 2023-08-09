package uk.gov.hmcts.sptribs.smoke;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest()
public class SmokeFT {

    @Value("${test.url:https://manage-case.aat.platform.hmcts.net}")
    private String testUrl;

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


    @Test
    public void testEndpoint() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured
            .given()
            .baseUri(testUrl)
            .get("/")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString();
    }
}
