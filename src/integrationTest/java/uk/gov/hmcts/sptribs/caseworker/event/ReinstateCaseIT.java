package uk.gov.hmcts.sptribs.caseworker.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.IntegrationTestBase;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.manager.CaseDataITManager;
import uk.gov.hmcts.sptribs.manager.CaseDocumentITManager;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.model.ReinstateReason.REQUEST_TO_SET_ASIDE_A_TRIBUNAL_DECISION_FOLLOWING_AN_ORAL_HEARING;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_REINSTATE_CASE;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC.APPLICANT_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.EMAIL;
import static uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC.SUBJECT;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_HYPHENATED;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentUploadList;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class ReinstateCaseIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaseDataITManager caseDataITManager;

    @Autowired
    private CaseDocumentITManager caseDocumentITManager;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

    private static final String CASEWORKER_REINSTATE_CASE_ABOUT_TO_START_RESPONSE =
        "classpath:responses/caseworker-reinstate-case-about-to-start-response.json";
    private static final String CASEWORKER_REINSTATE_CASE_ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/caseworker-reinstate-case-about-to-submit-response.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    void shouldUpdateReinstateDocumentsUploadOnAboutToStart() throws Exception {
        final CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .reinstateDocuments(getCaseworkerCICDocumentList("file.pdf"))
                .build()
            )
            .build();

        String response = mockMvc.perform(post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_REINSTATE_CASE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_REINSTATE_CASE_ABOUT_TO_START_RESPONSE)));
    }

    @Test
    void shouldClearReinstateDocumentsUploadAndSetReinstateDocumentsOnAboutToSubmit() throws Exception {
        final CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .reinstateDocumentsUpload(getCaseworkerCICDocumentUploadList("test.pdf"))
                .build()
            )
            .build();

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_REINSTATE_CASE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_REINSTATE_CASE_ABOUT_TO_SUBMIT_RESPONSE)));
    }

    @Test
    void shouldSaveAddedReinstateDocumentToCaseDocumentsTableOnAboutToSubmit() throws Exception {
        caseDataITManager.addCaseData(TEST_CASE_ID, "test", "{}");

        String documentUrl = "test.url/documents/" + UUID.randomUUID();
        String documentBinaryUrl = documentUrl + "/binary";

        CaseworkerCICDocumentUpload uploadedDocument = CaseworkerCICDocumentUpload.builder()
            .documentLink(Document.builder()
                .url(documentUrl)
                .binaryUrl(documentBinaryUrl)
                .filename("reinstate-added-doc.pdf")
                .build())
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentEmailContent("some email content")
            .build();

        final CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .reinstateDocumentsUpload(List.of(new ListValue<>("new-doc", uploadedDocument)))
                .build())
            .build();

        final CaseData caseDataBefore = CaseData.builder()
            .cicCase(CicCase.builder().reinstateDocuments(List.of()).build())
            .build();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, caseDataBefore, CASEWORKER_REINSTATE_CASE)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        assertThat(caseDocumentITManager.getCount(documentBinaryUrl)).isEqualTo(1);
        assertThat(caseDocumentITManager.findByBinaryUrl(documentBinaryUrl).getDocumentTypeName()).isEqualTo("LINKED_DOCS");
    }

    @Test
    void shouldUpdateRetainedDocumentCategoryAndRemoveDeletedDocumentOnAboutToSubmit() throws Exception {
        caseDataITManager.addCaseData(TEST_CASE_ID, "test", "{}");

        String retainedDocumentUrl = "test.url/documents/" + UUID.randomUUID();
        String retainedBinaryUrl = retainedDocumentUrl + "/binary";
        String removedDocumentUrl = "test.url/documents/" + UUID.randomUUID();
        String removedBinaryUrl = removedDocumentUrl + "/binary";

        caseDocumentITManager.addCaseDocument(TEST_CASE_ID, retainedBinaryUrl, 2L, OffsetDateTime.now());
        caseDocumentITManager.addCaseDocument(TEST_CASE_ID, removedBinaryUrl, 2L, OffsetDateTime.now());

        final CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .reinstateDocumentsUpload(List.of(
                    new ListValue<>("retained-doc", CaseworkerCICDocumentUpload.builder()
                        .documentLink(Document.builder()
                            .url(retainedDocumentUrl)
                            .binaryUrl(retainedBinaryUrl)
                            .filename("retained-doc.pdf")
                            .build())
                        .documentCategory(DocumentType.HOSPITAL_RECORDS)
                        .documentEmailContent("updated category")
                        .build())
                ))
                .build())
            .build();

        final CaseData caseDataBefore = CaseData.builder()
            .cicCase(CicCase.builder()
                .reinstateDocuments(List.of(
                    ListValue.<CaseworkerCICDocument>builder()
                        .id("retained-doc")
                        .value(CaseworkerCICDocument.builder()
                            .documentLink(Document.builder()
                                .url(retainedDocumentUrl)
                                .binaryUrl(retainedBinaryUrl)
                                .filename("retained-doc.pdf")
                                .build())
                            .documentCategory(DocumentType.LINKED_DOCS)
                            .documentEmailContent("old category")
                            .build())
                        .build(),
                    ListValue.<CaseworkerCICDocument>builder()
                        .id("removed-doc")
                        .value(CaseworkerCICDocument.builder()
                            .documentLink(Document.builder()
                                .url(removedDocumentUrl)
                                .binaryUrl(removedBinaryUrl)
                                .filename("removed-doc.pdf")
                                .build())
                            .documentCategory(DocumentType.LINKED_DOCS)
                            .documentEmailContent("removed")
                            .build())
                        .build()
                ))
                .build())
            .build();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, caseDataBefore, CASEWORKER_REINSTATE_CASE)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        assertThat(caseDocumentITManager.getCount(retainedBinaryUrl)).isEqualTo(1);
        assertThat(caseDocumentITManager.findByBinaryUrl(retainedBinaryUrl).getDocumentTypeName()).isEqualTo("HOSPITAL_RECORDS");
        assertThat(caseDocumentITManager.getCount(removedBinaryUrl)).isZero();
    }

    @Test
    void shouldReturnConfirmationMessageIfNotificationsDispatchedOnSubmitted() throws Exception {
        final CaseData caseData = caseData();
        caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
        caseData.setCicCase(
            CicCase.builder()
                .notifyPartySubject(Set.of(SUBJECT))
                .notifyPartyRespondent(Set.of(RESPONDENT))
                .notifyPartyRepresentative(Set.of(REPRESENTATIVE))
                .notifyPartyApplicant(Set.of(APPLICANT_CIC))
                .contactPreferenceType(EMAIL)
                .representativeContactDetailsPreference(EMAIL)
                .applicantContactDetailsPreference(EMAIL)
                .fullName("Test Name")
                .email("test@test.com")
                .representativeFullName("Rep Name")
                .representativeEmailAddress("representative@test.com")
                .respondentName("Respondent Name")
                .respondentEmail("respondent@test.com")
                .applicantFullName("Applicant Name")
                .applicantEmailAddress("applicant@test.com")
                .reinstateReason(REQUEST_TO_SET_ASIDE_A_TRIBUNAL_DECISION_FOLLOWING_AN_ORAL_HEARING)
                .build()
        );

        String response = mockMvc.perform(post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_REINSTATE_CASE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("""
                        # Case reinstated\s
                        ##  The case record will now be reopened.\s
                        ## A notification has been sent to: Subject, Respondent, Representative, Applicant\s"""
            );

        verify(notificationServiceCIC, times(4)).sendEmail(any(), eq(TEST_CASE_ID_HYPHENATED), eq(null));
        verifyNoMoreInteractions(notificationServiceCIC);
    }

    @Test
    void shouldReturnErrorMessageIfNotificationsFailOnSubmitted() throws Exception {
        final CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .notifyPartySubject(Set.of(SUBJECT))
                .notifyPartyRespondent(Set.of(RESPONDENT))
                .notifyPartyRepresentative(Set.of(REPRESENTATIVE))
                .notifyPartyApplicant(Set.of(APPLICANT_CIC))
                .build()
            )
            .build();

        String response = mockMvc.perform(post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_REINSTATE_CASE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Case Reinstate notification failed \n## Please resend the notification");

        verifyNoInteractions(notificationServiceCIC);
    }
}
