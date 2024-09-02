package uk.gov.hmcts.sptribs.testutil;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@TestPropertySource("classpath:application.yaml")
@Service
public class ServiceAuthenticationGenerator {

    @Value("${s2s.name}")
    private String s2sName;

    @Value("${s2s.task_management_name}")
    private String s2sTMName;

    @Value("${idam.s2s-auth.url}")
    private String s2sUrl;

    public String generateTaskManagementToken() {
        return generate(this.s2sTMName);
    }

    public String generate() {
        return generate(this.s2sName);
    }

    public String generate(final String s2sName) {
        final Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(s2sUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(Map.of("microservice", s2sName))
            .when()
            .post("/testing-support/lease")
            .andReturn();

        assertThat(response.getStatusCode()).isEqualTo(200);

        return "Bearer " + response.getBody().asString();
    }
}
