package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_ADD_NOTE;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerAddNoteFT extends FunctionalTestSuite {

    private static final String REQUEST_ABOUT_TO_SUBMIT = "classpath:request/casedata/ccd-callback-casedata-general.json";
    private static final String REQUEST_MULTIPLE_NOTES_ABOUT_TO_SUBMIT =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-multiple-notes-about-to-submit.json";

    private static final String RESPONSE_ABOUT_TO_SUBMIT = "classpath:responses/response-caseworker-add-notes-about-to-submit.json";
    private static final String RESPONSE_MULTIPLE_NOTES_ABOUT_TO_SUBMIT =
        "classpath:responses/response-caseworker-add-multiple-notes-about-to-submit.json";

    @Test
    public void shouldUpdateCaseDataWithNotesWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_ABOUT_TO_SUBMIT);

        final Response response = triggerCallback(caseData,CASEWORKER_ADD_NOTE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE_ABOUT_TO_SUBMIT)));
    }

    @Test
    public void shouldUpdateCaseDataWithMultipleNotesWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_MULTIPLE_NOTES_ABOUT_TO_SUBMIT);

        final Response response = triggerCallback(caseData, CASEWORKER_ADD_NOTE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE_MULTIPLE_NOTES_ABOUT_TO_SUBMIT)));
    }
}
