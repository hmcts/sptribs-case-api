package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.Ignore;
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
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
@Ignore
public class CaseworkerIssueCaseFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata.json";
    private static final String REQUEST_SUBMITTED =
        "classpath:request/casedata/ccd-callback-casedata-issue-cases-submitted.json";

    @Test
    @Ignore
    public void shouldCaseworkerSubmitAndIssueCaseSuccessfullyWhenThereIsNoError() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);

        final Response response =
            triggerCallback(caseData, EventConstants.CASEWORKER_ISSUE_CASE, ABOUT_TO_SUBMIT_URL);
        // Log the actual response
        String stringResponse = response.asString();
        System.out.println("Received response body: " + stringResponse);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-caseworker-issue-cases-about-to-submit.json"
            )));
    }

//    @Test
//    public void shouldCaseworkerIssueCaseSuccessfully() throws Exception {
//        final Map<String, Object> caseData = caseData(REQUEST_SUBMITTED);
//
//        final Response response = triggerCallback(caseData,
//            EventConstants.CASEWORKER_ISSUE_CASE, SUBMITTED_URL);
//        assertThat(response.getStatusCode()).isEqualTo(OK.value());
//
//        assertThatJson(response.asString())
//            .when(IGNORING_EXTRA_FIELDS)
//            .isEqualTo(json(expectedResponse(REQUEST_SUBMITTED
//            )));
//    }
}
