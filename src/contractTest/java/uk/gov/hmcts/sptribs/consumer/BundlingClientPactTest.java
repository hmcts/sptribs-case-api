package uk.gov.hmcts.sptribs.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import com.google.common.collect.Maps;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.Map;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;


@Provider("bundling-client")
@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class BundlingClientPactTest {
    private static final String ACCESS_TOKEN = "111";
    private static final String AUTHORIZATION_TOKEN = "222";

    private static final String ENDPOINT = "/api/new-bundle";


    @BeforeEach
    public void setUp() {
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.config().encoderConfig(new EncoderConfig("UTF-8", "UTF-8"));
    }

    @Pact(provider = "em_api", consumer = "sptribs-case-api")
    public RequestResponsePact executeCreateEvidenceBundleIdAndGet200Response(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.put("ServiceAuthorization", AUTHORIZATION_TOKEN);

        return builder
            .given("EM successfully returns OK Status")
            .uponReceiving("Provider receives a POST request from an SPTribs CASE API")
            .path(ENDPOINT)
            .method(org.springframework.http.HttpMethod.POST.toString())
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(new PactDslJsonBody()
                .stringType("id", "12345"))
            .toPact();
    }


    @Test
    @PactTestFor(pactMethod = "executeCreateEvidenceBundleIdAndGet200Response")
    public void should_post_to_Evidence_bundle_and_receive_code_with_200_response(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.put("ServiceAuthorization", AUTHORIZATION_TOKEN);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("response_type", "bundleCaseData");
        body.add("redirect_uri", ENDPOINT);

        String actualResponseBody =
            SerenityRest
                .given()
                .headers(headers)
                .contentType(ContentType.URLENC)
                .formParams(body)
                .when()
                .post(mockServer.getUrl() + ENDPOINT)
                .then()
                .statusCode(200)
                .and()
                .extract()
                .asString();

        assertThat(actualResponseBody).isNotNull();

        JSONObject response = new JSONObject(actualResponseBody);
        assertThat(response.get("id").toString()).isNotBlank();
    }


    }



