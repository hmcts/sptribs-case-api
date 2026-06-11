package uk.gov.hmcts.sptribs.citizen.event;

import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.notification.persistence.CorrespondenceEntity;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CicSubmitCaseEventFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-submit-case-about-to-submit.json";
    private static final String REQUEST_SUBMITTED = "classpath:request/casedata/ccd-callback-casedata-submit-case-submitted.json";
    private static final String REQUEST_MISSING_CASE_NUMBER =
        "classpath:request/casedata/ccd-callback-casedata-submit-case-missing-case-number.json";
    private static final String REQUEST_MISSING_PARTIES =
        "classpath:request/casedata/ccd-callback-casedata-submit-case-missing-parties.json";

    private static final String RESPONSE = "classpath:responses/response-caseworker-submit-case-about-to-submit.json";

    private static final String CITIZEN_SUBMIT_CASE_EVENT_ID = "citizen-cic-submit-dss-application";
    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldSuccessfullySubmitCaseWhenAboutToSubmitCallbackIsTriggered() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);
        final Response response = triggerCallback(caseData, CITIZEN_SUBMIT_CASE_EVENT_ID, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));

        long testCaseRef = Long.parseLong(caseData.get("hyphenatedCaseRef").toString().replace("-", ""));

        List<DocumentEntity> documentEntities = caseDocumentsFTDataManager.getDocumentEntities(testCaseRef);
        assertThat(documentEntities).hasSize(3);

        DocumentEntity firstDocumentEntity = documentEntities.getFirst();

        assertThat(firstDocumentEntity.getId()).isNotNull();
        assertThat(firstDocumentEntity.getCaseReferenceNumber()).isEqualTo(Long.parseLong(caseData.get("hyphenatedCaseRef")
            .toString().replace("-", "")));
        assertThat(firstDocumentEntity.getCategoryId()).isNotNull();
        assertThat(firstDocumentEntity.getSavedAt()).isNotNull();
        assertThat(firstDocumentEntity.getUpdatedAt()).isNotNull();
        assertThat(firstDocumentEntity.isDraft()).isFalse();
        assertThat(firstDocumentEntity.isSentToApplicantViaContactParties()).isFalse();
        assertThat(firstDocumentEntity.getDocumentUrl()).isNotNull();
        assertThat(firstDocumentEntity.getDocumentFilename()).isNotNull();
        assertThat(firstDocumentEntity.getDocumentBinaryUrl()).isNotNull();
    }

    @Test
    public void shouldReceiveNotificationWhenSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_SUBMITTED);
        final Response response = triggerCallback(caseData, CITIZEN_SUBMIT_CASE_EVENT_ID, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Application Received \\n## A notification has been sent to: Subject, Representative");

        long testCaseRef = Long.parseLong(caseData.get("hyphenatedCaseRef").toString().replace("-", ""));

        List<CorrespondenceEntity> correspondenceEntities = caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef);
        assertThat(correspondenceEntities).hasSize(2);

        CorrespondenceEntity firstCorrespondenceEntity = correspondenceEntities.getFirst();

        assertThat(firstCorrespondenceEntity.getId()).isNotNull();
        assertThat(firstCorrespondenceEntity.getCaseReferenceNumber()).isEqualTo(Long.parseLong(caseData.get("hyphenatedCaseRef")
            .toString().replace("-", "")));
        assertThat(firstCorrespondenceEntity.getEventType()).isEqualTo("APPLICATION_RECEIVED");
        assertThat(firstCorrespondenceEntity.getSentOn()).isNotNull();
        assertThat(firstCorrespondenceEntity.getSentFrom()).isNotNull();
        assertThat(firstCorrespondenceEntity.getSentTo()).isNotNull();
        assertThat(firstCorrespondenceEntity.getCorrespondenceType()).isEqualTo("Email");
        assertThat(firstCorrespondenceEntity.getDocumentUrl()).isNotNull();
        assertThat(firstCorrespondenceEntity.getDocumentFilename()).isNotNull();
        assertThat(firstCorrespondenceEntity.getDocumentBinaryUrl()).isNotNull();
    }

    @Test
    public void shouldReceiveWelshNotificationWhenSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_SUBMITTED);
        caseData.put("dssCaseDataLanguagePreference", WELSH);
        final Response response = triggerCallback(caseData, CITIZEN_SUBMIT_CASE_EVENT_ID, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Application Received \\n## A notification has been sent to: Subject, Representative");

        long testCaseRef = Long.parseLong(caseData.get("hyphenatedCaseRef").toString().replace("-", ""));

        List<CorrespondenceEntity> correspondenceEntities = caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef);
        assertThat(correspondenceEntities).hasSize(2);

        CorrespondenceEntity firstCorrespondenceEntity = correspondenceEntities.getFirst();

        assertThat(firstCorrespondenceEntity.getId()).isNotNull();
        assertThat(firstCorrespondenceEntity.getCaseReferenceNumber()).isEqualTo(Long.parseLong(caseData.get("hyphenatedCaseRef")
            .toString().replace("-", "")));
        assertThat(firstCorrespondenceEntity.getEventType()).isEqualTo("APPLICATION_RECEIVED_CY");
        assertThat(firstCorrespondenceEntity.getSentOn()).isNotNull();
        assertThat(firstCorrespondenceEntity.getSentFrom()).isNotNull();
        assertThat(firstCorrespondenceEntity.getSentTo()).isNotNull();
        assertThat(firstCorrespondenceEntity.getCorrespondenceType()).isEqualTo("Email");
        assertThat(firstCorrespondenceEntity.getDocumentUrl()).isNotNull();
        assertThat(firstCorrespondenceEntity.getDocumentFilename()).isNotNull();
        assertThat(firstCorrespondenceEntity.getDocumentBinaryUrl()).isNotNull();
    }

    @Disabled("Skipped to unblock WA - New case needs to be created before updating supplementary data")
    @Test
    public void shouldNotSendApplicationReceivedNotificationWhenNotifyPartiesNotFound() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_MISSING_PARTIES);
        final Response response = triggerCallback(caseData, CITIZEN_SUBMIT_CASE_EVENT_ID, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Application Received %n##");
    }

    @Test
    public void shouldRaiseErrorWhenNotificationExceptionFound() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST_MISSING_CASE_NUMBER);
        final Response response =
            triggerCallbackWithoutPersistedCase(caseData, CITIZEN_SUBMIT_CASE_EVENT_ID, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Application Received notification failed %n## Please resend the notification");
    }
}
