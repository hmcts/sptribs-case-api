package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.NoticeOption;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.util.HashMap;
import java.util.UUID;

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

    private static final NotificationResponse NOTIFICATION_RESPONSE = NotificationResponse.builder().id("123").build();

    @Test
    void shouldNotifySubjectOfDecisionIssuedWithEmailWithUploadedDocument() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testrepr@outlook.com");

        final UUID uuid = UUID.randomUUID();
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build())
            .documentEmailContent("content")
            .build();
        final CaseIssueDecision caseIssueDecision = CaseIssueDecision.builder()
            .decisionDocument(document)
            .decisionNotice(NoticeOption.UPLOAD_FROM_COMPUTER).build();
        data.setCaseIssueDecision(caseIssueDecision);

        //When
        when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        when(notificationService.sendEmail(any(NotificationRequest.class),
            eq(TEST_CASE_ID.toString()),
            any(Party.class))).thenReturn(NOTIFICATION_RESPONSE);

        decisionIssuedNotification.sendToSubject(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
        verify(notificationHelper).buildEmailNotificationRequest(
            eq(data.getCicCase().getEmail()),
            anyList(),
            anyMap(),
            anyMap(),
            eq(TemplateName.DECISION_ISSUED_EMAIL));
    }

    @Test
    void shouldNotifySubjectOfDecisionIssuedWithEmailWithNoUploadedDocument() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testSubject@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        when(notificationService.sendEmail(any(NotificationRequest.class),
            eq(TEST_CASE_ID.toString()),
            any(Party.class))).thenReturn(NOTIFICATION_RESPONSE);

        decisionIssuedNotification.sendToSubject(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
        verify(notificationHelper).buildEmailNotificationRequest(
            eq(data.getCicCase().getEmail()),
            anyList(),
            anyMap(),
            anyMap(),
            eq(TemplateName.DECISION_ISSUED_EMAIL));
    }

    @Test
    void shouldNotifySubjectOfDecisionIssuedWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setAddress(AddressGlobalUK.builder()
                .addressLine1("test addr1")
                .addressLine2("test addr2")
                .addressLine3("test addr3")
                .postCode("test postcode")
                .country("test county")
                .postTown("test postTown")
            .build());

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(NOTIFICATION_RESPONSE);

        decisionIssuedNotification.sendToSubject(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildLetterNotificationRequest(
            new HashMap<>(),
            TemplateName.DECISION_ISSUED_POST);
    }

    @Test
    void shouldNotifyRespondentOfDecisionIssuedWithEmailWithUploadDocument() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRespondentName("respondentName");
        data.getCicCase().setRespondentEmail("testRespodent@outlook.com");
        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();

        final CaseIssueDecision caseIssueDecision = CaseIssueDecision.builder()
            .issueDecisionDraft(document)
            .decisionNotice(NoticeOption.CREATE_FROM_TEMPLATE)
            .build();
        data.setCaseIssueDecision(caseIssueDecision);

        //When
        when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRespondentCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        when(notificationService.sendEmail(any(NotificationRequest.class),
            eq(TEST_CASE_ID.toString()),
            any(Party.class))).thenReturn(NOTIFICATION_RESPONSE);

        decisionIssuedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT));
        verify(notificationHelper).buildEmailNotificationRequest(
            eq(data.getCicCase().getRespondentEmail()),
            anyList(),
            anyMap(),
            anyMap(),
            eq(TemplateName.DECISION_ISSUED_EMAIL));
    }

    @Test
    void shouldNotifyRespondentOfDecisionIssuedWithEmailWithNoUploadedDocument() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRespondentEmail("testRespondent@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRespondentCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        when(notificationService.sendEmail(any(NotificationRequest.class),
            eq(TEST_CASE_ID.toString()),
            any(Party.class))).thenReturn(NOTIFICATION_RESPONSE);

        decisionIssuedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT));
        verify(notificationHelper).buildEmailNotificationRequest(
            eq(data.getCicCase().getRespondentEmail()),
            anyList(),
            anyMap(),
            anyMap(),
            eq(TemplateName.DECISION_ISSUED_EMAIL));
    }


    @Test
    void shouldNotifyRepresentativeOfDecisionIssuedWithEmailWithUploadedDocument() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");

        final UUID uuid = UUID.randomUUID();
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build())
            .documentEmailContent("content")
            .build();
        final CaseIssueDecision caseIssueDecision = CaseIssueDecision.builder()
            .decisionDocument(document)
            .decisionNotice(NoticeOption.UPLOAD_FROM_COMPUTER)
            .build();
        data.setCaseIssueDecision(caseIssueDecision);

        //When
        when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        when(notificationService.sendEmail(any(NotificationRequest.class),
            eq(TEST_CASE_ID.toString()),
            any(Party.class))).thenReturn(NOTIFICATION_RESPONSE);

        decisionIssuedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class),
            eq(TEST_CASE_ID.toString()),
            eq(Party.REPRESENTATIVE));
        verify(notificationHelper).buildEmailNotificationRequest(
            eq(data.getCicCase().getRepresentativeEmailAddress()),
            anyList(),
            anyMap(),
            anyMap(),
            eq(TemplateName.DECISION_ISSUED_EMAIL));
    }

    @Test
    void shouldNotifyRepresentativeOfDecisionIssuedWithEmailWithNoUploadedDocument() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        when(notificationService.sendEmail(any(NotificationRequest.class),
            eq(TEST_CASE_ID.toString()),
            any(Party.class))).thenReturn(NOTIFICATION_RESPONSE);


        decisionIssuedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class),
            eq(TEST_CASE_ID.toString()),
            eq(Party.REPRESENTATIVE));
        verify(notificationHelper).buildEmailNotificationRequest(
            eq(data.getCicCase().getRepresentativeEmailAddress()),
            anyList(),
            anyMap(),
            anyMap(),
            eq(TemplateName.DECISION_ISSUED_EMAIL));
    }

    @Test
    void shouldNotifyRepresentativeOfDecisionIssuedWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setRepresentativeAddress(AddressGlobalUK.builder().build());

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(NOTIFICATION_RESPONSE);

        decisionIssuedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildLetterNotificationRequest(
            new HashMap<>(),
            TemplateName.DECISION_ISSUED_POST);
    }

    @Test
    void shouldNotifyApplicantOfDecisionIssuedWithEmailWithUploadedDocument() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setApplicantEmailAddress("testApplicant@outlook.com");

        final UUID uuid = UUID.randomUUID();
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build())
            .documentEmailContent("content")
            .build();
        final CaseIssueDecision caseIssueDecision = CaseIssueDecision.builder()
            .decisionDocument(document)
            .decisionNotice(NoticeOption.UPLOAD_FROM_COMPUTER).build();
        data.setCaseIssueDecision(caseIssueDecision);

        //When
        when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        when(notificationService.sendEmail(any(NotificationRequest.class),
            eq(TEST_CASE_ID.toString()),
            any(Party.class))).thenReturn(NOTIFICATION_RESPONSE);

        decisionIssuedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.APPLICANT));
        verify(notificationHelper).buildEmailNotificationRequest(
            eq(data.getCicCase().getApplicantEmailAddress()),
            anyList(),
            anyMap(),
            anyMap(),
            eq(TemplateName.DECISION_ISSUED_EMAIL));
    }

    @Test
    void shouldNotifyApplicantOfDecisionIssuedWithEmailWithNoUploadedDocument() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setApplicantEmailAddress("testApplicant@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        when(notificationService.sendEmail(any(NotificationRequest.class),
            eq(TEST_CASE_ID.toString()),
            any(Party.class))).thenReturn(NOTIFICATION_RESPONSE);

        decisionIssuedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.APPLICANT));
        verify(notificationHelper).buildEmailNotificationRequest(
            eq(data.getCicCase().getApplicantEmailAddress()),
            anyList(),
            anyMap(),
            anyMap(),
            eq(TemplateName.DECISION_ISSUED_EMAIL));
    }

    @Test
    void shouldNotifyApplicantOfDecisionIssuedWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setApplicantAddress(AddressGlobalUK.builder().build());

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(NOTIFICATION_RESPONSE);

        decisionIssuedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildLetterNotificationRequest(
            new HashMap<>(),
            TemplateName.DECISION_ISSUED_POST);
    }

    private CaseData getMockCaseData() {
        CicCase cicCase = CicCase.builder()
            .fullName("fullName").caseNumber(TEST_CASE_ID.toString())
            .build();

        return CaseData.builder().cicCase(cicCase).build();
    }
}
