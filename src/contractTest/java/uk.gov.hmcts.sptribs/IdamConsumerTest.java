package uk.gov.hmcts.sptribs;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;
import io.restassured.RestAssured;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@PactDirectory("pacts")
class IdamConsumerTest {

    private static final String IDAM_USER_DETAILS = "/details";

    @Pact(provider = "idam_user_details", consumer = "sptribs-case-api")
    V4Pact executeIdamUserDetailApi(PactDslWithProvider builder) {
        Map<String, String> responseHeaders = Map.of(HttpHeaders.AUTHORIZATION, "Bearer UserAuthToken");

        return builder
            .given("a user exists")
            .uponReceiving("Provider receives a token request and send user details to the API")
            .path(IDAM_USER_DETAILS)
            .method(GET.toString())
            .willRespondWith()
            .status(OK.value())
            .headers(responseHeaders)
            .body(createAuthResponse())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "executeIdamUserDetailApi")
    void shouldReceiveUserDetails(MockServer mockServer) {
        String responseBody = RestAssured
            .given()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .log().all(true)
            .when()
            .get(mockServer.getUrl() + IDAM_USER_DETAILS)
            .then()
            .statusCode(200)
            .and()
            .extract()
            .asString();

        JSONObject response = new JSONObject(responseBody);
        Assertions.assertThat(response).isNotNull();
    }

    private PactDslJsonBody createAuthResponse() {
        return new PactDslJsonBody()
            .stringType("id", "123432")
            .stringType("forename", "Joe")
            .stringType("surname", "Bloggs")
            .stringType("email", "joe.bloggs@hmcts.net")
            .booleanType("active", true)
            .minArrayLike("roles", 2)
            .stringType("caseworker")
            .stringType("citizen");
    }
}
