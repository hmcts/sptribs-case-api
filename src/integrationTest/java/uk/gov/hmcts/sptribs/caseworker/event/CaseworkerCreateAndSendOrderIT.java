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
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.document.DocAssemblyService;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.model.OrderIssuingType.CREATE_AND_SEND_NEW_ORDER;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_AND_SEND_ORDER;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC.APPLICANT_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.EMAIL;
import static uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.SchemeCic.Year2012;
import static uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC.SUBJECT;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_HYPHENATED;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class CaseworkerCreateAndSendOrderIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private IdamService idamService;

    @MockitoBean
    private DocAssemblyService docAssemblyService;

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

    private static final String CASEWORKER_CREATE_AND_SEND_ORDER_ABOUT_TO_START_RESPONSE =
            "classpath:responses/caseworker-create-and-send-order-about-to-start-response.json";

    private static final String CASEWORKER_CREATE_AND_SEND_ORDER_ABOUT_TO_SUBMIT_RESPONSE =
            "classpath:responses/caseworker-create-and-send-order-about-to-submit-response.json";

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
    void shouldSetOrderIssueTypesAndTemplatesInAboutToStartCallback() throws Exception {
        final CaseData caseData = caseData();

        String response = mockMvc.perform(post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_CREATE_AND_SEND_ORDER)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_CREATE_AND_SEND_ORDER_ABOUT_TO_START_RESPONSE)));
    }

    @Test
    void shouldUpdateOrdersWithNewAnonymousOrderAboutToSubmit() throws Exception {
        DateModel dateModel = DateModel.builder()
            .dueDate(LocalDate.of(2026, 1, 2))
            .information("due date for test")
            .build();

        DraftOrderContentCIC draftOrderContentCIC = DraftOrderContentCIC.builder()
            .orderTemplate(OrderTemplate.CIC3_RULE_27)
            .mainContent("Main order content sample")
            .orderSignature("Supreme Judge Fudge")
            .build();

        Document document = Document.builder()
            .categoryId("TD")
            .filename("Order--[AAC]--09-05-2024 09:04:04.pdf")
            .binaryUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/6bcdb209-ba65-4f31-a12a-5abbdf250fb6/binary")
            .url("http://dm-store-aat.service.core-compute-aat.internal/documents/6bcdb209-ba65-4f31-a12a-5abbdf250fb6")
            .build();

        final CaseData caseData = CaseData.builder()
            .draftOrderContentCIC(draftOrderContentCIC)
            .cicCase(CicCase.builder()
                .orderIssuingType(CREATE_AND_SEND_NEW_ORDER)
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymisedAppellantName("AAC")
                .orderTemplateIssued(document)
                .notifyPartySubject(Set.of(SUBJECT))
                .notifyPartyRespondent(Set.of(RESPONDENT))
                .notifyPartyRepresentative(Set.of(REPRESENTATIVE))
                .notifyPartyApplicant(Set.of(APPLICANT_CIC))
                .orderDueDates(List.of(ListValue.<DateModel>builder().value(dateModel).build()))
                .fullName("Test Name")
                .schemeCic(Year2012)
                .orderTemplateIssued(document)
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
                        CASEWORKER_CREATE_AND_SEND_ORDER)))
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(response)
                .when(IGNORING_EXTRA_FIELDS)
                .isEqualTo(json(expectedResponse(CASEWORKER_CREATE_AND_SEND_ORDER_ABOUT_TO_SUBMIT_RESPONSE)));
    }

    @Test
    void shouldSuccessfullyDispatchEmailsOnSubmitted() throws Exception {
        final CaseData caseData = CaseData.builder()
            .hyphenatedCaseRef(TEST_CASE_ID_HYPHENATED)
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
                .build())
            .build();

        String response = mockMvc.perform(post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_CREATE_AND_SEND_ORDER)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Order sent \n## A notification has been sent to: Subject, Respondent, Representative, Applicant");

        verify(notificationServiceCIC, times(4)).sendEmail(any(), eq(TEST_CASE_ID_HYPHENATED));
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
                    CASEWORKER_CREATE_AND_SEND_ORDER)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
                .inPath(CONFIRMATION_HEADER)
                .isString()
                .contains("# Send order notification failed \n## Please resend the order");

        verifyNoInteractions(notificationServiceCIC);
    }
}
