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
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.NoticeOption;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.document.DocumentUtil;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.notification.model.Party;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class DecisionIssuedNotificationTest {

    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private DecisionIssuedNotification decisionIssuedNotification;

    @Captor
    private ArgumentCaptor<Map<String, String >> templateDocumentVarsCaptor;

    private static final NotificationResponse NOTIFICATION_RESPONSE = NotificationResponse.builder().id("123").build();

    private static final AddressGlobalUK TEST_ADDRESS = AddressGlobalUK.builder()
        .addressLine1("test addr1")
        .addressLine2("test addr2")
        .addressLine3("test addr3")
        .postCode("test postcode")
        .country("test county")
        .postTown("test postTown")
        .build();

    private static final String DECISION_NOTICE_1 = "DecisionNotice1";

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = new CaseData();
        CicCase cicCase = CicCase.builder().fullName("fullName").caseNumber(TEST_CASE_ID.toString()).build();
        caseData.setCicCase(cicCase);

        CICDocument document = CICDocument.builder()
            .documentLink(TestDataHelper.getDocumentWithBinary("test-doc.pdf"))
            .documentEmailContent("content")
            .build();
        CaseIssueDecision caseIssueDecision = CaseIssueDecision.builder()
            .decisionDocument(document)
            .decisionNotice(NoticeOption.UPLOAD_FROM_COMPUTER).build();
        caseData.setCaseIssueDecision(caseIssueDecision);

    }

    @Nested
    class WhenCitizenDashboardDisabled {
        @BeforeEach
        void setUpFlags() {
            ReflectionTestUtils.setField(decisionIssuedNotification, "citizenDashboardEnabled", false);
        }

        @Test
        void shouldNotifySubjectOfDecisionIssuedWithEmailWithUploadedDocument() {
            //Given
            caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            caseData.getCicCase().setEmail("testrepr@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class),
                eq(TEST_CASE_ID.toString()),
                any(Party.class))).thenReturn(NOTIFICATION_RESPONSE);

            String correspondenceId = decisionIssuedNotification.sendToSubject(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(caseData.getCicCase().getEmail()),
                eq(new ArrayList<>()),
                templateDocumentVarsCaptor.capture(),
                eq(new HashMap<>()),
                eq(TemplateName.DECISION_ISSUED_EMAIL));
            Map<String, String> actualDocumentTemplateVars = templateDocumentVarsCaptor.getValue();
            String expectedUuid = DocumentUtil.getDocumentUuidFromCICDocument(caseData.getCaseIssueDecision().getDecisionDocument());
            assertThat(actualDocumentTemplateVars).containsEntry(DECISION_NOTICE_1, expectedUuid);
        }

        @Test
        void shouldNotifySubjectOfDecisionIssuedWithEmailWithNoUploadedDocument() {
            //Given
            caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            caseData.getCicCase().setEmail("testSubject@outlook.com");
            caseData.setCaseIssueDecision(new CaseIssueDecision());

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class),
                eq(TEST_CASE_ID.toString()),
                any(Party.class))).thenReturn(NOTIFICATION_RESPONSE);

            String correspondenceId = decisionIssuedNotification.sendToSubject(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(caseData.getCicCase().getEmail()),
                eq(new ArrayList<>()),
                templateDocumentVarsCaptor.capture(),
                eq(new HashMap<>()),
                eq(TemplateName.DECISION_ISSUED_EMAIL));

            Map<String, String> actualTemplateDocumentVars = templateDocumentVarsCaptor.getValue();
            assertThat(actualTemplateDocumentVars).containsEntry(DECISION_NOTICE_1, "");
        }

        @Test
        void shouldNotifySubjectOfDecisionIssuedWithPost() {
            //Given
            caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
            caseData.getCicCase().setAddress(TEST_ADDRESS);

            //When
            when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
            when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = decisionIssuedNotification.sendToSubject(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
            verify(notificationHelper).buildLetterNotificationRequest(
                new HashMap<>(),
                TemplateName.DECISION_ISSUED_POST);
        }

        @Test
        void shouldNotifyRespondentOfDecisionIssuedWithEmailWithUploadDocument() {
            //Given
            caseData.getCicCase().setRespondentName("respondentName");
            caseData.getCicCase().setRespondentEmail("testRespodent@outlook.com");
            final Document document = TestDataHelper.getDocumentWithBinary("test.docx");
            final CaseIssueDecision caseIssueDecision = CaseIssueDecision.builder()
                .issueDecisionDraft(document)
                .decisionNotice(NoticeOption.CREATE_FROM_TEMPLATE)
                .build();
            caseData.setCaseIssueDecision(caseIssueDecision);

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRespondentCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class),
                eq(TEST_CASE_ID.toString()),
                any(Party.class))).thenReturn(NOTIFICATION_RESPONSE);

            String correspondenceId = decisionIssuedNotification.sendToRespondent(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(caseData.getCicCase().getRespondentEmail()),
                eq(new ArrayList<>()),
                templateDocumentVarsCaptor.capture(),
                eq(new HashMap<>()),
                eq(TemplateName.DECISION_ISSUED_EMAIL));

            Map<String, String> actualTemplateDocumentVars = templateDocumentVarsCaptor.getValue();
            String uuid = DocumentUtil.getUuid(caseData.getCaseIssueDecision().getIssueDecisionDraft());
            assertThat(actualTemplateDocumentVars).containsEntry(DECISION_NOTICE_1, uuid);
        }

        @Test
        void shouldNotifyRespondentOfDecisionIssuedWithEmailWithNoUploadedDocument() {
            //Given
            caseData.getCicCase().setRespondentEmail("testRespondent@outlook.com");
            caseData.setCaseIssueDecision(new CaseIssueDecision());

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRespondentCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class),
                eq(TEST_CASE_ID.toString()),
                any(Party.class))).thenReturn(NOTIFICATION_RESPONSE);

            String correspondenceId = decisionIssuedNotification.sendToRespondent(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(caseData.getCicCase().getRespondentEmail()),
                eq(new ArrayList<>()),
                templateDocumentVarsCaptor.capture(),
                eq(new HashMap<>()),
                eq(TemplateName.DECISION_ISSUED_EMAIL));

            Map<String, String> actualTemplateDocumentVars = templateDocumentVarsCaptor.getValue();
            assertThat(actualTemplateDocumentVars).containsEntry(DECISION_NOTICE_1, "");
        }


        @Test
        void shouldNotifyRepresentativeOfDecisionIssuedWithEmailWithUploadedDocument() {
            //Given
            caseData.getCicCase().setRepresentativeFullName("repFullName");
            caseData.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
            caseData.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class),
                eq(TEST_CASE_ID.toString()),
                any(Party.class))).thenReturn(NOTIFICATION_RESPONSE);

            String correspondenceId = decisionIssuedNotification.sendToRepresentative(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class),
                eq(TEST_CASE_ID.toString()),
                eq(Party.REPRESENTATIVE));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(caseData.getCicCase().getRepresentativeEmailAddress()),
                eq(new ArrayList<>()),
                templateDocumentVarsCaptor.capture(),
                eq(new HashMap<>()),
                eq(TemplateName.DECISION_ISSUED_EMAIL));

            Map<String, String> actualTemplateDocumentVars = templateDocumentVarsCaptor.getValue();
            String uuid = DocumentUtil.getDocumentUuidFromCICDocument(caseData.getCaseIssueDecision().getDecisionDocument());
            assertThat(actualTemplateDocumentVars).containsEntry(DECISION_NOTICE_1, uuid);
        }

        @Test
        void shouldNotifyRepresentativeOfDecisionIssuedWithEmailWithNoUploadedDocument() {
            //Given
            caseData.getCicCase().setRepresentativeFullName("repFullName");
            caseData.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
            caseData.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");
            caseData.setCaseIssueDecision(new CaseIssueDecision());

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = decisionIssuedNotification.sendToRepresentative(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class),
                eq(TEST_CASE_ID.toString()),
                eq(Party.REPRESENTATIVE));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(caseData.getCicCase().getRepresentativeEmailAddress()),
                eq(new ArrayList<>()),
                templateDocumentVarsCaptor.capture(),
                eq(new HashMap<>()),
                eq(TemplateName.DECISION_ISSUED_EMAIL));

            Map<String, String> actualTemplateDocumentVars = templateDocumentVarsCaptor.getValue();
            assertThat(actualTemplateDocumentVars).containsEntry(DECISION_NOTICE_1, "");
        }

        @Test
        void shouldNotifyRepresentativeOfDecisionIssuedWithPost() {
            //Given
            caseData.getCicCase().setRepresentativeFullName("repFullName");
            caseData.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
            caseData.getCicCase().setRepresentativeAddress(AddressGlobalUK.builder().build());

            //When
            when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
            when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = decisionIssuedNotification.sendToRepresentative(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
            verify(notificationHelper).buildLetterNotificationRequest(
                new HashMap<>(),
                TemplateName.DECISION_ISSUED_POST);
        }

        @Test
        void shouldNotifyApplicantOfDecisionIssuedWithEmailWithUploadedDocument() {
            //Given
            caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            caseData.getCicCase().setApplicantEmailAddress("testApplicant@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = decisionIssuedNotification.sendToApplicant(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.APPLICANT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(caseData.getCicCase().getApplicantEmailAddress()),
                eq(new ArrayList<>()),
                templateDocumentVarsCaptor.capture(),
                eq(new HashMap<>()),
                eq(TemplateName.DECISION_ISSUED_EMAIL));

            Map<String, String> actualTemplateDocumentVars = templateDocumentVarsCaptor.getValue();
            String uuid = DocumentUtil.getDocumentUuidFromCICDocument(caseData.getCaseIssueDecision().getDecisionDocument());
            assertThat(actualTemplateDocumentVars).containsEntry(DECISION_NOTICE_1, uuid);
        }

        @Test
        void shouldNotifyApplicantOfDecisionIssuedWithEmailWithNoUploadedDocument() {
            //Given
            caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            caseData.getCicCase().setApplicantEmailAddress("testApplicant@outlook.com");
            caseData.setCaseIssueDecision(new CaseIssueDecision());

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = decisionIssuedNotification.sendToApplicant(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.APPLICANT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(caseData.getCicCase().getApplicantEmailAddress()),
                eq(new ArrayList<>()),
                templateDocumentVarsCaptor.capture(),
                eq(new HashMap<>()),
                eq(TemplateName.DECISION_ISSUED_EMAIL));

            Map<String, String> actualTemplateDocumentVars = templateDocumentVarsCaptor.getValue();
            assertThat(actualTemplateDocumentVars).containsEntry(DECISION_NOTICE_1, "");
        }

        @Test
        void shouldNotifyApplicantOfDecisionIssuedWithPost() {
            //Given
            caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
            caseData.getCicCase().setApplicantAddress(AddressGlobalUK.builder().build());

            //When
            when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
            when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = decisionIssuedNotification.sendToApplicant(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
            verify(notificationHelper).buildLetterNotificationRequest(
                new HashMap<>(),
                TemplateName.DECISION_ISSUED_POST);
        }
    }

    @Nested
    class WhenCitizenDashboardEnabled {
        @Captor
        private ArgumentCaptor<Map<String, Object >> templateVarsCaptor;

        @BeforeEach
        void setUpFlags() {
            ReflectionTestUtils.setField(decisionIssuedNotification, "citizenDashboardEnabled", true);
            ReflectionTestUtils.setField(decisionIssuedNotification, "citizenDashboardUrl", "https://frontend.url/dashboard");
        }

        @Test
        void shouldNotifySubjectOfDecisionIssuedWithEmailWithUploadedDocument() {
            //Given
            caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            caseData.getCicCase().setEmail("testrepr@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class),
                eq(TEST_CASE_ID.toString()),
                any(Party.class))).thenReturn(NOTIFICATION_RESPONSE);

            String correspondenceId = decisionIssuedNotification.sendToSubject(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(caseData.getCicCase().getEmail()),
                eq(new ArrayList<>()),
                templateDocumentVarsCaptor.capture(),
                templateVarsCaptor.capture(),
                eq(TemplateName.DECISION_ISSUED_EMAIL_NEW_CD));

            Map<String, String> actualDocumentTemplateVars = templateDocumentVarsCaptor.getValue();
            String expectedUuid = DocumentUtil.getDocumentUuidFromCICDocument(caseData.getCaseIssueDecision().getDecisionDocument());
            assertThat(actualDocumentTemplateVars).containsEntry(DECISION_NOTICE_1, expectedUuid);

            Map<String,Object> actualTemplateVars = templateVarsCaptor.getValue();
            assertThat(actualTemplateVars).containsEntry(CommonConstants.DASHBOARD_KEY, "https://frontend.url/dashboard");
        }
    }
}
