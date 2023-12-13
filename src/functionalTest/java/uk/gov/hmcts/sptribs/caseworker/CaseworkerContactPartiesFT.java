package uk.gov.hmcts.sptribs.caseworker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.caseworker.util.EventConstants;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerContactPartiesFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata.json";

    @Test
    public void shouldContactOnePartyAfterCaseSubmit() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);

        final io.restassured.response.Response response = triggerCallback(caseData,
            EventConstants.CASEWORKER_CONTACT_PARTIES, ABOUT_TO_SUBMIT_URL);
        int statusCode = response.getStatusCode();
        // Log the actual response
        String stringResponse = response.asString();
        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(stringResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-caseworker-contact-parties-about-to-submit.json"
            )));
    }
}
