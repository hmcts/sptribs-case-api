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
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CONTACT_PARTIES;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerContactPartiesFT extends FunctionalTestSuite {

    private static final String ABOUT_TO_START_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-contact-parties-about-to-start.json";
    private static final String ABOUT_TO_START_RESPONSE =
        "classpath:responses/response-caseworker-contact-parties-about-to-start.json";

    private static final String SUBMITTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-contact-parties-submitted.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldPrepareContactPartiesDocumentListInAboutToStartCallback() throws Exception {

        final Map<String, Object> caseData = caseData(ABOUT_TO_START_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_CONTACT_PARTIES, ABOUT_TO_START_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(ABOUT_TO_START_RESPONSE)));
    }

    @Test
    public void shouldSendNotificationsInSubmittedCallback() throws Exception {

        final Map<String, Object> caseData = caseData(SUBMITTED_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_CONTACT_PARTIES, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Message sent \\n## A notification has been sent to: Subject");
    }
}
