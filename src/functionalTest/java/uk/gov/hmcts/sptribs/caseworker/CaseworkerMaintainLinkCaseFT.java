package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.notification.persistence.CorrespondenceEntity;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_MAINTAIN_LINK_CASE;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;

@SpringBootTest
public class CaseworkerMaintainLinkCaseFT extends FunctionalTestSuite {

    private static final String REQUEST_SUBMITTED_HAPPY_PATH =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-maintain-link-case-submitted.json";
    private static final String REQUEST_SUBMITTED_UNHAPPY_PATH =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-maintain-link-case-unhappy-submitted.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldSuccessfullySendNotificationWhenSubmittedEventCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_SUBMITTED_HAPPY_PATH);
        final Response response = triggerCallback(caseData, CASEWORKER_MAINTAIN_LINK_CASE, SUBMITTED_URL, false);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .isEqualTo("# Case Link updated \n");

        long testCaseRef = Long.parseLong(caseData.get("hyphenatedCaseRef").toString().replace("-", ""));

        List<CorrespondenceEntity> correspondenceEntities = caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef);
        assertThat(correspondenceEntities).hasSize(1);

        CorrespondenceEntity firstCorrespondenceEntity = correspondenceEntities.getFirst();

        assertThat(firstCorrespondenceEntity.getId()).isNotNull();
        assertThat(firstCorrespondenceEntity.getCaseReferenceNumber()).isEqualTo(Long.parseLong(caseData.get("hyphenatedCaseRef")
            .toString().replace("-", "")));
        assertThat(firstCorrespondenceEntity.getEventType()).isEqualTo("CASE_UNLINKED_EMAIL");
        assertThat(firstCorrespondenceEntity.getSentOn()).isNotNull();
        assertThat(firstCorrespondenceEntity.getSentFrom()).isNotNull();
        assertThat(firstCorrespondenceEntity.getSentTo()).isNotNull();
        assertThat(firstCorrespondenceEntity.getCorrespondenceType()).isEqualTo("Email");
        assertThat(firstCorrespondenceEntity.getDocumentUrl()).isNotNull();
        assertThat(firstCorrespondenceEntity.getDocumentFilename()).isNotNull();
        assertThat(firstCorrespondenceEntity.getDocumentBinaryUrl()).isNotNull();
    }

    @Test
    public void shouldUnsuccessfullySendNotificationWhenBadSubmittedEventCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_SUBMITTED_UNHAPPY_PATH);
        final Response response = triggerCallback(caseData, CASEWORKER_MAINTAIN_LINK_CASE, SUBMITTED_URL, false);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .isEqualTo("# Case Link update notification failed \n## Please resend the notification \n");
    }
}
