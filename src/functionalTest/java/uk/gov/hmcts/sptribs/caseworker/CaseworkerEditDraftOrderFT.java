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
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_EDIT_DRAFT_ORDER;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.EDIT_DRAFT_ORDER_MID_EVENT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerEditDraftOrderFT extends FunctionalTestSuite {

    private static final String REQUEST_MID_EVENT =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-edit-draft-order-mid-event.json";
    private static final String CALLBACK_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-edit-draft-order-about-to-submit.json";
    private static final String GENERAL_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-general.json";
    private static final String RESPONSE_MID_EVENT = "classpath:responses/response-caseworker-edit-draft-order-mid-event.json";
    private static final String RESPONSE_ABOUT_TO_SUBMIT = "classpath:responses/response-caseworker-edit-draft-order-about-to-submit.json";
    private static final String RESPONSE_ABOUT_TO_START = "classpath:responses/response-caseworker-edit-draft-order-about-to-start.json";
    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldSuccessFullyUpdateDraftOrderInformationWhenMidEventCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_MID_EVENT);

        final Response response = triggerCallback(caseData, CASEWORKER_EDIT_DRAFT_ORDER, EDIT_DRAFT_ORDER_MID_EVENT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_ARRAY_ORDER)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE_MID_EVENT)));
    }

    @Test
    public void shouldSetCurrentEventInAboutToStartCallback() throws Exception {
        final Map<String, Object> caseData = caseData(GENERAL_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_EDIT_DRAFT_ORDER, ABOUT_TO_START_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
                .when(IGNORING_EXTRA_FIELDS)
                .isEqualTo(json(expectedResponse(RESPONSE_ABOUT_TO_START)));
    }

    @Test
    public void shouldSuccessFullyInitialiseDraftOrderListWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_EDIT_DRAFT_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_ARRAY_ORDER)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE_ABOUT_TO_SUBMIT)));
    }

    @Test
    public void shouldTriggerSuccessfulResponseIfSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_EDIT_DRAFT_ORDER, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Draft order updated \n## Use 'Send order' to send the case documentation to parties in the case");
    }
}
