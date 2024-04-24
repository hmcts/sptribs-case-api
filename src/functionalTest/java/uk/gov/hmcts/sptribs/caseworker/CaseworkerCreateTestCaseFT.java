package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.HashMap;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerCreateTestCaseFT extends FunctionalTestSuite {

    private static final String RESPONSE = "classpath:responses/response-caseworker-submit-test-case.json";

    private static final String CASEWORKER_CREATE_TEST_CASE_EVENT_ID = "create-test-case";

    @Test
    public void shouldSuccessfullySubmitWhenAboutToSubmitCallbackIsTriggered() throws Exception {
        JSONObject emptyJsonObject = new JSONObject();
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("emptyString", "");
        caseData.put("emptyJsonObject", emptyJsonObject);

        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_TEST_CASE_EVENT_ID, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }
}
