package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.dispatcher.ContactPartiesNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.EMAIL;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.POST;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_1;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_7;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CASE_DOCUMENT;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_PARTY_INFO;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DOC_AVAILABLE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_EMAIL_VALUE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME_VALUE;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_POST;
import static uk.gov.hmcts.sptribs.testutil.IdamWireMock.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ContactPartiesNotificationIT {

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    @MockitoBean
    private CaseDocumentClientApi caseDocumentClientApi;

    @MockitoBean
    private DocumentsRepository documentsRepository;

    @MockitoBean
    private IdamService idamService;

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

    @Autowired
    private ContactPartiesNotification contactPartiesNotification;

    @Captor
    ArgumentCaptor<NotificationRequest> notificationRequestCaptor;

    private User systemUser;

    @BeforeEach
    void configureMocks() {
        final User user = new User(
            TEST_AUTHORIZATION_TOKEN,
            UserDetails.builder()
                .roles(List.of("caseworker-st_cic", "caseworker-sptribs-systemupdate"))
                .build()
        );

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

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
    }

    @Test
    void shouldSendEmailToSubject() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .contactPreferenceType(EMAIL)
                .fullName("Subject Name")
                .email("subject@email.com")
                .notifyPartyMessage("a message")
                .build())
            .contactPartiesDocuments(getContactPartiesDocuments())
            .build();

        contactPartiesNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("subject@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CONTACT_PARTIES_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Subject Name",
                CONTACT_PARTY_INFO, "a message"
            ));
        assertThat(notificationRequest.getUploadedDocuments())
            .containsAllEntriesOf(Map.of(
                DOC_AVAILABLE + 1, "yes",
                CASE_DOCUMENT + 1, "0777a9cf-4be8-4664-baa2-9eb5563abaec",
                DOC_AVAILABLE + 2, "no",
                CASE_DOCUMENT + 2, EMPTY_PLACEHOLDER
            ));
    }

    @Test
    void shouldSendLetterToSubject() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .contactPreferenceType(POST)
                .fullName("Subject Name")
                .address(AddressGlobalUK.builder()
                    .addressLine1("10 Buckingham Palace")
                    .postCode("W1 1BW")
                    .build()
                )
                .notifyPartyMessage("a message")
                .build())
            .build();

        contactPartiesNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CONTACT_PARTIES_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Subject Name",
                CONTACT_PARTY_INFO, "a message",
                ADDRESS_LINE_1, "10 Buckingham Palace",
                ADDRESS_LINE_7, "W1 1BW"
            ));
    }

    @Test
    void shouldSendEmailToApplicant() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .applicantContactDetailsPreference(EMAIL)
                .fullName("Subject Name")
                .applicantFullName("Applicant Name")
                .applicantEmailAddress("applicant@email.com")
                .notifyPartyMessage("a message")
                .build())
            .contactPartiesDocuments(getContactPartiesDocuments())
            .build();

        contactPartiesNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("applicant@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CONTACT_PARTIES_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Applicant Name",
                CONTACT_PARTY_INFO, "a message"
            ));
        assertThat(notificationRequest.getUploadedDocuments())
            .containsAllEntriesOf(Map.of(
                DOC_AVAILABLE + 1, "yes",
                CASE_DOCUMENT + 1, "0777a9cf-4be8-4664-baa2-9eb5563abaec",
                DOC_AVAILABLE + 2, "no",
                CASE_DOCUMENT + 2, EMPTY_PLACEHOLDER
            ));
    }

    @Test
    void shouldSendLetterToApplicant() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .applicantContactDetailsPreference(POST)
                .fullName("Subject Name")
                .applicantFullName("Applicant Name")
                .applicantEmailAddress("applicant@email.com")
                .applicantAddress(AddressGlobalUK.builder()
                    .addressLine1("10 Buckingham Palace")
                    .postCode("W1 1BW")
                    .build()
                )
                .notifyPartyMessage("a message")
                .build())
            .contactPartiesDocuments(getContactPartiesDocuments())
            .build();

        contactPartiesNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CONTACT_PARTIES_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Applicant Name",
                CONTACT_PARTY_INFO, "a message",
                ADDRESS_LINE_1, "10 Buckingham Palace",
                ADDRESS_LINE_7, "W1 1BW"
            ));
    }

    @Test
    void shouldSendEmailToRepresentative() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Subject Name")
                .representativeContactDetailsPreference(EMAIL)
                .representativeFullName("Representative Name")
                .representativeEmailAddress("representative@email.com")
                .notifyPartyMessage("a message")
                .build())
            .contactPartiesDocuments(getContactPartiesDocuments())
            .build();

        contactPartiesNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("representative@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CONTACT_PARTIES_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Representative Name",
                CONTACT_PARTY_INFO, "a message"
            ));
        assertThat(notificationRequest.getUploadedDocuments())
            .containsAllEntriesOf(Map.of(
                DOC_AVAILABLE + 1, "yes",
                CASE_DOCUMENT + 1, "0777a9cf-4be8-4664-baa2-9eb5563abaec",
                DOC_AVAILABLE + 2, "no",
                CASE_DOCUMENT + 2, EMPTY_PLACEHOLDER
            ));
    }

    @Test
    void shouldSendLetterToRepresentative() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Subject Name")
                .representativeContactDetailsPreference(POST)
                .representativeFullName("Representative Name")
                .representativeEmailAddress("representative@email.com")
                .representativeAddress(AddressGlobalUK.builder()
                    .addressLine1("10 Buckingham Palace")
                    .postCode("W1 1BW")
                    .build()
                )
                .notifyPartyMessage("a message")
                .build())
            .build();

        contactPartiesNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CONTACT_PARTIES_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Representative Name",
                CONTACT_PARTY_INFO, "a message",
                ADDRESS_LINE_1, "10 Buckingham Palace",
                ADDRESS_LINE_7, "W1 1BW"
            ));
    }

    @Test
    void shouldSendEmailToRespondentWithoutDocumentsAttached() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Subject Name")
                .respondentName("Respondent Name")
                .respondentEmail("respondent@email.com")
                .notifyPartyMessage("a message")
                .build())
            .build();

        contactPartiesNotification.sendToRespondent(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("respondent@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CONTACT_PARTIES_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Respondent Name",
                CONTACT_PARTY_INFO, "a message"
            ));
    }

    @Test
    void shouldSendEmailToRespondentWithDocumentsAttached() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Subject Name")
                .respondentName("Respondent Name")
                .respondentEmail("respondent@email.com")
                .notifyPartyMessage("a message")
                .build())
            .contactPartiesDocuments(getContactPartiesDocuments())
            .build();

        contactPartiesNotification.sendToRespondent(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("respondent@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CONTACT_PARTIES_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, "Respondent Name",
                CONTACT_PARTY_INFO, "a message"
            ));
        assertThat(notificationRequest.getUploadedDocuments())
            .containsAllEntriesOf(Map.of(
                DOC_AVAILABLE + 1, "yes",
                CASE_DOCUMENT + 1, "0777a9cf-4be8-4664-baa2-9eb5563abaec",
                DOC_AVAILABLE + 2, "no",
                CASE_DOCUMENT + 2, EMPTY_PLACEHOLDER
            ));
    }

    @Test
    void shouldSendEmailToTribunalWithoutDocumentsAttached() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Subject Name")
                .respondentName("Respondent Name")
                .respondentEmail("respondent@email.com")
                .notifyPartyMessage("a message")
                .build())
            .build();

        contactPartiesNotification.sendToTribunal(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo(TRIBUNAL_EMAIL_VALUE);
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CONTACT_PARTIES_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, TRIBUNAL_NAME_VALUE,
                CONTACT_PARTY_INFO, "a message"
            ));
    }

    @Test
    void shouldSendEmailToTribunalWithDocumentsAttached() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Subject Name")
                .respondentName("Respondent Name")
                .respondentEmail("respondent@email.com")
                .notifyPartyMessage("a message")
                .build())
            .contactPartiesDocuments(getContactPartiesDocuments())
            .build();

        contactPartiesNotification.sendToTribunal(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo(TRIBUNAL_EMAIL_VALUE);
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(CONTACT_PARTIES_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(Map.of(
                TRIBUNAL_NAME, CIC,
                CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME, "Subject Name",
                CONTACT_NAME, TRIBUNAL_NAME_VALUE,
                CONTACT_PARTY_INFO, "a message"
            ));
        assertThat(notificationRequest.getUploadedDocuments())
            .containsAllEntriesOf(Map.of(
                DOC_AVAILABLE + 1, "yes",
                CASE_DOCUMENT + 1, "0777a9cf-4be8-4664-baa2-9eb5563abaec",
                DOC_AVAILABLE + 2, "no",
                CASE_DOCUMENT + 2, EMPTY_PLACEHOLDER
            ));
    }

    private ContactPartiesDocuments getContactPartiesDocuments() {
        final List<DynamicListElement> elements = new ArrayList<>();
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("[pdf.pdf A - Application Form](http://manage-case.demo.platform.hmcts.net/documents/0777a9cf-4be8-4664-baa2-9eb5563abaec/binary)")
            .code(UUID.randomUUID())
            .build();
        elements.add(listItem);

        return ContactPartiesDocuments.builder()
            .documentList(DynamicMultiSelectList
                .builder()
                .value(elements)
                .listItems(elements)
                .build())
            .build();
    }
}
