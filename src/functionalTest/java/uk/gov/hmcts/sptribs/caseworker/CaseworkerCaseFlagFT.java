package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.sptribs.caseworker.util.EventConstants;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;

@SpringBootTest
public class CaseworkerCaseFlagFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldUpdateCaseStateWhenAboutToSubmitCallbackIsInvoked() throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST);
        final String caseRef = caseData.get("hyphenatedCaseRef").toString().replaceAll("-", "");
        triggerCallback(caseData, "caseworker-create-case", SUBMITTED_URL);

        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(EventConstants.CASEWORKER_CASE_FLAG)
            .caseDetailsBefore(
                CaseDetails
                    .builder()
                    .id(Long.valueOf(caseRef))
                    .createdDate(LOCAL_DATE_TIME)
                    .caseTypeId(CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName())
                    .build())
            .caseDetails(
                CaseDetails
                    .builder()
                    .id(Long.valueOf(caseRef))
                    .createdDate(LOCAL_DATE_TIME)
                    .caseTypeId(CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName())
                    .build()
            )
            .build();

        final Response last_response = triggerCallback(request, ABOUT_TO_SUBMIT_URL);
        assertThat(last_response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldTriggerSuccessfulResponseIfSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);

        final Response response = triggerCallback(caseData, EventConstants.CASEWORKER_CASE_FLAG, SUBMITTED_URL);

        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Flag created \\n## This Flag has been added to case");
    }
}
