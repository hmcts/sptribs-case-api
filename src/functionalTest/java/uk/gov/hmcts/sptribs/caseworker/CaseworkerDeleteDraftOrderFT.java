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
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_DELETE_DRAFT_ORDER;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerDeleteDraftOrderFT extends FunctionalTestSuite {

    private static final String EXISTING_DRAFT_ORDER_CIC_LIST_ABOUT_TO_START =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-delete-draft-order-about-to-start.json";

    private static final String EXISTING_DRAFT_ORDER_CIC_LIST_ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/response-caseworker-delete-draft-order-about-to-submit.json";

    private static final String SUBMITTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-general.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";


    @Test
    public void shouldReturnEmptyRemovedDraftListAndNullDynamicDraftListWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(EXISTING_DRAFT_ORDER_CIC_LIST_ABOUT_TO_START);

        final Response response = triggerCallback(caseData, CASEWORKER_DELETE_DRAFT_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_ARRAY_ORDER)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(EXISTING_DRAFT_ORDER_CIC_LIST_ABOUT_TO_SUBMIT_RESPONSE)));
    }

    @Test
    public void shouldReturnDraftOrderDeletedConfirmationInSubmittedCallback() throws Exception {
        final Map<String, Object> caseData = caseData(SUBMITTED_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_DELETE_DRAFT_ORDER, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Draft order deleted.");
    }
}
