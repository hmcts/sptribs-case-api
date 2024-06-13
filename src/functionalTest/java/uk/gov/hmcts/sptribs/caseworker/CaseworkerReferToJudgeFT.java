package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.time.LocalDate;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_REFER_TO_JUDGE;

@SpringBootTest
public class CaseworkerReferToJudgeFT extends FunctionalTestSuite {

    private static final String CALLBACK_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-refer-to-judge-callback-request.json";

    private static final String REFERRAL_DATE = "$.data.referToJudgeReferralDate";
    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldGivePositiveResponseWhenValidAboutToStartCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_REFER_TO_JUDGE, ABOUT_TO_START_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldInitialiseDateReferredWhenValidAboutToSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_REFER_TO_JUDGE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(REFERRAL_DATE)
            .isString()
            .contains(LocalDate.now().toString());
    }

    @Test
    public void shouldGivePositiveResponseWhenValidSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_REFER_TO_JUDGE, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Referral completed");
    }
}
