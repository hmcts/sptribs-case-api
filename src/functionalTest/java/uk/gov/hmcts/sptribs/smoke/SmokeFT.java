package uk.gov.hmcts.sptribs.smoke;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@SpringBootTest()

public class SmokeFT{

    @Value("${test.url:http://localhost:4013}")
    private String testUrl;


    @Test
    public void testLandingPageAccessibility() {

        try {
            URL url = new URL(testUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");


            int responseCode = connection.getResponseCode();


            if (responseCode == 200) {
                System.out.println("Landing page is accessible. Smoke test passed.");
            } else {
                System.err.println("Landing page is not accessible. Smoke test failed. Status code: " + responseCode);
            }

            connection.disconnect();

            assertEquals(200, responseCode);

        } catch (IOException e) {

            System.err.println("Failed to make the GET request: " + e.getMessage());

        }

    }



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
