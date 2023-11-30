package uk.gov.hmcts.sptribs.consumer;

import  au.com.dius.pact.consumer.dsl.DslPart;
import feign.Request.HttpMethod;
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


@Provider("bundling-client")
@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class BundlingClientPactTest {
    private static final String ACCESS_TOKEN = "111";
    private static final String AUTHORIZATION_TOKEN = "222";

    private static final String ENDPOINT = "/api/new-bundle";




    @Pact(provider = "bundling-client", consumer = "your-consumer")
    public RequestResponsePact createBundlePact(PactDslWithProvider builder) {

        Map<String, String> headers = com.google.common.collect.Maps.newHashMap();
        headers.put(org.springframework.http.HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put(org.springframework.http.HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.put("ServiceAuthorization", AUTHORIZATION_TOKEN);


        DslPart body = new PactDslJsonBody()
            .stringValue("status", "success")
            .stringValue("message", "Bundle created successfully");
        return builder
            .uponReceiving("A request to create a bundle")
            .path("/api/new-bundle")
            .method(HttpMethod.POST.toString())
            .headers("Content-Type", MediaType.APPLICATION_JSON_VALUE, "Service-Authorization",
                "your-service-auth", "Authorization", "your-authorization")
            .body(body)
            .willRespondWith()
            .status(200)
            .body(body)
            .toPact();
    }



    @Test
    @au.com.dius.pact.consumer.junit5.PactTestFor(providerName = "bundling-client")
    public void testCreateBundle(au.com.dius.pact.consumer.MockServer mockServer) {
        Map<String, String> headers = com.google.common.collect.Maps.newHashMap();
        headers.put(org.springframework.http.HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put(org.springframework.http.HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.put("ServiceAuthorization", AUTHORIZATION_TOKEN);
        org.springframework.util.MultiValueMap<String, String> body = new org.springframework.util.LinkedMultiValueMap<>();
        String actualResponseBody =
            net.serenitybdd.rest.SerenityRest
                .given()
                .headers(headers)
                .contentType(io.restassured.http.ContentType.URLENC)
                .formParams(body)
                .when()
                .post(mockServer.getUrl() + ENDPOINT)
                .then()
                .statusCode(200)
                .and()
                .extract()
                .asString();
         try {
         java.net.http.HttpResponse response = org.apache.http.client.fluent.Request.Post("/api/new-bundle")
         .bodyString("{key: value}", ContentType.APPLICATION_JSON)
         .execute()
             .returnResponse();

         } catch (Exception e) {

         e.printStackTrace();
         }
    }

    }



