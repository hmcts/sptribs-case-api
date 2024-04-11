package uk.gov.hmcts.sptribs.citizen.event;

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
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CicUpdateCaseEventFT extends FunctionalTestSuite {

    private static final String UPDATE_CASE_EVENT = "citizen-cic-update-dss-application";
    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-update-case-about-to-submit.json";
    private static final String RESPONSE = "classpath:responses/response-citizen-update-case-about-to-submit.json";

    @Test
    void shouldSaveDataInAboutToSubmitCallback() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);

        final Response response = triggerCallback(caseData, UPDATE_CASE_EVENT, ABOUT_TO_SUBMIT_URL);
        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }
}
