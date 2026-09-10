package uk.gov.hmcts.sptribs.caseworker.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.IntegrationTestBase;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.common.service.ContactPartiesService;
import uk.gov.hmcts.sptribs.document.exception.DocumentSelectionException;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.SelectedCaseDocuments;
import uk.gov.hmcts.sptribs.document.service.CaseDocumentReadService;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.model.Party;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CONTACT_PARTIES;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC.APPLICANT_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.EMAIL;
import static uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC.SUBJECT;
import static uk.gov.hmcts.sptribs.testutil.IdamWireMock.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_HYPHENATED;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class CaseworkerContactPartiesIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    @MockitoBean
    private CaseDocumentClientApi caseDocumentClientApi;

    @MockitoBean
    private CaseDocumentReadService caseDocumentReadService;

    @MockitoBean
    private IdamService idamService;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

    @MockitoBean
    private ContactPartiesService contactPartiesService;

    private User systemUser;

    private static final String CASEWORKER_CONTACT_PARTIES_ABOUT_TO_START_RESPONSE =
        "classpath:responses/caseworker-contact-parties-about-to-start-response.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    private static final String NOTIFICATION_RESPONSE_ID_1 = "121";
    private static final String NOTIFICATION_RESPONSE_ID_2 = "122";
    private static final String NOTIFICATION_RESPONSE_ID_3 = "123";
    private static final String NOTIFICATION_RESPONSE_ID_4 = "124";

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @BeforeEach
    void configureMocks() {
        final CICUser cicUser = new CICUser(TEST_AUTHORIZATION_TOKEN,
            UserInfo.builder()
                .roles(List.of("caseworker-st_cic", "caseworker-sptribs-systemupdate"))
                .build()
            );

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(cicUser);

        Document.DocumentLink testDocumentBinaryUrl = new Document.DocumentLink();
        testDocumentBinaryUrl.href = "testDoc.pdf/binary";
        Document.DocumentLink testDocumentUrl = new Document.DocumentLink();
        testDocumentUrl.href = "testDoc.pdf";
        Document.Links testDocumentLinks = new Document.Links();

        testDocumentLinks.binary = testDocumentBinaryUrl;
        testDocumentLinks.self = testDocumentUrl;

        Document testDocument = new Document();
        testDocument.links = testDocumentLinks;

        when(caseDocumentClientApi.getDocument(any(), any(), any()))
            .thenReturn(org.springframework.http.ResponseEntity.ok(testDocument));
        when(caseDocumentReadService.getContactPartyOptions(any(Long.class), any()))
            .thenReturn(DynamicMultiSelectList.builder().listItems(List.of()).value(List.of()).build());
        when(caseDocumentReadService.getSelectedContactPartyDocuments(any(Long.class), any(), any(Integer.class)))
            .thenReturn(SelectedCaseDocuments.builder()
                .documentEntityIds(List.of())
                .documents(List.of())
                .build());
    }

    @Test
    void shouldClearContactPartiesAndPrepareContactPartiesDocumentsOnAboutToStart() throws Exception {
        final CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .applicantDocumentsUploaded(getCaseworkerCICDocumentList())
                .build()
            )
            .contactParties(ContactParties.builder()
                .message("A contact parties message")
                .applicantContactParties(Set.of(APPLICANT_CIC))
                .build()
            ).build();

        DynamicListElement availableDocument = DynamicListElement.builder()
            .code(UUID.randomUUID())
            .label("available-document.pdf")
            .build();
        when(caseDocumentReadService.getContactPartyOptions(any(Long.class), any()))
            .thenReturn(DynamicMultiSelectList.builder()
                .listItems(List.of(availableDocument))
                .value(List.of())
                .build());

        String response = mockMvc.perform(post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_CONTACT_PARTIES)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_CONTACT_PARTIES_ABOUT_TO_START_RESPONSE)));
    }

    @Test
    void shouldReturnConfirmationMessageIfNotificationsDispatchedOnSubmitted() throws Exception {
        final ContactPartiesDocuments contactPartiesDocuments = new ContactPartiesDocuments();
        List<DynamicListElement> elements = new ArrayList<>();
        UUID testDocumentID = UUID.randomUUID();
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("[pdf.pdf A - Application Form](http://manage-case.demo.platform.hmcts.net/documents/" + testDocumentID + "/binary)")
            .code(testDocumentID)
            .build();
        elements.add(listItem);
        contactPartiesDocuments.setDocumentList(DynamicMultiSelectList
            .builder()
            .value(elements)
            .listItems(elements)
            .build());
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
                .build()
        );
        caseData.setContactPartiesDocuments(contactPartiesDocuments);

        CaseworkerCICDocument selectedDocument = CaseworkerCICDocument.builder()
            .documentLink(uk.gov.hmcts.ccd.sdk.type.Document.builder()
                .url("http://document-management/documents/" + testDocumentID)
                .binaryUrl("http://document-management/documents/" + testDocumentID + "/binary")
                .filename("pdf.pdf")
                .build())
            .build();
        when(caseDocumentReadService.getSelectedContactPartyDocuments(any(Long.class), eq(
            contactPartiesDocuments.getDocumentList()), any(Integer.class)))
            .thenReturn(SelectedCaseDocuments.builder()
                .documentEntityIds(List.of(42L))
                .documents(List.of(selectedDocument))
                .build());

        NotificationResponse notificationResponse1 = NotificationResponse.builder().id(NOTIFICATION_RESPONSE_ID_1).build();
        NotificationResponse notificationResponse2 = NotificationResponse.builder().id(NOTIFICATION_RESPONSE_ID_2).build();
        NotificationResponse notificationResponse3 = NotificationResponse.builder().id(NOTIFICATION_RESPONSE_ID_3).build();
        NotificationResponse notificationResponse4 = NotificationResponse.builder().id(NOTIFICATION_RESPONSE_ID_4).build();

        when(notificationServiceCIC.sendEmail(any(), anyList(), eq(TEST_CASE_ID_HYPHENATED), eq(Party.SUBJECT)))
            .thenReturn(notificationResponse1);
        when(notificationServiceCIC.sendEmail(any(), anyList(), eq(TEST_CASE_ID_HYPHENATED), eq(Party.RESPONDENT)))
            .thenReturn(notificationResponse2);
        when(notificationServiceCIC.sendEmail(any(), anyList(), eq(TEST_CASE_ID_HYPHENATED), eq(Party.REPRESENTATIVE)))
            .thenReturn(notificationResponse3);
        when(notificationServiceCIC.sendEmail(any(), anyList(), eq(TEST_CASE_ID_HYPHENATED), eq(Party.APPLICANT)))
            .thenReturn(notificationResponse4);


        String response = mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                    CASEWORKER_CONTACT_PARTIES)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Message sent \n## A notification has been sent to: Subject, Respondent, Representative, Applicant");

        verify(notificationServiceCIC, times(1))
            .sendEmail(any(), anyList(), eq(TEST_CASE_ID_HYPHENATED), eq(Party.SUBJECT));
        verify(notificationServiceCIC, times(1))
            .sendEmail(any(), anyList(), eq(TEST_CASE_ID_HYPHENATED), eq(Party.RESPONDENT));
        verify(notificationServiceCIC, times(1))
            .sendEmail(any(), anyList(), eq(TEST_CASE_ID_HYPHENATED), eq(Party.REPRESENTATIVE));
        verify(notificationServiceCIC, times(1))
            .sendEmail(any(), anyList(), eq(TEST_CASE_ID_HYPHENATED), eq(Party.APPLICANT));
        verifyNoMoreInteractions(notificationServiceCIC);
        verify(contactPartiesService).linkCorrespondenceIdsToDocuments(
            eq(List.of(42L)),
            argThat(list -> {
                assertThat(list).containsExactlyInAnyOrder(
                    NOTIFICATION_RESPONSE_ID_1,
                    NOTIFICATION_RESPONSE_ID_2,
                    NOTIFICATION_RESPONSE_ID_3,
                    NOTIFICATION_RESPONSE_ID_4
                );
                return true;
            })
        );
    }

    @Test
    void shouldNotDispatchNotificationsIfSelectedDocumentsCannotBeResolved() throws Exception {
        final CaseData caseData = caseData();
        caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
        caseData.setCicCase(CicCase.builder().notifyPartySubject(Set.of(SUBJECT)).build());

        DynamicMultiSelectList selection = DynamicMultiSelectList.builder()
            .value(List.of(DynamicListElement.builder().code(UUID.randomUUID()).label("missing.pdf").build()))
            .build();
        caseData.setContactPartiesDocuments(ContactPartiesDocuments.builder().documentList(selection).build());
        when(caseDocumentReadService.getSelectedContactPartyDocuments(any(Long.class), eq(selection), any(Integer.class)))
            .thenThrow(new DocumentSelectionException("Selected document is not available for this case"));

        String response = mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_CONTACT_PARTIES)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Documents could not be attached")
            .contains("Please resend the message and select the documents again");
        verifyNoInteractions(notificationServiceCIC, contactPartiesService);
    }

    @Test
    void shouldDispatchMessageWithoutDocuments() throws Exception {
        final CaseData caseData = caseData();
        caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
        caseData.setContactPartiesDocuments(ContactPartiesDocuments.builder().documentList(null).build());
        caseData.setCicCase(CicCase.builder()
            .notifyPartyRespondent(Set.of(RESPONDENT))
            .respondentName("Respondent Name")
            .respondentEmail("respondent@test.com")
            .build());
        when(notificationServiceCIC.sendEmail(any(), anyList(), eq(TEST_CASE_ID_HYPHENATED), eq(Party.RESPONDENT)))
            .thenReturn(NotificationResponse.builder().id(NOTIFICATION_RESPONSE_ID_1).build());

        String response = mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_CONTACT_PARTIES)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Message sent")
            .contains("Respondent");
        verify(notificationServiceCIC).sendEmail(any(), anyList(), eq(TEST_CASE_ID_HYPHENATED), eq(Party.RESPONDENT));
        verifyNoInteractions(contactPartiesService);
    }

    @Test
    void shouldDispatchMessageWhenDocumentsExistNoneSelected() throws Exception {
        final CaseData caseData = caseData();
        caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
        DynamicMultiSelectList documentList = DynamicMultiSelectList.builder()
            .listItems(List.of(DynamicListElement.builder().code(UUID.randomUUID()).label("doc.pdf").build()))
            .value(new ArrayList<>())
            .build();
        caseData.setContactPartiesDocuments(ContactPartiesDocuments.builder().documentList(documentList).build());
        caseData.setCicCase(CicCase.builder()
            .notifyPartySubject(Set.of(SUBJECT))
            .fullName("Subject Name")
            .email("subject@test.com")
            .contactPreferenceType(EMAIL)
            .build());
        when(notificationServiceCIC.sendEmail(any(), anyList(), eq(TEST_CASE_ID_HYPHENATED), eq(Party.SUBJECT)))
            .thenReturn(NotificationResponse.builder().id(NOTIFICATION_RESPONSE_ID_1).build());

        String response = mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_CONTACT_PARTIES)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Message sent")
            .contains("Subject");
        verify(notificationServiceCIC).sendEmail(any(), anyList(), eq(TEST_CASE_ID_HYPHENATED), eq(Party.SUBJECT));
        verifyNoInteractions(contactPartiesService);
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
                    CASEWORKER_CONTACT_PARTIES)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Contact Parties notification failed \n## Please resend the notification");

        verifyNoInteractions(notificationServiceCIC);
    }
}
