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
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_AND_SEND_ORDER;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerCreateAndSendOrderFT extends FunctionalTestSuite {

    private static final String ABOUT_TO_START_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-general.json";
    private static final String ABOUT_TO_START_RESPONSE =
        "classpath:responses/response-caseworker-create-and-send-order-about-to-start.json";

    private static final String CALLBACK_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-create-and-send-order-callback-request.json";
    private static final String CALLBACK_RESPONSE =
        "classpath:responses/response-caseworker-create-and-send-order-about-to-submit.json";

    private static final String NEW_ORDER_TEMPLATE_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-create-and-send-order-new-anonymised-callback-request.json";
    private static final String NEW_ORDER_TEMPLATE_RESPONSE =
        "classpath:responses/response-caseworker-create-and-send-order-new-anonymised-about-to-submit.json";
    private static final String NEW_ORDER_TEMPLATE_NON_ANONYMISED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-create-and-send-order-new-callback-request.json";
    private static final String NEW_ORDER_TEMPLATE_NON_ANONYMISED_RESPONSE =
        "classpath:responses/response-caseworker-create-and-send-order-new-about-to-submit.json";

    private static final String UPLOADED_ORDER_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-create-and-send-order-upload-callback-request.json";
    private static final String UPLOADED_ORDER_RESPONSE =
        "classpath:responses/response-caseworker-create-and-send-order-upload-about-to-submit.json";

    private static final String REQUEST_UNHAPPY_ABOUT_TO_SUBMIT =
        "classpath:request/casedata/ccd-callback-casedata-create-and-send-order-unhappy-about-to-submit.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldSetOrderIssueTypesAndTemplatesInAboutToStartCallback() throws Exception {
        final Map<String, Object> caseData = caseData(ABOUT_TO_START_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_AND_SEND_ORDER, ABOUT_TO_START_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(ABOUT_TO_START_RESPONSE)));
    }

    @Test
    public void shouldRetainSelectedOrderDataWhenAboutToSubmitCallbackIsTriggered() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_AND_SEND_ORDER, ABOUT_TO_SUBMIT_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(CALLBACK_RESPONSE)));
    }

    @Test
    public void shouldAddNewAnonymisedOrderFromTemplateWhenAboutToSubmitCallbackIsTriggered() throws Exception {
        final Map<String, Object> caseData = caseData(NEW_ORDER_TEMPLATE_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_AND_SEND_ORDER, ABOUT_TO_SUBMIT_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(NEW_ORDER_TEMPLATE_RESPONSE)));
    }

    @Test
    public void shouldAddNonAnonymisedOrderFromTemplateWhenAboutToSubmitCallbackIsTriggered() throws Exception {
        final Map<String, Object> caseData = caseData(NEW_ORDER_TEMPLATE_NON_ANONYMISED_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_AND_SEND_ORDER, ABOUT_TO_SUBMIT_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(NEW_ORDER_TEMPLATE_NON_ANONYMISED_RESPONSE)));
    }

    @Test
    public void shouldAddNewOrderFromUploadWhenAboutToSubmitCallbackIsTriggered() throws Exception {
        final Map<String, Object> caseData = caseData(UPLOADED_ORDER_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_AND_SEND_ORDER, ABOUT_TO_SUBMIT_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(UPLOADED_ORDER_RESPONSE)));

        long testCaseRef = Long.parseLong(caseData.get("hyphenatedCaseRef").toString().replace("-", ""));

        assertThat(caseDocumentsFTDataManager.getDocumentEntities(testCaseRef)).hasSize(1);
        assertThat(caseDocumentsFTDataManager.getDocumentEntities(testCaseRef).getFirst().getId())
            .isNotNull();
        assertThat(caseDocumentsFTDataManager.getDocumentEntities(testCaseRef).getFirst().getCaseReferenceNumber())
            .isEqualTo(Long.parseLong(caseData.get("hyphenatedCaseRef").toString().replace("-", "")));
        assertThat(caseDocumentsFTDataManager.getDocumentEntities(testCaseRef).getFirst().getCategoryId()).isEqualTo("TD");
        assertThat(caseDocumentsFTDataManager.getDocumentEntities(testCaseRef).getFirst().getSavedAt()).isNotNull();
        assertThat(caseDocumentsFTDataManager.getDocumentEntities(testCaseRef).getFirst().isDraft()).isFalse();
        assertThat(caseDocumentsFTDataManager.getDocumentEntities(testCaseRef).getFirst().isSentToApplicantViaContactParties()).isFalse();
        assertThat(caseDocumentsFTDataManager.getDocumentEntities(testCaseRef).getFirst().getDocumentUrl())
            .isNotNull();
        assertThat(caseDocumentsFTDataManager.getDocumentEntities(testCaseRef).getFirst().getDocumentFilename())
            .isNotNull();
        assertThat(caseDocumentsFTDataManager.getDocumentEntities(testCaseRef).getFirst().getDocumentBinaryUrl())
            .isNotNull();
    }

    @Test
    public void shouldReturnHappyResponseWhenValidSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_AND_SEND_ORDER, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Order sent \n## A notification has been sent to: Representative");

        long testCaseRef = Long.parseLong(caseData.get("hyphenatedCaseRef").toString().replace("-", ""));

        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef)).hasSize(1);
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getId())
            .isNotNull();
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getCaseReferenceNumber())
            .isEqualTo(Long.parseLong(caseData.get("hyphenatedCaseRef").toString().replace("-", "")));
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getEventType())
            .isEqualTo("NEW_ORDER_ISSUED_POST");
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getSentOn())
            .isNotNull();
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getSentFrom())
            .isNotNull();
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getSentTo())
            .isNotNull();
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getCorrespondenceType())
            .isEqualTo("Letter");
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getDocumentUrl())
            .isNotNull();
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getDocumentFilename())
            .isNotNull();
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getDocumentBinaryUrl())
            .isNotNull();
    }

    @Test
    public void shouldReturnUnhappyResponseWhenInvalidSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_UNHAPPY_ABOUT_TO_SUBMIT);

        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_AND_SEND_ORDER, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Send order notification failed \n## Please resend the order");
    }
}
