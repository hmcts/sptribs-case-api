package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.document.DocumentUtil;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.dispatcher.ContactPartiesNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.notification.model.Party;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.EMAIL;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.POST;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_1;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_7;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_PARTY_INFO;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_EMAIL_VALUE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME_VALUE;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_EMAIL_NEW_CD;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_POST;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ContactPartiesNotificationIT {

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    @MockitoBean
    private DocumentsRepository documentsRepository;

    @Captor
    ArgumentCaptor<NotificationRequest> notificationRequestCaptor;

    private static final NotificationResponse NOTIFICATION_RESPONSE = NotificationResponse.builder().id("123").build();

    @Nested
    @TestPropertySource(properties = "feature.citizen-dashboard.enabled=false")
    class WhenCitizenDashboardDisabled {
        @Autowired
        private ContactPartiesNotification contactPartiesNotification;

        @MockitoBean
        private NotificationServiceCIC notificationServiceCIC;

        @Test
        void shouldSendEmailToSubject() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .contactPreferenceType(EMAIL)
                    .fullName("Subject Name")
                    .email("subject@email.com")
                    .notifyPartyMessage("a message")
                    .build())
                .build();

            setUpCaseContactPartyDocuments(data);

            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.SUBJECT))).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToSubject(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.SUBJECT));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress()).isEqualTo("subject@email.com");
            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_EMAIL);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                "Subject Name",
                CONTACT_PARTY_INFO,
                "a message"));

            List<String> expectedDocUUIDs = getAttachedDocumentUUIDs(data);

            assertThat(notificationRequest.getUploadedDocuments()).hasSize(20);
            assertThat(notificationRequest.getUploadedDocuments().values()).containsAll(expectedDocUUIDs);
            assertThat(data.getCicCase().getSubjectNotifyList()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldSendEmailToSubject_NoAttachments() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .contactPreferenceType(EMAIL)
                    .fullName("Subject Name")
                    .email("subject@email.com")
                    .notifyPartyMessage("a message")
                    .build())
                .build();

            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.SUBJECT))).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToSubject(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.SUBJECT));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress()).isEqualTo("subject@email.com");
            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_EMAIL);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                "Subject Name",
                CONTACT_PARTY_INFO,
                "a message"));
            assertThat(notificationRequest.getUploadedDocuments()).isEmpty();
            assertThat(data.getCicCase().getSubjectNotifyList()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldSendLetterToSubject() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .contactPreferenceType(POST)
                    .fullName("Subject Name")
                    .address(AddressGlobalUK.builder().addressLine1("10 Buckingham Palace").postCode("W1 1BW").build())
                    .notifyPartyMessage("a message")
                    .build())
                .build();

            when(notificationServiceCIC.sendLetter(any(NotificationRequest.class), any())).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToSubject(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_POST);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                "Subject Name",
                CONTACT_PARTY_INFO,
                "a message",
                ADDRESS_LINE_1,
                "10 Buckingham Palace",
                ADDRESS_LINE_7,
                "W1 1BW"));
            assertThat(notificationRequest.getUploadedDocuments()).isNull();
            assertThat(data.getCicCase().getSubjectLetterNotifyList()).isEqualTo(NOTIFICATION_RESPONSE);
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
                .build();
            setUpCaseContactPartyDocuments(data);

            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.APPLICANT))).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToApplicant(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.APPLICANT));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress()).isEqualTo("applicant@email.com");
            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_EMAIL);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                "Applicant Name",
                CONTACT_PARTY_INFO,
                "a message"));

            List<String> expectedDocUUIDs = getAttachedDocumentUUIDs(data);

            assertThat(notificationRequest.getUploadedDocuments()).hasSize(20);
            assertThat(notificationRequest.getUploadedDocuments().values()).containsAll(expectedDocUUIDs);
            assertThat(data.getCicCase().getAppNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldSendEmailToApplicant_NoAttachments() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .applicantContactDetailsPreference(EMAIL)
                    .fullName("Subject Name")
                    .applicantFullName("Applicant Name")
                    .applicantEmailAddress("applicant@email.com")
                    .notifyPartyMessage("a message")
                    .build())
                .build();

            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.APPLICANT))).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToApplicant(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.APPLICANT));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress()).isEqualTo("applicant@email.com");
            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_EMAIL);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                "Applicant Name",
                CONTACT_PARTY_INFO,
                "a message"));

            assertThat(notificationRequest.getUploadedDocuments()).isEmpty();
            assertThat(data.getCicCase().getAppNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldSendLetterToApplicant() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .applicantContactDetailsPreference(POST)
                    .fullName("Subject Name")
                    .applicantFullName("Applicant Name")
                    .applicantEmailAddress("applicant@email.com")
                    .applicantAddress(AddressGlobalUK.builder().addressLine1("10 Buckingham Palace").postCode("W1 1BW").build())
                    .notifyPartyMessage("a message")
                    .build())
                .build();

            when(notificationServiceCIC.sendLetter(any(), any())).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToApplicant(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_POST);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                "Applicant Name",
                CONTACT_PARTY_INFO,
                "a message",
                ADDRESS_LINE_1,
                "10 Buckingham Palace",
                ADDRESS_LINE_7,
                "W1 1BW"));
            assertThat(notificationRequest.getUploadedDocuments()).isNull();
            assertThat(data.getCicCase().getAppLetterNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
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
                .build();

            setUpCaseContactPartyDocuments(data);

            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.REPRESENTATIVE))).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.REPRESENTATIVE));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            List<String> expectedDocUUIDs = getAttachedDocumentUUIDs(data);

            assertThat(notificationRequest.getDestinationAddress()).isEqualTo("representative@email.com");
            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_EMAIL);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                "Representative Name",
                CONTACT_PARTY_INFO,
                "a message"));
            assertThat(notificationRequest.getUploadedDocuments()).hasSize(20);
            assertThat(notificationRequest.getUploadedDocuments().values()).containsAll(expectedDocUUIDs);
            assertThat(data.getCicCase().getRepNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldSendEmailToRepresentative_NoAttachments() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .fullName("Subject Name")
                    .representativeContactDetailsPreference(EMAIL)
                    .representativeFullName("Representative Name")
                    .representativeEmailAddress("representative@email.com")
                    .notifyPartyMessage("a message")
                    .build())
                .build();

            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.REPRESENTATIVE))).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.REPRESENTATIVE));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress()).isEqualTo("representative@email.com");
            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_EMAIL);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                "Representative Name",
                CONTACT_PARTY_INFO,
                "a message"));
            assertThat(notificationRequest.getUploadedDocuments()).isEmpty();
            assertThat(data.getCicCase().getRepNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldSendLetterToRepresentative() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .fullName("Subject Name")
                    .representativeContactDetailsPreference(POST)
                    .representativeFullName("Representative Name")
                    .representativeEmailAddress("representative@email.com")
                    .representativeAddress(AddressGlobalUK.builder().addressLine1("10 Buckingham Palace").postCode("W1 1BW").build())
                    .notifyPartyMessage("a message")
                    .build())
                .build();

            when(notificationServiceCIC.sendLetter(any(), any())).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_POST);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                "Representative Name",
                CONTACT_PARTY_INFO,
                "a message",
                ADDRESS_LINE_1,
                "10 Buckingham Palace",
                ADDRESS_LINE_7,
                "W1 1BW"));
            assertThat(notificationRequest.getUploadedDocuments()).isNull();
            assertThat(data.getCicCase().getRepLetterNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldSendEmailToRespondent_NoAttachments() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .fullName("Subject Name")
                    .respondentName("Respondent Name")
                    .respondentEmail("respondent@email.com")
                    .notifyPartyMessage("a message")
                    .build())
                .build();

            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.RESPONDENT))).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToRespondent(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.RESPONDENT));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress()).isEqualTo("respondent@email.com");
            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_EMAIL);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                "Respondent Name",
                CONTACT_PARTY_INFO,
                "a message"));
            assertThat(notificationRequest.getUploadedDocuments()).isEmpty();
            assertThat(data.getCicCase().getResNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldSendEmailToRespondent() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .fullName("Subject Name")
                    .respondentName("Respondent Name")
                    .respondentEmail("respondent@email.com")
                    .notifyPartyMessage("a message")
                    .build())
                .build();
            setUpCaseContactPartyDocuments(data);

            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.RESPONDENT))).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToRespondent(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.RESPONDENT));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            List<String> expectedDocUUIDs = getAttachedDocumentUUIDs(data);

            assertThat(notificationRequest.getDestinationAddress()).isEqualTo("respondent@email.com");
            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_EMAIL);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                "Respondent Name",
                CONTACT_PARTY_INFO,
                "a message"));
            assertThat(notificationRequest.getUploadedDocuments()).hasSize(20);
            assertThat(notificationRequest.getUploadedDocuments().values()).containsAll(expectedDocUUIDs);
            assertThat(data.getCicCase().getResNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldSendEmailToTribunal_NoAttachments() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .fullName("Subject Name")
                    .respondentName("Respondent Name")
                    .respondentEmail("respondent@email.com")
                    .notifyPartyMessage("a message")
                    .build())
                .build();

            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.TRIBUNAL))).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToTribunal(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.TRIBUNAL));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress()).isEqualTo(TRIBUNAL_EMAIL_VALUE);
            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_EMAIL);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                TRIBUNAL_NAME_VALUE,
                CONTACT_PARTY_INFO,
                "a message"));
            assertThat(notificationRequest.getUploadedDocuments()).isEmpty();
            assertThat(data.getCicCase().getTribunalNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldSendEmailToTribunal() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .fullName("Subject Name")
                    .respondentName("Respondent Name")
                    .respondentEmail("respondent@email.com")
                    .notifyPartyMessage("a message")
                    .build())
                .build();
            setUpCaseContactPartyDocuments(data);

            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.TRIBUNAL))).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToTribunal(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.TRIBUNAL));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            List<String> expectedDocUUIDs = getAttachedDocumentUUIDs(data);

            assertThat(notificationRequest.getDestinationAddress()).isEqualTo(TRIBUNAL_EMAIL_VALUE);
            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_EMAIL);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                TRIBUNAL_NAME_VALUE,
                CONTACT_PARTY_INFO,
                "a message"));
            assertThat(notificationRequest.getUploadedDocuments()).hasSize(20);
            assertThat(notificationRequest.getUploadedDocuments().values()).containsAll(expectedDocUUIDs);
            assertThat(data.getCicCase().getTribunalNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
        }
    }

    @Nested
    @TestPropertySource(properties = "feature.citizen-dashboard.enabled=true")
    class WhenCitizenDashboardEnabled {
        @Autowired
        private ContactPartiesNotification contactPartiesNotification;

        @MockitoBean
        private NotificationServiceCIC notificationServiceCIC;

        @Test
        void shouldSendEmailToSubject() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .contactPreferenceType(EMAIL)
                    .fullName("Subject Name")
                    .email("subject@email.com")
                    .notifyPartyMessage("a message")
                    .build())
                .build();

            setUpCaseContactPartyDocuments(data);

            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.SUBJECT))).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToSubject(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.SUBJECT));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress()).isEqualTo("subject@email.com");
            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_EMAIL_NEW_CD);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                "Subject Name",
                CONTACT_PARTY_INFO,
                "a message"));

            List<String> expectedDocUUIDs = getAttachedDocumentUUIDs(data);

            assertThat(notificationRequest.getUploadedDocuments()).hasSize(20);
            assertThat(notificationRequest.getUploadedDocuments().values()).containsAll(expectedDocUUIDs);
            assertThat(data.getCicCase().getSubjectNotifyList()).isEqualTo(NOTIFICATION_RESPONSE);
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
                .build();
            setUpCaseContactPartyDocuments(data);

            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.APPLICANT))).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToApplicant(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.APPLICANT));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            assertThat(notificationRequest.getDestinationAddress()).isEqualTo("applicant@email.com");
            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_EMAIL_NEW_CD);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                "Applicant Name",
                CONTACT_PARTY_INFO,
                "a message"));

            List<String> expectedDocUUIDs = getAttachedDocumentUUIDs(data);

            assertThat(notificationRequest.getUploadedDocuments()).hasSize(20);
            assertThat(notificationRequest.getUploadedDocuments().values()).containsAll(expectedDocUUIDs);
            assertThat(data.getCicCase().getAppNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
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
                .build();

            setUpCaseContactPartyDocuments(data);

            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.REPRESENTATIVE))).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.REPRESENTATIVE));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            List<String> expectedDocUUIDs = getAttachedDocumentUUIDs(data);

            assertThat(notificationRequest.getDestinationAddress()).isEqualTo("representative@email.com");
            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_EMAIL_NEW_CD);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                "Representative Name",
                CONTACT_PARTY_INFO,
                "a message"));
            assertThat(notificationRequest.getUploadedDocuments()).hasSize(20);
            assertThat(notificationRequest.getUploadedDocuments().values()).containsAll(expectedDocUUIDs);
            assertThat(data.getCicCase().getRepNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldSendEmailToRespondent() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .fullName("Subject Name")
                    .respondentName("Respondent Name")
                    .respondentEmail("respondent@email.com")
                    .notifyPartyMessage("a message")
                    .build())
                .build();
            setUpCaseContactPartyDocuments(data);

            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.RESPONDENT))).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToRespondent(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.RESPONDENT));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            List<String> expectedDocUUIDs = getAttachedDocumentUUIDs(data);

            assertThat(notificationRequest.getDestinationAddress()).isEqualTo("respondent@email.com");
            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_EMAIL);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                "Respondent Name",
                CONTACT_PARTY_INFO,
                "a message"));
            assertThat(notificationRequest.getUploadedDocuments()).hasSize(20);
            assertThat(notificationRequest.getUploadedDocuments().values()).containsAll(expectedDocUUIDs);
            assertThat(data.getCicCase().getResNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldSendEmailToTribunal() {
            final CaseData data = CaseData.builder()
                .cicCase(CicCase.builder()
                    .fullName("Subject Name")
                    .respondentName("Respondent Name")
                    .respondentEmail("respondent@email.com")
                    .notifyPartyMessage("a message")
                    .build())
                .build();
            setUpCaseContactPartyDocuments(data);

            when(notificationServiceCIC.sendEmail(any(NotificationRequest.class),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.TRIBUNAL))).thenReturn(NOTIFICATION_RESPONSE);

            contactPartiesNotification.sendToTribunal(data, TEST_CASE_ID.toString());

            verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(),
                anyList(),
                eq(TEST_CASE_ID.toString()),
                eq(Party.TRIBUNAL));

            NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

            List<String> expectedDocUUIDs = getAttachedDocumentUUIDs(data);

            assertThat(notificationRequest.getDestinationAddress()).isEqualTo(TRIBUNAL_EMAIL_VALUE);
            assertThat(notificationRequest.getTemplate()).isEqualTo(CONTACT_PARTIES_EMAIL);
            assertThat(notificationRequest.getTemplateVars()).containsAllEntriesOf(Map.of(TRIBUNAL_NAME,
                CIC,
                CIC_CASE_NUMBER,
                TEST_CASE_ID.toString(),
                CIC_CASE_SUBJECT_NAME,
                "Subject Name",
                CONTACT_NAME,
                TRIBUNAL_NAME_VALUE,
                CONTACT_PARTY_INFO,
                "a message"));
            assertThat(notificationRequest.getUploadedDocuments()).hasSize(20);
            assertThat(notificationRequest.getUploadedDocuments().values()).containsAll(expectedDocUUIDs);
            assertThat(data.getCicCase().getTribunalNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
        }
    }

    private static List<String> getAttachedDocumentUUIDs(CaseData data) {
        var selectedDocuments = DocumentListUtil.getSelectedDocumentsFromDynamicList(data,
            data.getContactPartiesDocuments().getDocumentList());
        return selectedDocuments.stream().map(DocumentUtil::getDocumentUUID).toList();
    }

    private void setUpCaseContactPartyDocuments(CaseData caseData) {
        List<ListValue<CaseworkerCICDocument>> docs = TestDataHelper.getCaseworkerCICDocumentList("test.pdf", "test1.doc", "test2.pdf");
        caseData.setContactPartiesDocuments(ContactPartiesDocuments
            .builder()
            .build());
        caseData.setAllDocManagement(DocumentManagement
            .builder()
            .caseworkerCICDocument(docs)
            .build());

        DynamicMultiSelectList contactPartiesList = DocumentListUtil.prepareContactPartiesDocumentList(caseData, "test.url");
        contactPartiesList.setValue(contactPartiesList.getListItems());
        caseData
            .getContactPartiesDocuments()
            .setDocumentList(contactPartiesList);
    }
}
