package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_EDIT_CASE;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerEditCaseFT extends FunctionalTestSuite {
    private static final String CALLBACK_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-edit-case-request.json";

    private static final String RESPONSE_ABOUT_TO_SUBMIT =
        "classpath:responses/response-caseworker-edit-case-about-to-submit.json";

    private static final String INITIALISE_FLAGS_CALLBACK_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-edit-case-request-initialise-flags.json";

    private static final String INITIALISE_FLAGS_RESPONSE_ABOUT_TO_SUBMIT =
        "classpath:responses/response-caseworker-edit-case-about-to-submit-initialise-flags.json";

    private static final String UPDATE_FLAGS_CALLBACK_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-edit-case-request-update-flags.json";

    private static final String UPDATE_FLAGS_RESPONSE_ABOUT_TO_SUBMIT =
        "classpath:responses/response-caseworker-edit-case-about-to-submit-update-flags.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldSuccessfullyInitialiseFieldsWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_EDIT_CASE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_ARRAY_ORDER)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE_ABOUT_TO_SUBMIT)));
    }

    @Test
    public void shouldReceiveValidResponseWhenSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_EDIT_CASE, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Case Updated");
    }

    @Test
    public void shouldSuccessfullyInitialiseFlagsWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(INITIALISE_FLAGS_CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_EDIT_CASE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_ARRAY_ORDER)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(INITIALISE_FLAGS_RESPONSE_ABOUT_TO_SUBMIT)));
    }

    @Test
    public void shouldSuccessfullyUpdateFlagPartyNamesWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(UPDATE_FLAGS_CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_EDIT_CASE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_ARRAY_ORDER)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(UPDATE_FLAGS_RESPONSE_ABOUT_TO_SUBMIT)));
    }
}
