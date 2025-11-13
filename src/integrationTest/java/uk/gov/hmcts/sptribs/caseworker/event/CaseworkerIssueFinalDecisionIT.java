package uk.gov.hmcts.sptribs.caseworker.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.DecisionTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;
import uk.gov.hmcts.sptribs.testutil.TestConstants;

import java.util.List;
import java.util.Set;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC.APPLICANT_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.EMAIL;
import static uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC.SUBJECT;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.FINAL_DECISION_ANNEX_TEMPLATE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ISSUE_FINAL_DECISION_MID_EVENT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ISSUE_FINAL_DECISION_UPLOAD_MID_EVENT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_ISSUE_FINAL_DECISION;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class CaseworkerIssueFinalDecisionIT {
    private static final String CASEWORKER_ISSUE_FINAL_DECISION_MID_EVENT_RESPONSE =
        "classpath:responses/caseworker-issue-final-decision-mid-event-response.json";
    private static final String CASEWORKER_ISSUE_FINAL_DECISION_UPLOAD_MID_EVENT_RESPONSE =
        "classpath:responses/caseworker-issue-final-decision-upload-mid-event-response.json";
    private static final String CASEWORKER_ISSUE_FINAL_DECISION_ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/caseworker-issue-final-decision-about-to-submit-response.json";
    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private IdamService idamService;

    @MockitoBean
    private CaseDataDocumentService caseDataDocumentService;

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    void shouldClearDecisionSignatureInAboutToStart() throws Exception {
        final CaseData caseData = caseData();

        String response = mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(TestConstants.SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_FINAL_DECISION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath("$.data.decisionSignature")
            .isString()
            .isEqualTo("");
    }

    @Test
    void shouldRenderFinalDecisionDocumentInMidEvent() throws Exception {
        final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
            .decisionTemplate(DecisionTemplate.ELIGIBILITY)
            .build();

        final CaseData caseData = CaseData.builder()
            .caseIssueFinalDecision(caseIssueFinalDecision)
            .build();

        final User user = new User(
            TEST_AUTHORIZATION_TOKEN,
            UserDetails.builder()
                .roles(List.of("caseworker-st_cic"))
                .build()
        );

        Document document = Document.builder()
            .url("url")
            .binaryUrl("binary url")
            .filename("filename")
            .categoryId("category")
            .build();

        when(idamService.retrieveUser(eq(TEST_AUTHORIZATION_TOKEN)))
            .thenReturn(user);

        when(caseDataDocumentService.renderDocument(
            anyMap(),
            any(),
            eq(DecisionTemplate.ELIGIBILITY.getId()),
            eq(LanguagePreference.ENGLISH),
            any(),
            any()
        )).thenReturn(document);

        String response = mockMvc.perform(MockMvcRequestBuilders.post(ISSUE_FINAL_DECISION_MID_EVENT_URL)
                .contentType(APPLICATION_JSON)
                .header(TestConstants.SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_FINAL_DECISION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_ISSUE_FINAL_DECISION_MID_EVENT_RESPONSE)));

        verify(caseDataDocumentService).renderDocument(
            anyMap(),
            any(),
            eq(DecisionTemplate.ELIGIBILITY.getId()),
            eq(LanguagePreference.ENGLISH),
            any(),
            any()
        );
    }

    @Test
    void shouldValidateUploadedDocumentInMidEvent() throws Exception {
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().url("url").binaryUrl("binary").filename("filename.pdf").build())
            .build();

        final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
            .document(document)
            .build();

        final CaseData caseData = CaseData.builder()
            .caseIssueFinalDecision(caseIssueFinalDecision)
            .build();

        mockMvc.perform(MockMvcRequestBuilders.post(ISSUE_FINAL_DECISION_UPLOAD_MID_EVENT_URL)
                .contentType(APPLICATION_JSON)
                .header(TestConstants.SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_FINAL_DECISION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(CASEWORKER_ISSUE_FINAL_DECISION_UPLOAD_MID_EVENT_RESPONSE))
            );
    }

    @Test
    void shouldSetCategoryIdOnDecisionDocumentInAboutToSubmit() throws Exception {
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().url("url").binaryUrl("binary").filename("filename.pdf").build())
            .build();

        final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
            .document(document)
            .build();

        final CaseData caseData = CaseData.builder()
            .caseIssueFinalDecision(caseIssueFinalDecision)
            .build();

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(TestConstants.SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_FINAL_DECISION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(CASEWORKER_ISSUE_FINAL_DECISION_ABOUT_TO_SUBMIT_RESPONSE))
            );
    }

    @Test
    void shouldReturnConfirmationMessageIfNotificationsDispatchedOnSubmitted() throws Exception {
        final CICDocument cicDocument = CICDocument.builder()
            .documentLink(Document.builder().url("url").binaryUrl("binary").filename("filename.pdf").build())
            .build();

        CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
            .document(cicDocument)
            .build();

        final CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
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
                .build()
            )
            .caseIssueFinalDecision(caseIssueFinalDecision)
            .build();

        final Document document = Document.builder()
            .url("url")
            .binaryUrl("binary url")
            .filename("filename")
            .categoryId("category")
            .build();

        when(caseDataDocumentService.renderDocument(
            anyMap(),
            any(),
            eq(FINAL_DECISION_ANNEX_TEMPLATE_ID),
            eq(LanguagePreference.ENGLISH),
            any(),
            any()
        )).thenReturn(document);

        String response = mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(TestConstants.SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_FINAL_DECISION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Final decision notice issued \n"
                + "## A notification has been sent to: Subject, Respondent, Representative, Applicant");

        verify(notificationServiceCIC, times(4)).sendEmail(any());
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
                .build()
            )
            .build();

        String response = mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(TestConstants.SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_FINAL_DECISION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Issue final decision notification failed \n## Please resend the notification");

        verifyNoMoreInteractions(notificationServiceCIC);
    }
}
