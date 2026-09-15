package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.notification.model.Party;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_EMAIL_VALUE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME_VALUE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class ContactPartiesNotificationTest {

    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private ContactPartiesNotification contactPartiesNotification;

    @Captor
    private ArgumentCaptor<List<CaseworkerCICDocument>> selectedDocumentsCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> templateVarsCaptor;

    private static final UUID TEST_DOCUMENT_ID = UUID.randomUUID();
    private static final String NOTIFICATION_RESPONSE_ID = "123";


    @Nested
    class WhenCitizenDashboardDisabled {
        @BeforeEach
        void setUpFlags() {
            ReflectionTestUtils.setField(contactPartiesNotification, "citizenDashboardEnabled", false);
        }

        @Test
        void shouldNotifySubjectOfContactPartiesWithEmailWithAttachments() {
            //Given
            final CaseData data = withSelectedContactPartiesDocument(getMockCaseData());
            data.getCicCase().setNotifyPartyMessage("message");
            data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            data.getCicCase().setEmail("testSubject@outlook.com");
            final Map<String, Object> comonVarsMap = Map.of(
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
                CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName());
            final Map<String, String> uploadedDocuments = getDocumentUploadMap();
            NotificationResponse notificationResponse = new NotificationResponse();
            notificationResponse.setId(NOTIFICATION_RESPONSE_ID);

            //When
            when(notificationHelper.buildDocumentList(eq(data.getContactPartiesDocuments().getDocumentList()), eq(10)))
                .thenReturn(uploadedDocuments);
            when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().templateDocumentVars(uploadedDocuments).build());
            when(notificationService.sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT)))
                .thenReturn(notificationResponse);

            String correspondenceId =
                contactPartiesNotification.sendToSubject(data, TEST_CASE_ID.toString(), uploadedDocuments);

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE_ID);
            verify(notificationService).sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(data.getCicCase().getEmail()),
                eq(true),
                eq(uploadedDocuments),
                templateVarsCaptor.capture(),
                eq(TemplateName.CONTACT_PARTIES_EMAIL));
            assertThat(selectedDocumentsCaptor.getValue()).hasSize(1);
            assertThat(templateVarsCaptor.getValue()).containsAllEntriesOf(comonVarsMap)
                .doesNotContainKey(DASHBOARD_KEY);
        }

        @Test
        void shouldNotifySubjectOfContactPartiesWithPost() {
            //Given
            final Map<String, String> uploadedDocuments = getDocumentUploadMap();
            final CaseData data = getMockCaseData();
            data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
            data.getCicCase().setNotifyPartyMessage("message");
            data.getCicCase().setAddress(
                new AddressGlobalUK("11", "JOHN", "STREET", "WINCHESTER", "COUNTY", "TW4 5BH", "UK"));
            final Map<String, Object> comonVarsMap = Map.of(
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
                CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName());
            NotificationResponse notificationResponse = new NotificationResponse();
            notificationResponse.setId(NOTIFICATION_RESPONSE_ID);

            //When
            when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString())))
                .thenReturn(notificationResponse);

            String correspondenceId =
                contactPartiesNotification.sendToSubject(data, TEST_CASE_ID.toString(), uploadedDocuments);

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE_ID);
            verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
            verify(notificationHelper).buildLetterNotificationRequest(
                comonVarsMap,
                TemplateName.CONTACT_PARTIES_POST);
        }

        @Test
        void shouldNotifyApplicantOfContactPartiesWithEmailWithAttachments() {
            //Given
            final Map<String, String> uploadedDocuments = getDocumentUploadMap();
            final CaseData data = withSelectedContactPartiesDocument(getMockCaseData());
            data.getCicCase().setApplicantFullName("appFullName");
            data.getCicCase().setNotifyPartyMessage("message");
            data.getCicCase().setApplicantEmailAddress("testApplicant@outlook.com");
            data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.EMAIL);
            final Map<String, Object> comonVarsMap = Map.of(
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
                CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName());
            NotificationResponse notificationResponse = new NotificationResponse();
            notificationResponse.setId(NOTIFICATION_RESPONSE_ID);

            //When
            when(notificationHelper.buildDocumentList(eq(data.getContactPartiesDocuments().getDocumentList()), eq(10)))
                .thenReturn(uploadedDocuments);
            when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().templateDocumentVars(getDocumentUploadMap()).build());

            when(notificationService.sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(Party.APPLICANT)))
                .thenReturn(notificationResponse);

            String correspondenceId =
                contactPartiesNotification.sendToApplicant(data, TEST_CASE_ID.toString(), uploadedDocuments);

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE_ID);
            verify(notificationService).sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(Party.APPLICANT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(data.getCicCase().getApplicantEmailAddress()),
                eq(true),
                eq(uploadedDocuments),
                templateVarsCaptor.capture(),
                eq(TemplateName.CONTACT_PARTIES_EMAIL));
            assertThat(selectedDocumentsCaptor.getValue()).hasSize(1);
            assertThat(templateVarsCaptor.getValue()).containsAllEntriesOf(comonVarsMap)
                .doesNotContainKey(DASHBOARD_KEY);
        }

        @Test
        void shouldNotifyApplicantOfContactPartiesWithPost() {
            //Given
            final Map<String, String> uploadedDocuments = getDocumentUploadMap();
            final CaseData data = getMockCaseData();
            data.getCicCase().setApplicantFullName("appFullName");
            data.getCicCase().setNotifyPartyMessage("message");
            data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.POST);
            data.getCicCase().setApplicantAddress(
                new AddressGlobalUK("11", "JOHN", "STREET", "WINCHESTER", "COUNTY", "TW4 5BH", "UK"));

            final Map<String, Object> comonVarsMap = Map.of(
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
                CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName());
            NotificationResponse notificationResponse = new NotificationResponse();
            notificationResponse.setId(NOTIFICATION_RESPONSE_ID);

            //When
            when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString())))
                .thenReturn(notificationResponse);
            String correspondenceId =
                contactPartiesNotification.sendToApplicant(data, TEST_CASE_ID.toString(), uploadedDocuments);

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE_ID);
            verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
            verify(notificationHelper).buildLetterNotificationRequest(
                comonVarsMap,
                TemplateName.CONTACT_PARTIES_POST);
        }

        @Test
        void shouldNotifyRepresentativeOfContactPartiesWithEmailWithAttachments() {
            //Given
            final Map<String, String> uploadedDocuments = getDocumentUploadMap();
            final CaseData data = withSelectedContactPartiesDocument(getMockCaseData());
            data.getCicCase().setRepresentativeFullName("repFullName");
            data.getCicCase().setNotifyPartyMessage("message");
            data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");
            data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
            final Map<String, Object> comonVarsMap = Map.of(
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
                CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName());
            NotificationResponse notificationResponse = new NotificationResponse();
            notificationResponse.setId(NOTIFICATION_RESPONSE_ID);

            //When
            when(notificationHelper.buildDocumentList(eq(data.getContactPartiesDocuments().getDocumentList()), eq(10)))
                .thenReturn(uploadedDocuments);
            when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().templateDocumentVars(getDocumentUploadMap()).build());
            when(notificationService.sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(Party.REPRESENTATIVE)))
                .thenReturn(notificationResponse);

            String correspondenceId =
                contactPartiesNotification.sendToRepresentative(data, TEST_CASE_ID.toString(), uploadedDocuments);

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE_ID);
            verify(notificationService).sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(Party.REPRESENTATIVE));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(data.getCicCase().getRepresentativeEmailAddress()),
                eq(true),
                eq(uploadedDocuments),
                templateVarsCaptor.capture(),
                eq(TemplateName.CONTACT_PARTIES_EMAIL));

            assertThat(selectedDocumentsCaptor.getValue()).hasSize(1);
            assertThat(templateVarsCaptor.getValue()).containsAllEntriesOf(comonVarsMap)
                .doesNotContainKey(DASHBOARD_KEY);
        }

        @Test
        void shouldNotifyRepresentativeOfContactPartiesWithPost() {
            //Given
            final Map<String, String> uploadedDocuments = getDocumentUploadMap();
            final CaseData data = getMockCaseData();
            data.getCicCase().setRepresentativeFullName("repFullName");
            data.getCicCase().setNotifyPartyMessage("message");
            data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
            data.getCicCase().setRepresentativeAddress(
                new AddressGlobalUK("11", "JOHN", "STREET", "WINCHESTER", "COUNTY", "TW4 5BH", "UK")
            );
            final Map<String, Object> comonVarsMap = Map.of(
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
                CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName());
            NotificationResponse notificationResponse = new NotificationResponse();
            notificationResponse.setId(NOTIFICATION_RESPONSE_ID);

            //When
            when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString())))
                .thenReturn(notificationResponse);

            String correspondenceId =
                contactPartiesNotification.sendToRepresentative(data, TEST_CASE_ID.toString(), uploadedDocuments);

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE_ID);
            verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
            verify(notificationHelper).buildLetterNotificationRequest(
                comonVarsMap,
                TemplateName.CONTACT_PARTIES_POST);
        }


        @Test
        void shouldNotifyRespondentOfContactPartiesWithEmail() {
            //Given
            final Map<String, String> uploadedDocuments = getDocumentUploadMap();
            final CaseData data = getMockCaseData();
            data.getCicCase().setRepresentativeFullName("respFullName");
            data.getCicCase().setNotifyPartyMessage("message");
            final Map<String, Object> comonVarsMap = Map.of(
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
                CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName());
            NotificationResponse notificationResponse = new NotificationResponse();
            notificationResponse.setId(NOTIFICATION_RESPONSE_ID);

            //When
            when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT)))
                .thenReturn(notificationResponse);

            String correspondenceId =
                contactPartiesNotification.sendToRespondent(data, TEST_CASE_ID.toString(), uploadedDocuments);

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE_ID);
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(data.getCicCase().getRespondentEmail()),
                templateVarsCaptor.capture(),
                eq(TemplateName.CONTACT_PARTIES_EMAIL));
            assertThat(templateVarsCaptor.getValue()).containsAllEntriesOf(comonVarsMap)
                .doesNotContainKey(DASHBOARD_KEY);
        }

        @Test
        void shouldNotifyRespondentOfContactPartiesWithEmailWithAttachments() {
            //Given
            final Map<String, String> uploadedDocuments = getDocumentUploadMap();
            final CaseData data = withSelectedContactPartiesDocument(getMockCaseData());
            data.getCicCase().setRepresentativeFullName("respFullName");
            data.getCicCase().setNotifyPartyMessage("message");
            final Map<String, Object> comonVarsMap = Map.of(
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
                CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName());
            NotificationResponse notificationResponse = new NotificationResponse();
            notificationResponse.setId(NOTIFICATION_RESPONSE_ID);

            //When
            when(notificationHelper.buildDocumentList(eq(data.getContactPartiesDocuments().getDocumentList()), eq(10)))
                .thenReturn(uploadedDocuments);
            when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().templateDocumentVars(getDocumentUploadMap()).build());
            when(notificationService.sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT)))
                .thenReturn(notificationResponse);

            String correspondenceId =
                contactPartiesNotification.sendToRespondent(data, TEST_CASE_ID.toString(), uploadedDocuments);

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE_ID);
            verify(notificationService).sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(data.getCicCase().getRespondentEmail()),
                eq(true),
                eq(uploadedDocuments),
                templateVarsCaptor.capture(),
                eq(TemplateName.CONTACT_PARTIES_EMAIL));
            assertThat(selectedDocumentsCaptor.getValue()).hasSize(1);
            assertThat(templateVarsCaptor.getValue()).containsAllEntriesOf(comonVarsMap)
                .doesNotContainKey(DASHBOARD_KEY);
        }

        @Test
        void shouldNotifyTribunalOfContactPartiesWithEmail() {
            //Given
            final Map<String, String> uploadedDocuments = getDocumentUploadMap();
            final CaseData data = getMockCaseData();
            data.getCicCase().setRepresentativeFullName("respFullName");
            data.getCicCase().setNotifyPartyMessage("message");
            NotificationResponse notificationResponse = new NotificationResponse();
            notificationResponse.setId(NOTIFICATION_RESPONSE_ID);

            //When
            when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.TRIBUNAL)))
                .thenReturn(notificationResponse);

            String correspondenceId =
                contactPartiesNotification.sendToTribunal(data, TEST_CASE_ID.toString(), uploadedDocuments);
            final Map<String, Object> comonVarsMap = Map.of(
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
                CommonConstants.CIC_CASE_TRIBUNAL_NAME, TRIBUNAL_NAME_VALUE);

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE_ID);
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.TRIBUNAL));
            verify(notificationHelper).buildEmailNotificationRequest(
                TRIBUNAL_EMAIL_VALUE,
                comonVarsMap,
                TemplateName.CONTACT_PARTIES_EMAIL);
        }

        @Test
        void shouldNotifyTribunalOfContactPartiesWithEmailWithAttachments() {
            //Given
            final Map<String, String> uploadedDocuments = getDocumentUploadMap();
            final CaseData data = withSelectedContactPartiesDocument(getMockCaseData());
            data.getCicCase().setNotifyPartyMessage("message");
            final Map<String, Object> comonVarsMap = Map.of(
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
                CommonConstants.CIC_CASE_TRIBUNAL_NAME, TRIBUNAL_NAME_VALUE);
            NotificationResponse notificationResponse = new NotificationResponse();
            notificationResponse.setId(NOTIFICATION_RESPONSE_ID);

            //When
            when(notificationHelper.buildDocumentList(eq(data.getContactPartiesDocuments().getDocumentList()), eq(10)))
                .thenReturn(uploadedDocuments);
            when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().templateDocumentVars(getDocumentUploadMap()).build());

            when(notificationService.sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(Party.TRIBUNAL)))
                .thenReturn(notificationResponse);

            String correspondenceId =
                contactPartiesNotification.sendToTribunal(data, TEST_CASE_ID.toString(), uploadedDocuments);

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE_ID);
            verify(notificationService).sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(Party.TRIBUNAL));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(TRIBUNAL_EMAIL_VALUE),
                eq(true),
                eq(uploadedDocuments),
                templateVarsCaptor.capture(),
                eq(TemplateName.CONTACT_PARTIES_EMAIL));
            assertThat(selectedDocumentsCaptor.getValue()).hasSize(1);
            assertThat(templateVarsCaptor.getValue()).containsAllEntriesOf(comonVarsMap)
                .doesNotContainKey(DASHBOARD_KEY);
        }
    }

    @Nested
    class WhenCitizenDashboardEnabled {
        @BeforeEach
        void setUpFlags() {
            ReflectionTestUtils.setField(contactPartiesNotification, "citizenDashboardEnabled", true);
            ReflectionTestUtils.setField(contactPartiesNotification, "citizenDashboardUrl", "https://frontend.url/dashboard");
        }

        @Test
        void shouldNotifySubjectOfContactPartiesWithEmailWithAttachments() {
            //Given
            final CaseData data = withSelectedContactPartiesDocument(getMockCaseData());
            data.getCicCase().setNotifyPartyMessage("message");
            data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            data.getCicCase().setEmail("testSubject@outlook.com");
            final Map<String, Object> comonVarsMap = Map.of(
                CommonConstants.CONTACT_PARTY_INFO, data.getCicCase().getNotifyPartyMessage(),
                CommonConstants.CIC_CASE_SUBJECT_NAME, data.getCicCase().getFullName());
            final Map<String, String> uploadedDocuments = getDocumentUploadMap();
            NotificationResponse notificationResponse = new NotificationResponse();
            notificationResponse.setId(NOTIFICATION_RESPONSE_ID);

            //When
            when(notificationHelper.buildDocumentList(eq(data.getContactPartiesDocuments().getDocumentList()), eq(10)))
                .thenReturn(uploadedDocuments);
            when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().templateDocumentVars(uploadedDocuments).build());
            when(notificationService.sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT)))
                .thenReturn(notificationResponse);

            String correspondenceId =
                contactPartiesNotification.sendToSubject(data, TEST_CASE_ID.toString(), uploadedDocuments);

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE_ID);
            verify(notificationService).sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(data.getCicCase().getEmail()),
                eq(true),
                eq(uploadedDocuments),
                templateVarsCaptor.capture(),
                eq(TemplateName.CONTACT_PARTIES_EMAIL_NEW_CD));
            assertThat(selectedDocumentsCaptor.getValue()).hasSize(1);
            assertThat(templateVarsCaptor.getValue()).containsAllEntriesOf(comonVarsMap)
                .containsKey(DASHBOARD_KEY);
        }

    }

    private CaseData getMockCaseData() {
        final CicCase cicCase = CicCase.builder().fullName("fullName").caseNumber(TEST_CASE_ID.toString()).build();

        return CaseData.builder()
            .cicCase(cicCase)
            .build();
    }

    private CaseData withSelectedContactPartiesDocument(CaseData caseData) {
        caseData.setContactPartiesDocuments(ContactPartiesDocuments.builder().build());

        List<ListValue<CaseworkerCICDocument>> cicDocumentList = TestDataHelper.getCaseworkerCICDocumentList("test.pdf");
        caseData.setAllDocManagement(DocumentManagement.builder().caseworkerCICDocument(cicDocumentList).build());

        DynamicMultiSelectList contactPartiesList = DocumentListUtil.prepareContactPartiesDocumentList(caseData, "test.url");
        contactPartiesList.setValue(contactPartiesList.getListItems());
        caseData.getContactPartiesDocuments().setDocumentList(contactPartiesList);
        return caseData;
    }

    private Map<String, String> getDocumentUploadMap() {
        return java.util.Map.of(
            "CaseDocument1", TEST_DOCUMENT_ID.toString(),
            "CaseDocument2", "",
            "CaseDocument3", "",
            "CaseDocument4", "",
            "CaseDocument5", "",
            "DocumentAvailable1", "yes",
            "DocumentAvailable2", "no",
            "DocumentAvailable3", "no",
            "DocumentAvailable4", "no",
            "DocumentAvailable5", "no");
    }
}
