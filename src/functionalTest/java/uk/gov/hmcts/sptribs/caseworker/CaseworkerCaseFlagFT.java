package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.sptribs.caseworker.util.EventConstants;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;

@SpringBootTest
public class CaseworkerCaseFlagFT extends FunctionalTestSuite {

    private static final String REQUEST_SUBMITTED = "classpath:request/casedata/ccd-callback-casedata-general.json";
    private static final String ABOUT_TO_SUBMIT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-manage-case-flag-about-to-submit.json";
    private static final String ABOUT_TO_SUBMIT_BEFORE_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-manage-case-flag-about-to-submit-before.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldMergeAnonymityFlagsOnAboutToSubmit() throws Exception {
        final Map<String, Object> caseData = caseData(ABOUT_TO_SUBMIT_REQUEST);
        final Map<String, Object> caseDataBefore = caseData(ABOUT_TO_SUBMIT_BEFORE_REQUEST);

        final Response response = triggerCallback(caseData, caseDataBefore, EventConstants.CASEWORKER_CASE_FLAG, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString()).inPath("$.data.caseFlags.details").isArray().hasSize(1);
    }

    @Test
    @Disabled("New case needs to be created before updating supplementary data")
    public void shouldTriggerSuccessfulResponseIfSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_SUBMITTED);

        final Long caseId = createCaseInCcd().getId();
        functionalTestDataManager.addReference(caseId);

        final String hyphenatedCaseRef = CaseData.builder().build().formatCaseRef(caseId);
        caseData.put("hyphenatedCaseRef", hyphenatedCaseRef);

        final CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(EventConstants.CASEWORKER_CASE_FLAG)
            .caseDetails(
                CaseDetails.builder()
                    .id(caseId)
                    .data(caseData)
                    .caseTypeId(CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName())
                    .build())
            .build();

        final Response response = triggerCallback(callbackRequest, SUBMITTED_URL);

        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Flag created \n## This Flag has been added to case");
    }

    @Test
    @Disabled("New case needs to be created before updating supplementary data")
    public void shouldSendAnonymityNotificationOnSubmittedCallbackWhenNewlyApplied() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_SUBMITTED);
        caseData.put("cicCaseAnonymiseYesOrNo", "Yes");
        caseData.put("cicCaseAnonymisedAppellantName", "AAC");

        final Map<String, Object> caseDataBefore = new java.util.HashMap<>(caseData);
        caseDataBefore.put("cicCaseAnonymiseYesOrNo", "No");
        caseDataBefore.remove("cicCaseAnonymisedAppellantName");
        caseDataBefore.put("cicCaseAnonymityAlreadyApplied", "No");

        final Long caseId = createCaseInCcd().getId();
        functionalTestDataManager.addReference(caseId);

        final String hyphenatedCaseRef = CaseData.builder().build().formatCaseRef(caseId);
        caseData.put("hyphenatedCaseRef", hyphenatedCaseRef);
        caseDataBefore.put("hyphenatedCaseRef", hyphenatedCaseRef);

        final CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(EventConstants.CASEWORKER_CASE_FLAG)
            .caseDetails(
                CaseDetails.builder()
                    .id(caseId)
                    .data(caseData)
                    .caseTypeId(CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName())
                    .build())
            .caseDetailsBefore(
                CaseDetails.builder()
                    .id(caseId)
                    .data(caseDataBefore)
                    .caseTypeId(CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName())
                    .build())
            .build();

        final Response response = triggerCallback(callbackRequest, SUBMITTED_URL);

        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Flag created \n## This Flag has been added to case");
    }
}
