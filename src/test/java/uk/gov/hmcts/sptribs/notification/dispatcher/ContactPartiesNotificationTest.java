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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_EMAIL_VALUE;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_POST;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class ContactPartiesNotificationTest {

    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private ContactPartiesNotification contactPartiesNotification;

    private static final NotificationResponse NOTIFICATION_RESPONSE = NotificationResponse.builder().id("123").build();
    private static final AddressGlobalUK TEST_ADDRESS = AddressGlobalUK.builder().addressLine1("1 Test St").build();
    private List<CaseworkerCICDocument> expectedAttachedDocuments;

    @Captor
    private ArgumentCaptor<Map<String, Object>> templateVarsCaptor;

    private CaseData caseData;
    private CicCase cicCase;

    @BeforeEach
    void setUp() {
        caseData = new CaseData();
        cicCase = CicCase.builder().fullName("fullName").caseNumber(TEST_CASE_ID.toString()).notifyPartyMessage("message").build();

        caseData.setCicCase(cicCase);
        caseData.setContactPartiesDocuments(ContactPartiesDocuments.builder().build());

        caseData.setContactPartiesDocuments(ContactPartiesDocuments.builder().build());
        List<ListValue<CaseworkerCICDocument>> cicDocumentList = TestDataHelper.getCaseworkerCICDocumentList("test.pdf",
            "test1.doc",
            "test2.pdf");
        caseData.setAllDocManagement(DocumentManagement.builder().caseworkerCICDocument(cicDocumentList).build());

        DynamicMultiSelectList contactPartiesList = DocumentListUtil.prepareContactPartiesDocumentList(caseData, "test.url");
        contactPartiesList.setValue(contactPartiesList.getListItems());
        caseData.getContactPartiesDocuments().setDocumentList(contactPartiesList);

        expectedAttachedDocuments = cicDocumentList.stream().map(ListValue::getValue).toList();
    }

    @Nested
    class WhenCitizenDashboardDisabled {
        @BeforeEach
        void setUpFlags() {
            ReflectionTestUtils.setField(contactPartiesNotification, "citizenDashboardEnabled", false);
        }

        @Test
        void shouldNotifySubjectByEmailWithCorrectPartyAndWriteBack() {
            //Given
            caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            caseData.getCicCase().setEmail("test@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = contactPartiesNotification.sendToSubject(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(caseData.getCicCase().getEmail()),
                eq(expectedAttachedDocuments),
                eq(new HashMap<>()),
                templateVarsCaptor.capture(),
                eq(TemplateName.CONTACT_PARTIES_EMAIL));

            Map<String, Object> actualTemplateVars = templateVarsCaptor.getValue();
            assertThat(actualTemplateVars)
                .containsEntry(CommonConstants.CONTACT_PARTY_INFO, "message")
                .containsEntry(CommonConstants.CIC_CASE_SUBJECT_NAME, "fullName");
            assertThat(cicCase.getSubjectNotifyList()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldNotifySubjectByLetterWhenPreferenceIsNotEmail() {
            //given
            cicCase.setContactPreferenceType(ContactPreferenceType.POST);
            cicCase.setAddress(TEST_ADDRESS);

            when(notificationHelper.getSubjectCommonVars(TEST_CASE_ID.toString(), caseData)).thenReturn(new HashMap<>());
            when(notificationHelper.buildLetterNotificationRequest(anyMap(), eq(CONTACT_PARTIES_POST)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(
                NOTIFICATION_RESPONSE);

            //when
            String correspondenceId = contactPartiesNotification.sendToSubject(caseData, TEST_CASE_ID.toString());

            //then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationHelper).addAddressTemplateVars(eq(TEST_ADDRESS), anyMap());
            verify(notificationService, never()).sendEmail(any(), any(), any());
            assertThat(cicCase.getSubjectLetterNotifyList()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldNotifyApplicantByEmailWithCorrectPartyAndWriteBack() {
            //Given
            caseData.getCicCase().setApplicantFullName("appFullName");
            caseData.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.EMAIL);
            caseData.getCicCase().setApplicantEmailAddress("test@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class),
                eq(TEST_CASE_ID.toString()),
                eq(Party.APPLICANT))).thenReturn(NOTIFICATION_RESPONSE);

            String correspondenceId = contactPartiesNotification.sendToApplicant(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.APPLICANT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(caseData.getCicCase().getApplicantEmailAddress()),
                eq(expectedAttachedDocuments),
                eq(new HashMap<>()),
                templateVarsCaptor.capture(),
                eq(TemplateName.CONTACT_PARTIES_EMAIL));
            assertThat(cicCase.getAppNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldNotifyApplicantByLetterWhenPreferenceIsNotEmail() {
            //given
            cicCase.setApplicantContactDetailsPreference(ContactPreferenceType.POST);
            cicCase.setApplicantAddress(TEST_ADDRESS);

            when(notificationHelper.getApplicantCommonVars(TEST_CASE_ID.toString(), caseData)).thenReturn(new HashMap<String, Object>());
            when(notificationHelper.buildLetterNotificationRequest(anyMap(), eq(CONTACT_PARTIES_POST)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(
                NOTIFICATION_RESPONSE);

            //when
            String correspondenceId = contactPartiesNotification.sendToApplicant(caseData, TEST_CASE_ID.toString());

            //then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationHelper).addAddressTemplateVars(eq(TEST_ADDRESS), anyMap());
            verify(notificationService, never()).sendEmail(any(), any(), any());
            assertThat(cicCase.getAppLetterNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldNotifyRepresentativeByEmailWithCorrectPartyAndWriteBack() {
            //Given
            caseData.getCicCase().setRepresentativeFullName("repFullName");
            caseData.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
            caseData.getCicCase().setRepresentativeEmailAddress("test@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class),
                eq(TEST_CASE_ID.toString()),
                eq(Party.REPRESENTATIVE))).thenReturn(NOTIFICATION_RESPONSE);

            String correspondenceId = contactPartiesNotification.sendToRepresentative(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.REPRESENTATIVE));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(caseData.getCicCase().getRepresentativeEmailAddress()),
                eq(expectedAttachedDocuments),
                eq(new HashMap<>()),
                templateVarsCaptor.capture(),
                eq(TemplateName.CONTACT_PARTIES_EMAIL));
            assertThat(cicCase.getRepNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldNotifyRepresentativeByLetterWhenPreferenceIsNotEmail() {
            //given
            cicCase.setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
            cicCase.setRepresentativeAddress(TEST_ADDRESS);

            when(notificationHelper.getRepresentativeCommonVars(TEST_CASE_ID.toString(), caseData)).thenReturn(new HashMap<>());
            when(notificationHelper.buildLetterNotificationRequest(anyMap(), eq(CONTACT_PARTIES_POST)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(
                NOTIFICATION_RESPONSE);

            //when
            String correspondenceId = contactPartiesNotification.sendToRepresentative(caseData, TEST_CASE_ID.toString());

            //then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationHelper).addAddressTemplateVars(eq(TEST_ADDRESS), anyMap());
            verify(notificationService, never()).sendEmail(any(), any(), any());
            assertThat(cicCase.getRepLetterNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        @Test
        void shouldNotifyRespondentByEmailOnlyRegardlessOfPreference() {
            //given
            cicCase.setRespondentEmail("respondent@example.com");

            when(notificationHelper.getRespondentCommonVars(TEST_CASE_ID.toString(), caseData)).thenReturn(new HashMap<>());
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT)))
                .thenReturn(NOTIFICATION_RESPONSE);

            //when
            String correspondenceId = contactPartiesNotification.sendToRespondent(caseData, TEST_CASE_ID.toString());

            //then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT));
            verify(notificationHelper).buildEmailNotificationRequest(eq(caseData.getCicCase().getRespondentEmail()),
                eq(expectedAttachedDocuments),
                eq(new HashMap<>()),
                templateVarsCaptor.capture(),
                eq(TemplateName.CONTACT_PARTIES_EMAIL));
            assertThat(cicCase.getResNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
        }

        //
        @Test
        void shouldNotifyTribunalAndWriteToTribunalField() {
            //given
            when(notificationHelper.getTribunalCommonVars(TEST_CASE_ID.toString(), caseData)).thenReturn(new HashMap<>());
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.TRIBUNAL)))
                .thenReturn(NOTIFICATION_RESPONSE);

            //when
            String correspondenceId = contactPartiesNotification.sendToTribunal(caseData, TEST_CASE_ID.toString());

            //then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationHelper).buildEmailNotificationRequest(eq(TRIBUNAL_EMAIL_VALUE),
                eq(expectedAttachedDocuments),
                eq(new HashMap<>()),
                templateVarsCaptor.capture(),
                eq(TemplateName.CONTACT_PARTIES_EMAIL));
            assertThat(cicCase.getTribunalNotificationResponse()).isEqualTo(NOTIFICATION_RESPONSE);
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
        void shouldNotifySubjectByEmailWithCorrectPartyAndWriteBack() {
            //Given
            caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            caseData.getCicCase().setEmail("test@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = contactPartiesNotification.sendToSubject(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(caseData.getCicCase().getEmail()),
                eq(expectedAttachedDocuments),
                eq(new HashMap<>()),
                templateVarsCaptor.capture(),
                eq(TemplateName.CONTACT_PARTIES_EMAIL_NEW_CD));

            Map<String, Object> actualTemplateVars = templateVarsCaptor.getValue();
            assertThat(actualTemplateVars)
                .containsEntry(CommonConstants.CONTACT_PARTY_INFO, "message")
                .containsEntry(CommonConstants.CIC_CASE_SUBJECT_NAME, "fullName")
                .containsEntry(CommonConstants.DASHBOARD_KEY, "https://frontend.url/dashboard");
            assertThat(cicCase.getSubjectNotifyList()).isEqualTo(NOTIFICATION_RESPONSE);
        }
    }
}
