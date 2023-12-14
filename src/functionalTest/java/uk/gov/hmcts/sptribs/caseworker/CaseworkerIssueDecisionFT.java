package uk.gov.hmcts.sptribs.caseworker;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@org.springframework.boot.test.context.SpringBootTest
public class CaseworkerIssueDecisionFT extends uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata.json";

    @org.junit.jupiter.api.Test
    public void shouldUpdateCaseDataWithNotesWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final java.util.Map<String, Object> caseData = caseData(REQUEST);

        final io.restassured.response.Response response =
            triggerCallback(caseData, uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_ISSUE_DECISION, ABOUT_TO_SUBMIT_URL);
        // Log the actual response
        String stringResponse = response.asString();
        System.out.println("Received response body: " + stringResponse);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-caseworker-issue-decisions-about-to-submit.json"
            )));
    }
}
