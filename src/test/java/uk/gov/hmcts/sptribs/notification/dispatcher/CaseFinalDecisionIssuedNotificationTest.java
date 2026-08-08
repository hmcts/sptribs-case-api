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
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CaseStay;
import uk.gov.hmcts.sptribs.caseworker.model.NoticeOption;
import uk.gov.hmcts.sptribs.caseworker.model.ReinstateReason;
import uk.gov.hmcts.sptribs.caseworker.model.StayReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
public class CaseFinalDecisionIssuedNotificationTest {

    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private CaseFinalDecisionIssuedNotification finalDecisionIssuedNotification;

    private static final NotificationResponse NOTIFICATION_RESPONSE = NotificationResponse.builder().id("123").build();

    @Nested
    class WhenCitizenDashboardDisabled {

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(finalDecisionIssuedNotification, "citizenDashboardEnabled", false);
        }

        @Test
        void shouldNotifySubjectOfCaseFinalDecisionIssuedWithEmail() {
            //Given
            final LocalDate expDate = LocalDate.now();
            final CaseData data = getMockCaseData(expDate);
            data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            data.getCicCase().setEmail("testsubject@outlook.com");

            final UUID uuid = UUID.randomUUID();
            final Document guidanceDocument = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();
            final CICDocument document = CICDocument.builder()
                .documentLink(Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build())
                .documentEmailContent("content")
                .build();

            final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
                .finalDecisionNotice(NoticeOption.UPLOAD_FROM_COMPUTER)
                .document(document)
                .finalDecisionGuidance(guidanceDocument).build();
            data.setCaseIssueFinalDecision(caseIssueFinalDecision);

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            finalDecisionIssuedNotification.sendToSubject(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getEmail(),
                new ArrayList<>(),
                Map.of(
                    CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                    CommonConstants.FINAL_DECISION_NOTICE, uuid.toString()),
                new HashMap<>(),
                TemplateName.FINAL_DECISION_ISSUED_EMAIL);
        }

        @Test
        void shouldNotifySubjectOfCaseFinalDecisionIssuedWithEmailWithoutDecisionDocument() {
            //Given
            final LocalDate expDate = LocalDate.now();
            final CaseData data = getMockCaseData(expDate);
            data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            data.getCicCase().setEmail("testsubject@outlook.com");

            final UUID uuid = UUID.randomUUID();
            final Document guidanceDocument = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();


            final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
                .finalDecisionNotice(NoticeOption.UPLOAD_FROM_COMPUTER)
                .document(null)
                .finalDecisionGuidance(guidanceDocument).build();
            data.setCaseIssueFinalDecision(caseIssueFinalDecision);

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            finalDecisionIssuedNotification.sendToSubject(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getEmail(),
                new ArrayList<>(),
                Map.of(
                    CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                    CommonConstants.FINAL_DECISION_NOTICE, ""),
                new HashMap<>(),
                TemplateName.FINAL_DECISION_ISSUED_EMAIL);
        }

        @Test
        void shouldNotifySubjectOfCaseFinalDecisionIssuedWithPost() {
            //Given
            LocalDate expDate = LocalDate.now();
            final CaseData data = getMockCaseData(expDate);
            data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
            data.getCicCase().setAddress(AddressGlobalUK.builder().build());
            data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

            //When
            when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
            when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(
                NOTIFICATION_RESPONSE);

            finalDecisionIssuedNotification.sendToSubject(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
            verify(notificationHelper).buildLetterNotificationRequest(
                new HashMap<>(),
                TemplateName.FINAL_DECISION_ISSUED_POST);
        }

        @Test
        void shouldNotifyRespondentOfCaseFinalDecisionIssuedWithEmail() {
            //Given
            LocalDate expDate = LocalDate.now();
            final CaseData data = getMockCaseData(expDate);
            data.getCicCase().setRespondentName("respondentName");
            data.getCicCase().setRespondentEmail("testrepr@outlook.com");

            final UUID uuid = UUID.randomUUID();
            final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();

            final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
                .finalDecisionDraft(document)
                .finalDecisionNotice(NoticeOption.CREATE_FROM_TEMPLATE)
                .finalDecisionGuidance(document)
                .build();
            data.setCaseIssueFinalDecision(caseIssueFinalDecision);

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRespondentCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            finalDecisionIssuedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getRespondentEmail(),
                new ArrayList<>(),
                Map.of(
                    CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                    CommonConstants.FINAL_DECISION_NOTICE, uuid.toString()),
                new HashMap<>(),
                TemplateName.FINAL_DECISION_ISSUED_EMAIL);
        }

        @Test
        void shouldNotifyRespondentOfCaseFinalDecisionIssuedWithEmailWithoutDecisionNotice() {
            //Given
            LocalDate expDate = LocalDate.now();
            final CaseData data = getMockCaseData(expDate);
            data.getCicCase().setRespondentName("respondentName");
            data.getCicCase().setRespondentEmail("testrepr@outlook.com");

            final UUID uuid = UUID.randomUUID();
            final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();

            final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
                .finalDecisionDraft(document)
                .finalDecisionNotice(null)
                .finalDecisionGuidance(document)
                .build();
            data.setCaseIssueFinalDecision(caseIssueFinalDecision);

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRespondentCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            finalDecisionIssuedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getRespondentEmail(),
                new ArrayList<>(),
                Map.of(
                    CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                    CommonConstants.FINAL_DECISION_NOTICE, ""),
                new HashMap<>(),
                TemplateName.FINAL_DECISION_ISSUED_EMAIL);
        }

        @Test
        void shouldNotifyRepresentativeOfCaseFinalDecisionIssuedWithEmail() {
            //Given
            LocalDate expDate = LocalDate.now();
            final CaseData data = getMockCaseData(expDate);
            data.getCicCase().setRepresentativeFullName("repFullName");
            data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
            data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");
            data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

            final UUID uuid = UUID.randomUUID();
            final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();

            final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
                .finalDecisionDraft(document)
                .finalDecisionNotice(NoticeOption.CREATE_FROM_TEMPLATE)
                .finalDecisionGuidance(document)
                .build();
            data.setCaseIssueFinalDecision(caseIssueFinalDecision);

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            finalDecisionIssuedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.REPRESENTATIVE));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getRepresentativeEmailAddress(),
                new ArrayList<>(),
                Map.of(
                    CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                    CommonConstants.FINAL_DECISION_NOTICE, uuid.toString()),
                new HashMap<>(),
                TemplateName.FINAL_DECISION_ISSUED_EMAIL);
        }

        @Test
        void shouldNotifyRepresentativeOfCaseFinalDecisionIssuedWithEmailWithoutDecisionDraft() {
            //Given
            LocalDate expDate = LocalDate.now();
            final CaseData data = getMockCaseData(expDate);
            data.getCicCase().setRepresentativeFullName("repFullName");
            data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
            data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");
            data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

            final UUID uuid = UUID.randomUUID();
            final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();

            final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
                .finalDecisionDraft(null)
                .finalDecisionNotice(NoticeOption.CREATE_FROM_TEMPLATE)
                .finalDecisionGuidance(document)
                .build();
            data.setCaseIssueFinalDecision(caseIssueFinalDecision);


            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            finalDecisionIssuedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.REPRESENTATIVE));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getRepresentativeEmailAddress(),
                new ArrayList<>(),
                Map.of(
                    CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                    CommonConstants.FINAL_DECISION_NOTICE, ""),
                new HashMap<>(),
                TemplateName.FINAL_DECISION_ISSUED_EMAIL);
        }

        @Test
        void shouldNotifyRepresentativeOfCaseFinalDecisionIssuedWithPost() {
            //Given
            LocalDate expDate = LocalDate.now();
            final CaseData data = getMockCaseData(expDate);
            data.getCicCase().setRepresentativeFullName("repFullName");
            data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
            data.getCicCase().setRepresentativeAddress(AddressGlobalUK.builder().build());

            //When
            when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            doNothing().when(notificationHelper).addAddressTemplateVars(any(), anyMap());
            when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(
                NOTIFICATION_RESPONSE);

            finalDecisionIssuedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
            verify(notificationHelper).buildLetterNotificationRequest(
                new HashMap<>(),
                TemplateName.FINAL_DECISION_ISSUED_POST);
        }

        @Test
        void shouldNotifyApplicantOfCaseFinalDecisionIssuedWithEmail() {
            //Given
            LocalDate expDate = LocalDate.now();
            final CaseData data = getMockCaseData(expDate);
            data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.EMAIL);
            data.getCicCase().setApplicantEmailAddress("testapp@outlook.com");

            final UUID uuid = UUID.randomUUID();
            final Document guidanceDocument = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();
            final CICDocument document = CICDocument.builder()
                .documentLink(Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build())
                .documentEmailContent("content")
                .build();

            final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
                .finalDecisionNotice(NoticeOption.UPLOAD_FROM_COMPUTER)
                .document(document)
                .finalDecisionGuidance(guidanceDocument).build();
            data.setCaseIssueFinalDecision(caseIssueFinalDecision);


            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            finalDecisionIssuedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.APPLICANT));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getApplicantEmailAddress(),
                new ArrayList<>(),
                Map.of(
                    CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                    CommonConstants.FINAL_DECISION_NOTICE, uuid.toString()),
                new HashMap<>(),
                TemplateName.FINAL_DECISION_ISSUED_EMAIL);
        }

        @Test
        void shouldNotifyApplicantOfCaseFinalDecisionIssuedWithPost() {
            //Given
            LocalDate expDate = LocalDate.now();
            final CaseData data = getMockCaseData(expDate);
            data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.POST);
            data.getCicCase().setApplicantAddress(AddressGlobalUK.builder().build());
            data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

            //When
            when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
            when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(
                NOTIFICATION_RESPONSE);

            finalDecisionIssuedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
            verify(notificationHelper).buildLetterNotificationRequest(
                new HashMap<>(),
                TemplateName.FINAL_DECISION_ISSUED_POST);
        }
    }

    @Nested
    class WhenCitizenDashboardEnabled {

        @Captor
        private ArgumentCaptor<Map<String, Object>> templateVarsCaptor;

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(finalDecisionIssuedNotification, "citizenDashboardEnabled", true);
            ReflectionTestUtils.setField(finalDecisionIssuedNotification, "citizenDashboardUrl", "https://sptribs.frontend/dashboard");
        }

        @Test
        void shouldNotifySubjectOfCaseFinalDecisionIssuedWithEmail() {
            //Given
            final LocalDate expDate = LocalDate.now();
            final CaseData data = getMockCaseData(expDate);
            data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            data.getCicCase().setEmail("testsubject@outlook.com");

            final UUID uuid = UUID.randomUUID();
            final Document guidanceDocument = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();
            final CICDocument document = CICDocument.builder()
                .documentLink(Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build())
                .documentEmailContent("content")
                .build();

            final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
                .finalDecisionNotice(NoticeOption.UPLOAD_FROM_COMPUTER)
                .document(document)
                .finalDecisionGuidance(guidanceDocument).build();
            data.setCaseIssueFinalDecision(caseIssueFinalDecision);

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            finalDecisionIssuedNotification.sendToSubject(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
            verify(notificationHelper).buildEmailNotificationRequest(eq(data.getCicCase().getEmail()),
                eq(new ArrayList<>()),
                eq(Map.of(CommonConstants.FINAL_DECISION_GUIDANCE,
                    uuid.toString(),
                    CommonConstants.FINAL_DECISION_NOTICE,
                    uuid.toString())),
                templateVarsCaptor.capture(),
                eq(TemplateName.FINAL_DECISION_ISSUED_EMAIL_NEW_CD));

            assertThat(templateVarsCaptor.getValue())
                .containsEntry(CommonConstants.DASHBOARD_KEY, "https://sptribs.frontend/dashboard");
        }
    }

    private CaseData getMockCaseData(LocalDate stayCaseExpDate) {
        final CicCase cicCase = CicCase.builder()
            .fullName("fullName").caseNumber(TEST_CASE_ID.toString())
            .build();
        final CaseStay caseStay = CaseStay.builder()
            .expirationDate(stayCaseExpDate)
            .stayReason(StayReason.OTHER)
            .additionalDetail("addlDetail")
            .build();
        return CaseData.builder()
            .cicCase(cicCase)
            .caseStay(caseStay)
            .build();
    }
}
