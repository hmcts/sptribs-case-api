package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.MINOR_FATAL_SUBJECT_ERROR_MESSAGE;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.SELECT_AT_LEAST_ONE_CONTACT_PARTY;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.RESPONDENT_CONTACT_PARTIES;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ERRORS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
@Slf4j
public class RespondentContactPartiesFT extends FunctionalTestSuite {

    public static final String PARTIES_TO_CONTACT_MID_EVENT_URL
        = "/callbacks/mid-event?page=partiesToContact";
    public static final String CONTACT_PARTIES_SELECT_DOCUMENT_MID_EVENT_URL
        = "/callbacks/mid-event?page=contactPartiesSelectDocument";

    private static final String PARTIES_TO_CONTACT_MID_EVENT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-parties-to-contact-mid-event.json";
    private static final String PARTIES_TO_CONTACT_NO_CONTACT_SELECTED_MID_EVENT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-parties-to-contact-no-contact-selected-mid-event.json";
    private static final String PARTIES_TO_CONTACT_MINOR_FATAL_CASE_MID_EVENT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-parties-to-contact-minor-fatal-mid-event.json";

    private static final String CONTACT_PARTIES_SELECT_DOCUMENT_MID_EVENT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-contact-parties-select-document-mid-event.json";
    private static final String CONTACT_PARTIES_SELECT_DOCUMENT_TOO_MANY_DOCUMENTS_MID_EVENT_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-contact-parties-select-document-too-many-mid-event.json";

    private static final String ABOUT_TO_START_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-respondent-contact-parties-about-to-start.json";
    private static final String ABOUT_TO_START_RESPONSE =
        "classpath:responses/response-caseworker-respondent-contact-parties-about-to-start.json";

    private static final String SUBMITTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-respondent-contact-parties-submitted.json";
    private static final String UNHAPPY_PATH_SUBMITTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-respondent-contact-parties-submitted-error.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldNotReturnAnyErrorsInMidEventCallback() throws Exception {
        final Map<String, Object> caseData = caseData(PARTIES_TO_CONTACT_MID_EVENT_REQUEST);

        final Response response = triggerCallback(
            caseData,
            RESPONDENT_CONTACT_PARTIES,
            PARTIES_TO_CONTACT_MID_EVENT_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(ERRORS)
            .isArray()
            .isEmpty();
    }

    @Test
    public void shouldReturnErrorWhenNoContactPartySelectedInMidEventCallback() throws Exception {
        final Map<String, Object> caseData = caseData(PARTIES_TO_CONTACT_NO_CONTACT_SELECTED_MID_EVENT_REQUEST);

        final Response response = triggerCallback(
            caseData,
            RESPONDENT_CONTACT_PARTIES,
            PARTIES_TO_CONTACT_MID_EVENT_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(ERRORS)
            .isArray()
            .contains(SELECT_AT_LEAST_ONE_CONTACT_PARTY);
    }

    @Test
    public void shouldReturnErrorWhenSubjectSelectedAndCaseIsMinorOrFatalInMidEventCallback() throws Exception {
        final Map<String, Object> caseData = caseData(PARTIES_TO_CONTACT_MINOR_FATAL_CASE_MID_EVENT_REQUEST);

        final Response response = triggerCallback(
            caseData,
            RESPONDENT_CONTACT_PARTIES,
            PARTIES_TO_CONTACT_MID_EVENT_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(ERRORS)
            .isArray()
            .contains(MINOR_FATAL_SUBJECT_ERROR_MESSAGE);
    }

    @Test
    public void shouldNotReturnAnyErrorsInSelectDocumentMidEventCallback() throws Exception {
        final Map<String, Object> caseData = caseData(CONTACT_PARTIES_SELECT_DOCUMENT_MID_EVENT_REQUEST);

        String documentId = "a17fa26b-6e91-48ba-841e-8ab75c772463";

        UploadResponse uploadResponse = uploadTestDocumentIfMissing(documentId, DRAFT_ORDER_FILE);
        String resolvedDocumentId = resolveDocumentId(documentId, uploadResponse);
        updateDocumentSelectionWithDocumentId(caseData, resolvedDocumentId);

        final Response response = triggerCallback(
            caseData,
            RESPONDENT_CONTACT_PARTIES,
            CONTACT_PARTIES_SELECT_DOCUMENT_MID_EVENT_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(ERRORS)
            .isArray()
            .isEmpty();
    }

    @Test
    public void shouldReturnAnErrorIfMoreThan10DocumentsUploadedInSelectDocumentMidEventCallback() throws Exception {
        final Map<String, Object> caseData = caseData(CONTACT_PARTIES_SELECT_DOCUMENT_TOO_MANY_DOCUMENTS_MID_EVENT_REQUEST);

        String documentId = "a17fa26b-6e91-48ba-841e-8ab75c772463";

        UploadResponse docUploadResponse = uploadTestDocumentIfMissing(documentId, DRAFT_ORDER_FILE);
        String resolvedDocumentId = resolveDocumentId(documentId, docUploadResponse);
        updateDocumentSelectionWithDocumentId(caseData, resolvedDocumentId);

        final Response response = triggerCallback(
            caseData,
            RESPONDENT_CONTACT_PARTIES,
            CONTACT_PARTIES_SELECT_DOCUMENT_MID_EVENT_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(ERRORS)
            .isArray()
            .contains("Select up to 10 documents");
    }

    @Test
    public void shouldPopulateContactPartiesDocumentInAboutToStartCallback() throws Exception {
        final Map<String, Object> caseData = caseData(ABOUT_TO_START_REQUEST);

        final Response response = triggerCallback(
            caseData,
            RESPONDENT_CONTACT_PARTIES,
            ABOUT_TO_START_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(ABOUT_TO_START_RESPONSE)));
    }

    @Test
    public void shouldSuccessfullySendEmailNotificationsInSubmittedEvent() throws Exception {
        final Map<String, Object> caseData = caseData(SUBMITTED_REQUEST);

        final Response response = triggerCallback(
            caseData,
            RESPONDENT_CONTACT_PARTIES,
            SUBMITTED_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Message sent \\n## A notification has been sent to: Subject");
    }

    @Test
    public void shouldReturnErrorWhenEmailNotificationsNotSentInSubmittedEvent() throws Exception {
        final Map<String, Object> caseData = caseData(UNHAPPY_PATH_SUBMITTED_REQUEST);

        final Response response = triggerCallback(
            caseData,
            RESPONDENT_CONTACT_PARTIES,
            SUBMITTED_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Contact Parties notification failed \\n## Please resend the notification");
    }

    private String resolveDocumentId(String fallbackDocumentId, UploadResponse uploadResponse) {
        if (uploadResponse == null
            || uploadResponse.getDocuments() == null
            || uploadResponse.getDocuments().isEmpty()) {
            return fallbackDocumentId;
        }

        Document uploadedDocument = uploadResponse.getDocuments().getFirst();
        if (uploadedDocument == null || uploadedDocument.links == null) {
            return fallbackDocumentId;
        }

        String documentHref = Optional.ofNullable(uploadedDocument.links)
            .map(links -> {
                if (links.self != null && StringUtils.isNotBlank(links.self.href)) {
                    return links.self.href;
                }
                if (links.binary != null && StringUtils.isNotBlank(links.binary.href)) {
                    return links.binary.href;
                }
                return null;
            })
            .orElse(null);

        if (StringUtils.isBlank(documentHref)) {
            return fallbackDocumentId;
        }

        if (Strings.CS.endsWith(documentHref, "/binary")) {
            documentHref = StringUtils.substringBeforeLast(documentHref, "/");
        }

        String extractedDocumentId = StringUtils.substringAfterLast(documentHref, "/");
        return isValidUuid(extractedDocumentId) ? extractedDocumentId : fallbackDocumentId;
    }

    private boolean isValidUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private void updateDocumentSelectionWithDocumentId(Map<String, Object> caseData, String documentId) {
        if (caseData == null || StringUtils.isBlank(documentId)) {
            return;
        }

        Object docListObject = caseData.get("contactPartiesDocumentsDocumentList");
        if (!(docListObject instanceof Map<?, ?> documentList)) {
            return;
        }

        replaceDocumentId((List<Map<String, Object>>) documentList.get("value"), documentId);
        replaceDocumentId((List<Map<String, Object>>) documentList.get("list_items"), documentId);
    }

    private void replaceDocumentId(List<Map<String, Object>> elements, String documentId) {
        if (elements == null) {
            return;
        }

        for (Map<String, Object> element : elements) {
            Object labelObj = element.get("label");
            if (labelObj instanceof String label) {
                String updatedLabel = label
                    .replace("/null/", "/" + documentId + "/")
                    .replace("/null)", "/" + documentId + ")");
                element.put("label", updatedLabel);
            }
        }
    }
}
