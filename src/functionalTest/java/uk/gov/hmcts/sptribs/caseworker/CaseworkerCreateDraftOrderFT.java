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
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_DRAFT_ORDER;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerCreateDraftOrderFT extends FunctionalTestSuite {

    public static final String CREATE_DRAFT_ORDER_ADD_FOOTER_MID_EVENT_URL =
        "/callbacks/mid-event?page=createDraftOrderAddDocumentFooter";

    private static final String MID_EVENT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-create-draft-order-mid-event.json";
    private static final String MID_EVENT_RESPONSE =
        "classpath:responses/response-caseworker-create-draft-order-mid-event.json";

    private static final String EMPTY_DRAFT_ORDER_CIC_LIST_ABOUT_TO_SUBMIT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-create-draft-order-empty-about-to-submit.json";
    private static final String EMPTY_DRAFT_ORDER_CIC_LIST_ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/response-caseworker-create-draft-order-empty-about-to-submit.json";

    private static final String EXISTING_DRAFT_ORDER_CIC_LIST_ABOUT_TO_SUBMIT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-create-draft-order-about-to-submit.json";
    private static final String EXISTING_DRAFT_ORDER_CIC_LIST_ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/response-caseworker-create-draft-order-about-to-submit.json";

    private static final String SUBMITTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-general.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldGenerateOrderFileInMidEventCallback() throws Exception {
        final Map<String, Object> caseData = caseData(MID_EVENT_REQUEST);
        final Response response = triggerCallback(
            caseData,
            CASEWORKER_CREATE_DRAFT_ORDER,
            CREATE_DRAFT_ORDER_ADD_FOOTER_MID_EVENT_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(MID_EVENT_RESPONSE)));
    }

    @Test
    public void shouldCreateDraftOrderCICListIfEmptyInAboutToSubmitCallback() throws Exception {
        final Map<String, Object> caseData = caseData(EMPTY_DRAFT_ORDER_CIC_LIST_ABOUT_TO_SUBMIT_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_DRAFT_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(EMPTY_DRAFT_ORDER_CIC_LIST_ABOUT_TO_SUBMIT_RESPONSE)));
    }

    @Test
    public void shouldAddToDraftOrderCICListIfNotEmptyInAboutToSubmitCallback() throws Exception {
        final Map<String, Object> caseData = caseData(EXISTING_DRAFT_ORDER_CIC_LIST_ABOUT_TO_SUBMIT_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_DRAFT_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(EXISTING_DRAFT_ORDER_CIC_LIST_ABOUT_TO_SUBMIT_RESPONSE)));
    }

    @Test
    public void shouldReturnDraftOrderCreatedConfirmationInSubmittedCallback() throws Exception {
        final Map<String, Object> caseData = caseData(SUBMITTED_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_DRAFT_ORDER, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Draft order created.");
    }
}
