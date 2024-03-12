package uk.gov.hmcts.sptribs.citizen.event;

import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
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
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_DSS_UPDATE_CASE_SUBMISSION;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
@Disabled("Disabled until after testing completed in AAT")
public class CicDssUpdateCaseEventFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-dss-update-case-submission-about-to-submit.json";
    private static final String RESPONSE =
        "classpath:responses/response-dss-update-case-submission-about-to-submit.json";

    private static final String REQUEST_EMPTY_APPLICANT_DOCUMENTS_UPLOADED =
        "classpath:request/casedata/ccd-callback-casedata-dss-update-case-submission-empty-docs-about-to-submit.json";
    private static final String RESPONSE_EMPTY_APPLICANT_DOCUMENTS_UPLOADED =
        "classpath:responses/response-dss-update-case-submission-empty-docs-about-to-submit.json";

    private static final String REQUEST_SUBMITTED_CALLBACK =
        "classpath:request/casedata/ccd-callback-casedata-dss-update-case-submission-submitted.json";
    private static final String INVALID_REQUEST_SUBMITTED_CALLBACK =
        "classpath:request/casedata/ccd-callback-casedata-dss-update-case-submission-invalid-submitted.json";


    @Test
    public void shouldAddDocumentsAndMessagesToExistingListsInAboutToSubmitCallback()
        throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST);

        final Response response = triggerCallback(caseData, CITIZEN_DSS_UPDATE_CASE_SUBMISSION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldCreateNewListsWhenExistingDocumentsAndMessagesListIsEmptyInAboutToSubmitCallback()
        throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST_EMPTY_APPLICANT_DOCUMENTS_UPLOADED);

        final Response response = triggerCallback(caseData, CITIZEN_DSS_UPDATE_CASE_SUBMISSION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE_EMPTY_APPLICANT_DOCUMENTS_UPLOADED)));
    }

    @Test
    public void shouldSendEmailNotificationsInSubmittedCallback()
        throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST_SUBMITTED_CALLBACK);

        final Response response = triggerCallback(caseData, CITIZEN_DSS_UPDATE_CASE_SUBMISSION, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString(),
            json -> json.inPath("confirmation_header").isEqualTo("# CIC Dss Update Case Event Email notifications sent")
        );
    }

    @Test
    public void shouldReturnErrorIfSendEmailNotificationsInSubmittedCallbackFails()
        throws Exception {

        final Map<String, Object> caseData = caseData(INVALID_REQUEST_SUBMITTED_CALLBACK);

        final Response response = triggerCallback(caseData, CITIZEN_DSS_UPDATE_CASE_SUBMISSION, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString(),
            json -> json.inPath("confirmation_header")
                .isEqualTo("# CIC Dss Update Case Event Email notification failed %n## Please resend the notification")
        );
    }
}
