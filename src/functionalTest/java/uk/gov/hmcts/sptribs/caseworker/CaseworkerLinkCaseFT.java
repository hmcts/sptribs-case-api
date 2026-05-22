package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_LINK_CASE;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;

@SpringBootTest
public class CaseworkerLinkCaseFT extends FunctionalTestSuite {

    private static final String REQUEST_SUBMITTED_HAPPY_PATH =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-link-case-submitted.json";
    private static final String REQUEST_SUBMITTED_UNHAPPY_PATH =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-link-case-bad-submitted.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldGiveInvalidResponseWhenBadSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_SUBMITTED_UNHAPPY_PATH);
        final Response response = triggerCallback(caseData, CASEWORKER_LINK_CASE, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .isEqualTo("# Case Link notification failed \n## Please resend the notification");
    }

    @Test
    public void shouldGiveValidResponseWhenSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_SUBMITTED_HAPPY_PATH);
        final Response response = triggerCallback(caseData, CASEWORKER_LINK_CASE, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .isEqualTo("# Case Link created \n");

        long testCaseRef = Long.parseLong(caseData.get("hyphenatedCaseRef").toString().replace("-", ""));

        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef)).hasSize(1);
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getId())
            .isNotNull();
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getCaseReferenceNumber())
            .isEqualTo(Long.parseLong(caseData.get("hyphenatedCaseRef").toString().replace("-", "")));
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getEventType())
            .isEqualTo("CASE_LINKED_EMAIL");
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getSentOn())
            .isNotNull();
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getSentFrom())
            .isNotNull();
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getSentTo())
            .isNotNull();
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getCorrespondenceType())
            .isEqualTo("Email");
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getDocumentUrl())
            .isNotNull();
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getDocumentFilename())
            .isNotNull();
        assertThat(caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef).getFirst().getDocumentBinaryUrl())
            .isNotNull();
    }
}
