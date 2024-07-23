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
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_STAY_THE_CASE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerStayTheCaseFT extends FunctionalTestSuite {

    private static final String CALLBACK_CASE_DATA = "classpath:request/casedata/ccd-callback-casedata-general.json";

    private static final String ABOUT_TO_START_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-stay-the-case-about-to-start.json";
    private static final String ABOUT_TO_START_RESPONSE =
        "classpath:responses/response-caseworker-stay-the-case-about-to-start.json";
    private static final String ABOUT_TO_START_CLEAR_FIELDS_RESPONSE =
        "classpath:responses/response-caseworker-stay-the-case-cleared-about-to-start.json";

    private static final String SUBMITTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-stay-the-case-submitted.json";
    private static final String SUBMITTED_INCOMPLETE_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-stay-the-case-incomplete-submitted.json";


    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldAlterCaseDataIfCaseStateIsNotCaseStayedInAboutToStart() throws Exception {
        final Map<String, Object> caseData = caseData(ABOUT_TO_START_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_STAY_THE_CASE, ABOUT_TO_START_URL, CaseManagement);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(ABOUT_TO_START_CLEAR_FIELDS_RESPONSE)));
    }

    @Test
    public void shouldNotAlterCaseDataIfCaseStateIsCaseStayedInAboutToStart() throws Exception {
        final Map<String, Object> caseData = caseData(ABOUT_TO_START_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_STAY_THE_CASE, ABOUT_TO_START_URL, CaseStayed);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(ABOUT_TO_START_RESPONSE)));
    }

    @Test
    public void shouldSetIsCaseStayedAndSetCaseStayedStateInAboutToSubmit() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_CASE_DATA);

        final Response response = triggerCallback(caseData, CASEWORKER_STAY_THE_CASE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath("$.data.stayIsCaseStayed")
            .isEqualTo("Yes");
        assertThatJson(response.asString())
            .inPath("$.state")
            .isEqualTo("CaseStayed");
    }

    @Test
    public void submittedSuccess() throws Exception {
        final Map<String, Object> caseData = caseData(SUBMITTED_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_STAY_THE_CASE, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Stay Added to Case \n## A notification has been sent to: Subject, Representative, Applicant");
    }

    @Test
    public void submittedFailed() throws Exception {
        final Map<String, Object> caseData = caseData(SUBMITTED_INCOMPLETE_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_STAY_THE_CASE, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        System.out.println(response.asString());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Case stay notification failed \n## Please resend the notification");
    }
}
