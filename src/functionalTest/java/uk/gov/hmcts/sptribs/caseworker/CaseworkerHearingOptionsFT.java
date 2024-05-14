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
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_HEARING_OPTIONS;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_OPTIONS_REGION_DATA_MID_EVENT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerHearingOptionsFT extends FunctionalTestSuite {

    private static final String ABOUT_TO_START_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-hearing-options-about-to-start.json";
    private static final String MID_EVENT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-hearing-options-mid-event.json";
    private static final String MID_EVENT_NULL_LIST_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-hearing-options-null-list-mid-event.json";
    private static final String ABOUT_TO_SUBMIT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-hearing-options-about-to-submit.json";

    private static final String ABOUT_TO_START_RESPONSE =
        "classpath:responses/response-caseworker-hearing-options-about-to-start.json";
    private static final String MID_EVENT_RESPONSE =
        "classpath:responses/response-caseworker-hearing-options-mid-event.json";
    private static final String MID_EVENT_NULL_LIST_RESPONSE =
        "classpath:responses/response-caseworker-hearing-options-null-list-mid-event.json";

    @Test
    public void shouldPopulateRegionDataWhenAboutToStartCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(ABOUT_TO_START_REQUEST);
        final Response response = triggerCallback(caseData, CASEWORKER_HEARING_OPTIONS, ABOUT_TO_START_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(ABOUT_TO_START_RESPONSE)));
    }

    @Test
    public void shouldPopulateVenueDataWhenMidEventCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(MID_EVENT_REQUEST);
        final Response response =
            triggerCallback(caseData, CASEWORKER_HEARING_OPTIONS, HEARING_OPTIONS_REGION_DATA_MID_EVENT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(MID_EVENT_RESPONSE)));
    }

    @Test
    public void shouldPopulateVenueDataWithNullRegionListWhenMidEventCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(MID_EVENT_NULL_LIST_REQUEST);
        final Response response =
            triggerCallback(caseData, CASEWORKER_HEARING_OPTIONS, HEARING_OPTIONS_REGION_DATA_MID_EVENT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(MID_EVENT_NULL_LIST_RESPONSE)));
    }

    @Test
    public void shouldTransitionStateWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(ABOUT_TO_SUBMIT_REQUEST);
        final Response response =
            triggerCallback(caseData, CASEWORKER_HEARING_OPTIONS, ABOUT_TO_SUBMIT_URL, CaseManagement);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString(),
            json -> json.inPath("state").isEqualTo(ReadyToList.name())
        );
    }
}
