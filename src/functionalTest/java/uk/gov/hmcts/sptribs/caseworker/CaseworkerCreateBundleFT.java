package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CREATE_BUNDLE;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerCreateBundleFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-create-bundle-about-to-submit.json";
    private static final String RESPONSE =
        "classpath:responses/response-caseworker-create-bundle-about-to-submit.json";
    private static final String CALLBACK_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-create-bundle-about-to-start.json";
    private static final String SUBMITTED_FAILURE_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-create-bundle-about-to-start-failure.json";
    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldCreateBundleInAboutToSubmitCallback() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);
        final Response response = triggerCallback(caseData, CREATE_BUNDLE, ABOUT_TO_SUBMIT_URL, false);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldNotSetTimestampForOldBundlesWithoutTimestampEntryWhenCreatingNewBundle() throws Exception {

        String existingOldBundleUUID = UUID.randomUUID().toString();

        final Map<String, Object> caseDataBefore = caseData(REQUEST);
        Map<String, Object> existingBundle = new HashMap<>();
        existingBundle.put("id", "1");
        Map<String, Object> bundleValue = new HashMap<>();
        bundleValue.put("id", existingOldBundleUUID);
        existingBundle.put("value", bundleValue);

        List<Map<String, Object>> existingBundles = new ArrayList<>();
        existingBundles.add(existingBundle);
        caseDataBefore.put("caseBundles", existingBundles);

        final Map<String, Object> caseData = caseData(REQUEST);
        caseData.put("caseBundleIdsAndTimestamps", new ArrayList<>());

        final Response response = triggerCallback(caseData, caseDataBefore, CREATE_BUNDLE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .inPath("$.errors")
            .isAbsent();
    }

    @Test
    public void shouldHandleNullBundleIdsAndTimestampsGracefully() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);
        caseData.remove("caseBundleIdsAndTimestamps");

        final Response response = triggerCallback(caseData, CREATE_BUNDLE, ABOUT_TO_SUBMIT_URL, false);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .inPath("$.errors")
            .isAbsent();
    }

    @Test
    public void shouldBeSuccessfulWhenSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(CALLBACK_REQUEST);

        final Response response = triggerCallback(caseData, CREATE_BUNDLE, SUBMITTED_URL, false);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Bundle created. \n## A notification has been sent to");
    }

    @Test
    public void shouldReturnFailureMessageWhenEmailCouldNotSendWhenSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(SUBMITTED_FAILURE_REQUEST);

        final Response response = triggerCallback(caseData, CREATE_BUNDLE, SUBMITTED_URL, false);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .isEqualTo("""
                # Bundle creation notification failed\s
                ## A notification could not be sent to: Respondent\s
                ## Please resend the notification.""");
    }
    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotIncludeAudioVideoDocumentsInCaseBundlesDuringAboutToSubmit() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);

        Map<String, Object> mp3Doc = new HashMap<>();
        mp3Doc.put("id", UUID.randomUUID().toString());
        Map<String, Object> mp3Value = new HashMap<>();
        mp3Value.put("documentCategory", "Linked docs");
        mp3Value.put("documentEmailContent", "Audio evidence");
        Map<String, Object> mp3Link = new HashMap<>();
        mp3Link.put("document_url", "http://dm-store/documents/media-a");
        mp3Link.put("document_binary_url", "http://dm-store/documents/media-a/binary");
        mp3Link.put("document_filename", "media-a.mp3");
        mp3Value.put("documentLink", mp3Link);
        mp3Value.put("date", "2026-01-10");
        mp3Doc.put("value", mp3Value);

        Map<String, Object> mp4Doc = new HashMap<>();
        mp4Doc.put("id", UUID.randomUUID().toString());
        Map<String, Object> mp4Value = new HashMap<>();
        mp4Value.put("documentCategory", "PoliceEvidence");
        mp4Value.put("documentEmailContent", "Video evidence");
        Map<String, Object> mp4Link = new HashMap<>();
        mp4Link.put("document_url", "http://dm-store/documents/media-b");
        mp4Link.put("document_binary_url", "http://dm-store/documents/media-b/binary");
        mp4Link.put("document_filename", "media-b.mp4");
        mp4Value.put("documentLink", mp4Link);
        mp4Value.put("date", "2026-01-12");
        mp4Doc.put("value", mp4Value);

        List<Map<String, Object>> applicantDocs = (List<Map<String, Object>>) caseData.get("cicCaseApplicantDocumentsUploaded");
        applicantDocs.add(mp3Doc);
        applicantDocs.add(mp4Doc);

        final Response response = triggerCallback(caseData, CREATE_BUNDLE, ABOUT_TO_SUBMIT_URL, false);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .inPath("$.errors")
            .isAbsent();
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .inPath("$.data.cicCaseApplicantDocumentsUploaded[*].value.documentLink.document_filename")
            .isArray()
            .contains("media-a.mp3", "media-b.mp4", "pdf.pdf");
    }
}
