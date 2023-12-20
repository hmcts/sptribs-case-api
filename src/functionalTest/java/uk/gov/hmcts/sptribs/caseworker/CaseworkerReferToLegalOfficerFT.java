package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.caseworker.util.EventConstants;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerReferToLegalOfficerFT extends uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata.json";
    private static final String REQUEST_SUBMITTED ="classpath:request/casedata/ccd-callback-casedata-refer-cases-to-legal-officer-submitted.json";

    @Test
    public void shouldRaiseErrorIfLegalOfficerDetailsAreNull() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);

        final io.restassured.response.Response response =
            triggerCallback(caseData, EventConstants.CASEWORKER_REFER_TO_LEGAL_OFFICER, ABOUT_TO_SUBMIT_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-caseworker-refer-cases-to-legal-officer-about-to-submit.json"
            )));
    }

    @Test
    public void shouldSubmitCaseSuccessfullyWhenReferToLegalOfficer() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_SUBMITTED);

        final Response response = triggerCallback(caseData, EventConstants.CASEWORKER_REFER_TO_LEGAL_OFFICER, SUBMITTED_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(REQUEST_SUBMITTED
               // "classpath:responses/response-caseworker-refer-cases-to-legal-officer-submitted.json"
            )));
    }
}
