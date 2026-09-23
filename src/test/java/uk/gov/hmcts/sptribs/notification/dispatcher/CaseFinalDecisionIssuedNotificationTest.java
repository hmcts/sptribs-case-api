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
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.document.DocumentUtil;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class CaseFinalDecisionIssuedNotificationTest {

    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private CaseFinalDecisionIssuedNotification finalDecisionIssuedNotification;

    @Captor
    private ArgumentCaptor<Map<String, Object>> templateVarsCaptor;

    @Captor
    private ArgumentCaptor<List<CaseworkerCICDocument>> selectedDocumentsCaptor;

    @Nested
    class WhenCitizenDashboardDisabled {
        @BeforeEach
        void setUpFlags() {
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
            final Document decisionDocument = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();
            final CICDocument document = CICDocument.builder()
                .documentLink(decisionDocument)
                .documentEmailContent("content")
                .build();

            final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
                .finalDecisionNotice(NoticeOption.UPLOAD_FROM_COMPUTER)
                .document(document)
                .finalDecisionGuidance(guidanceDocument).build();
            data.setCaseIssueFinalDecision(caseIssueFinalDecision);

            //When
            when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());

            finalDecisionIssuedNotification.sendToSubject(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getEmail(),
                true,
                Map.of(
                    CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                    CommonConstants.FINAL_DECISION_NOTICE, uuid.toString()),
                new HashMap<>(),
                TemplateName.FINAL_DECISION_ISSUED_EMAIL);

            List<Document> documents = selectedDocumentsCaptor.getValue().stream().map(CaseworkerCICDocument::getDocumentLink).toList();
            assertThat(documents).contains(decisionDocument, guidanceDocument);
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
            when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());

            finalDecisionIssuedNotification.sendToSubject(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getEmail(),
                true,
                Map.of(
                    CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                    CommonConstants.FINAL_DECISION_NOTICE, ""),
                new HashMap<>(),
                TemplateName.FINAL_DECISION_ISSUED_EMAIL);

            assertThat(selectedDocumentsCaptor.getValue().getFirst().getDocumentLink()).isEqualTo(guidanceDocument);
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

            Document decisionDocument = TestDataHelper.getDocumentWithBinary("decisionDocument");
            Document guidanceDocument = TestDataHelper.getDocumentWithBinary("guidanceDocument");

            final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
                .finalDecisionDraft(decisionDocument)
                .finalDecisionNotice(NoticeOption.CREATE_FROM_TEMPLATE)
                .finalDecisionGuidance(guidanceDocument)
                .build();
            data.setCaseIssueFinalDecision(caseIssueFinalDecision);

            //When
            when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRespondentCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            finalDecisionIssuedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

            String decisionDocumentUuid = DocumentUtil.getUuid(decisionDocument);
            String guidanceDocumentUuid = DocumentUtil.getUuid(guidanceDocument);
            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getRespondentEmail(),
                true,
                Map.of(
                    CommonConstants.FINAL_DECISION_GUIDANCE, guidanceDocumentUuid,
                    CommonConstants.FINAL_DECISION_NOTICE, decisionDocumentUuid),
                new HashMap<>(),
                TemplateName.FINAL_DECISION_ISSUED_EMAIL);
            assertThat(selectedDocumentsCaptor.getValue()).hasSize(2);
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
            when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRespondentCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            finalDecisionIssuedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getRespondentEmail(),
                true,
                Map.of(
                    CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                    CommonConstants.FINAL_DECISION_NOTICE, ""),
                new HashMap<>(),
                TemplateName.FINAL_DECISION_ISSUED_EMAIL);
            assertThat(selectedDocumentsCaptor.getValue().getFirst().getDocumentLink()).isEqualTo(document);
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

            Document decisionDocument = TestDataHelper.getDocumentWithBinary("decisionDocument");
            Document guidanceDocument = TestDataHelper.getDocumentWithBinary("guidanceDocument");

            final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
                .finalDecisionDraft(decisionDocument)
                .finalDecisionNotice(NoticeOption.CREATE_FROM_TEMPLATE)
                .finalDecisionGuidance(guidanceDocument)
                .build();
            data.setCaseIssueFinalDecision(caseIssueFinalDecision);


            //When
            when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());

            finalDecisionIssuedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            String decisionDocumentUuid = DocumentUtil.getUuid(decisionDocument);
            String guidanceDocumentUuid = DocumentUtil.getUuid(guidanceDocument);
            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getRepresentativeEmailAddress(),
                true,
                Map.of(
                    CommonConstants.FINAL_DECISION_GUIDANCE, guidanceDocumentUuid,
                    CommonConstants.FINAL_DECISION_NOTICE, decisionDocumentUuid),
                new HashMap<>(),
                TemplateName.FINAL_DECISION_ISSUED_EMAIL);

            assertThat(selectedDocumentsCaptor.getValue()).hasSize(2);
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
            when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());

            finalDecisionIssuedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getRepresentativeEmailAddress(),
                true,
                Map.of(
                    CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                    CommonConstants.FINAL_DECISION_NOTICE, ""),
                new HashMap<>(),
                TemplateName.FINAL_DECISION_ISSUED_EMAIL);

            assertThat(selectedDocumentsCaptor.getValue()).hasSize(1);
            assertThat(selectedDocumentsCaptor.getValue().getFirst().getDocumentLink()).isEqualTo(document);
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
            data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            data.getCicCase().setApplicantEmailAddress("testapp@outlook.com");

            Document decisionDocument = TestDataHelper.getDocumentWithBinary("decisionDocument");
            final CICDocument document = CICDocument.builder()
                .documentLink(decisionDocument)
                .documentEmailContent("content")
                .build();

            Document guidanceDocument = TestDataHelper.getDocumentWithBinary("guidanceDocument");

            final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
                .finalDecisionNotice(NoticeOption.UPLOAD_FROM_COMPUTER)
                .document(document)
                .finalDecisionGuidance(guidanceDocument).build();
            data.setCaseIssueFinalDecision(caseIssueFinalDecision);


            //When
            when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());

            finalDecisionIssuedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

            String decisionDocumentUuid = DocumentUtil.getUuid(decisionDocument);
            String guidanceDocumentUuid = DocumentUtil.getUuid(guidanceDocument);
            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                data.getCicCase().getApplicantEmailAddress(),
                true,
                Map.of(
                    CommonConstants.FINAL_DECISION_GUIDANCE, guidanceDocumentUuid,
                    CommonConstants.FINAL_DECISION_NOTICE, decisionDocumentUuid),
                new HashMap<>(),
                TemplateName.FINAL_DECISION_ISSUED_EMAIL);

            assertThat(selectedDocumentsCaptor.getValue()).hasSize(2);
            assertThat(selectedDocumentsCaptor.getValue().stream().map(CaseworkerCICDocument::getDocumentLink).toList())
                .contains(guidanceDocument, decisionDocument);
        }

        @Test
        void shouldNotifyApplicantOfCaseFinalDecisionIssuedWithPost() {
            //Given
            LocalDate expDate = LocalDate.now();
            final CaseData data = getMockCaseData(expDate);
            data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
            data.getCicCase().setApplicantAddress(AddressGlobalUK.builder().build());
            data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

            //When
            when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
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
        @BeforeEach
        void setUpFlags() {
            ReflectionTestUtils.setField(finalDecisionIssuedNotification, "citizenDashboardEnabled", true);
            ReflectionTestUtils.setField(finalDecisionIssuedNotification, "citizenDashboardUrl", "https://frontend.url/dashboard");
        }

        @Test
        void shouldNotifySubjectOfCaseFinalDecisionIssuedWithEmail() {
            //Given
            final LocalDate expDate = LocalDate.now();
            final CaseData data = getMockCaseData(expDate);
            data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            data.getCicCase().setEmail("testsubject@outlook.com");

            Document decisionDocument = TestDataHelper.getDocumentWithBinary("decisionDocument");
            final CICDocument document = CICDocument.builder()
                .documentLink(decisionDocument)
                .documentEmailContent("content")
                .build();
            Document guidanceDocument = TestDataHelper.getDocumentWithBinary("guidanceDocument");

            final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
                .finalDecisionNotice(NoticeOption.UPLOAD_FROM_COMPUTER)
                .document(document)
                .finalDecisionGuidance(guidanceDocument).build();
            data.setCaseIssueFinalDecision(caseIssueFinalDecision);

            //When
            when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());

            finalDecisionIssuedNotification.sendToSubject(data, TEST_CASE_ID.toString());

            String decisionDocumentUuid = DocumentUtil.getUuid(decisionDocument);
            String guidanceDocumentUuid = DocumentUtil.getUuid(guidanceDocument);
            //Then
            verify(notificationService).sendEmail(any(NotificationRequest.class), selectedDocumentsCaptor.capture(),
                eq(TEST_CASE_ID.toString()), eq(null));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(data.getCicCase().getEmail()),
                eq(true),
                eq(Map.of(
                    CommonConstants.FINAL_DECISION_GUIDANCE, guidanceDocumentUuid,
                    CommonConstants.FINAL_DECISION_NOTICE, decisionDocumentUuid)),
                templateVarsCaptor.capture(),
                eq(TemplateName.FINAL_DECISION_ISSUED_EMAIL_NEW_CD));

            List<Document> documents = selectedDocumentsCaptor.getValue().stream().map(CaseworkerCICDocument::getDocumentLink).toList();
            assertThat(documents).contains(decisionDocument, guidanceDocument);
            assertThat(templateVarsCaptor.getValue()).containsKey(DASHBOARD_KEY);
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
