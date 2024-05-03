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
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_HEARING_SUMMARY;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerCreateHearingSummaryFT extends FunctionalTestSuite {
    public static final String HEARING_RECORDING_UPLOAD_DOCUMENTS_MID_EVENT_URL
        = "/callbacks/mid-event?page=hearingRecordingUploadPage";

    public static final String SELECT_HEARING_MID_EVENT_URL
        = "/callbacks/mid-event?page=selectHearing";

    public static final String HEARING_VENUES_MID_EVENT_URL
        = "/callbacks/mid-event?page=listingDetails";

    private static final String CALLBACK_CASE_DATA = "classpath:request/casedata/ccd-callback-casedata.json";

    private static final String SELECT_HEARING_MID_EVENT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-create-hearing-summary-select-hearing-mid-event.json";
    private static final String SELECT_HEARING_MID_EVENT_RESPONSE =
        "classpath:responses/response-caseworker-create-hearing-summary-select-hearing-mid-event.json";

    private static final String HEARING_VENUES_MID_EVENT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-create-hearing-summary-hearing-venues-mid-event.json";
    private static final String HEARING_VENUES_MID_EVENT_RESPONSE =
        "classpath:responses/response-caseworker-create-hearing-summary-hearing-venues-mid-event.json";

    private static final String HEARING_RECORDING_UPLOAD_MID_EVENT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-create-hearing-summary-hearing-recording-upload-mid-event.json";
    private static final String HEARING_RECORDING_UPLOAD_MID_EVENT_RESPONSE =
        "classpath:responses/response-caseworker-create-hearing-summary-hearing-recording-upload-mid-event.json";

    private static final String ABOUT_TO_SUBMIT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-create-hearing-summary-about-to-submit.json";
    private static final String ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/response-caseworker-create-hearing-summary-about-to-submit.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldPopulateJudgeAndMemberListInAboutToStartCallback() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_CASE_DATA);

        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_HEARING_SUMMARY, ABOUT_TO_START_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .inPath("$.data.judge.list_items")
            .isArray()
            .isNotEmpty();

        assertThatJson(response.asString())
            .inPath("$.data.judgeList")
            .isArray()
            .isNotEmpty();

        assertThatJson(response.asString())
            .inPath("$.data.memberList")
            .isArray()
            .isNotEmpty();
    }

    @Test
    public void shouldSelectListingInMidEventCallback() throws Exception {
        final Map<String, Object> caseData = caseData(SELECT_HEARING_MID_EVENT_REQUEST);

        final Response response = triggerCallback(
            caseData,
            CASEWORKER_CREATE_HEARING_SUMMARY,
            SELECT_HEARING_MID_EVENT_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(SELECT_HEARING_MID_EVENT_RESPONSE)));
    }

    @Test
    public void shouldValidateHearingVenueInputInMidEventCallback() throws Exception {
        final Map<String, Object> caseData = caseData(HEARING_VENUES_MID_EVENT_REQUEST);

        final Response response = triggerCallback(
            caseData,
            CASEWORKER_CREATE_HEARING_SUMMARY,
            HEARING_VENUES_MID_EVENT_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(HEARING_VENUES_MID_EVENT_RESPONSE)));
    }

    @Test
    public void shouldValidateHearingSummaryRecFilesInMidEventCallback() throws Exception {
        final Map<String, Object> caseData = caseData(HEARING_RECORDING_UPLOAD_MID_EVENT_REQUEST);

        final Response response = triggerCallback(
            caseData,
            CASEWORKER_CREATE_HEARING_SUMMARY,
            HEARING_RECORDING_UPLOAD_DOCUMENTS_MID_EVENT_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(HEARING_RECORDING_UPLOAD_MID_EVENT_RESPONSE)));
    }

    @Test
    public void shouldNotReturnErrorsIfRecFileListIsEmpty() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_CASE_DATA);

        final Response response = triggerCallback(
            caseData,
            CASEWORKER_CREATE_HEARING_SUMMARY,
            HEARING_RECORDING_UPLOAD_DOCUMENTS_MID_EVENT_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath("$.errors")
            .isArray()
            .isEmpty();
    }

    @Test
    public void shouldUpdateCategoryToCaseworkerDocumentInAboutToSubmitCallback() throws Exception {
        final Map<String, Object> caseData = caseData(ABOUT_TO_SUBMIT_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_HEARING_SUMMARY, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(ABOUT_TO_SUBMIT_RESPONSE)));
    }

    @Test
    public void shouldSuccessfullySubmit() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_CASE_DATA);

        final Response response = triggerCallback(caseData, CASEWORKER_CREATE_HEARING_SUMMARY, SUBMITTED_URL);

        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo(
                "# Hearing summary created \\n"
                    + "## This hearing summary has been added to the case record."

            );
    }
}
