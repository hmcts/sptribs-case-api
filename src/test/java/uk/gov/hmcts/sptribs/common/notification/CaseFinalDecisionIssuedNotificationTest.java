package uk.gov.hmcts.sptribs.common.notification;

import org.elasticsearch.core.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CaseFinalDecisionIssuedNotificationTest {

    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private CaseFinalDecisionIssuedNotification finalDecisionIssuedNotification;

    @Test
    void shouldNotifySubjectOfCaseFinalDecisionIssuedWithEmail() {
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

        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        finalDecisionIssuedNotification.sendToSubject(data, "CN1");

        verify(notificationService).sendEmailNew(any(NotificationRequest.class), any(), any(CaseData.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getEmail(),
            true,
            Map.of(
                CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                CommonConstants.FINAL_DECISION_NOTICE, uuid.toString()),
            new HashMap<>(),
            TemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
    }

    @Test
    void shouldNotifySubjectOfCaseFinalDecisionIssuedWithEmailWithoutDecisionDocument() {
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

        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        finalDecisionIssuedNotification.sendToSubject(data, "CN1");

        verify(notificationService).sendEmailNew(any(NotificationRequest.class), any(), any(CaseData.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getEmail(),
            true,
            Map.of(
                CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                CommonConstants.FINAL_DECISION_NOTICE, ""),
            new HashMap<>(),
            TemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
    }

    @Test
    void shouldNotifySubjectOfCaseFinalDecisionIssuedWithPost() {
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setAddress(AddressGlobalUK.builder().build());
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        finalDecisionIssuedNotification.sendToSubject(data, "CN1");

        verify(notificationService).sendLetterNew(any(NotificationRequest.class), any(), any(CaseData.class));
        verify(notificationHelper).buildLetterNotificationRequest(
            new HashMap<>(),
            TemplateName.CASE_FINAL_DECISION_ISSUED_POST);
    }

    @Test
    void shouldNotifyRespondentOfCaseFinalDecisionIssuedWithEmail() {
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

        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRespondentCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        finalDecisionIssuedNotification.sendToRespondent(data, "CN1");

        verify(notificationService).sendEmailNew(any(NotificationRequest.class), any(), any(CaseData.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getRespondentEmail(),
            true,
            Map.of(
                CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                CommonConstants.FINAL_DECISION_NOTICE, uuid.toString()),
            new HashMap<>(),
            TemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
    }

    @Test
    void shouldNotifyRespondentOfCaseFinalDecisionIssuedWithEmailWithoutDecisionNotice() {
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

        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRespondentCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        finalDecisionIssuedNotification.sendToRespondent(data, "CN1");

        //Then
        verify(notificationService).sendEmailNew(any(NotificationRequest.class), any(), any(CaseData.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getRespondentEmail(),
            true,
            Map.of(
                CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                CommonConstants.FINAL_DECISION_NOTICE, ""),
            new HashMap<>(),
            TemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
    }

    @Test
    void shouldNotifyRepresentativeOfCaseFinalDecisionIssuedWithEmail() {
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

        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        finalDecisionIssuedNotification.sendToRepresentative(data, "CN1");

        verify(notificationService).sendEmailNew(any(NotificationRequest.class), any(), any(CaseData.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getRepresentativeEmailAddress(),
            true,
            Map.of(
                CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                CommonConstants.FINAL_DECISION_NOTICE, uuid.toString()),
            new HashMap<>(),
            TemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
    }

    @Test
    void shouldNotifyRepresentativeOfCaseFinalDecisionIssuedWithEmailWithoutDecisionDraft() {
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

        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        finalDecisionIssuedNotification.sendToRepresentative(data, "CN1");

        verify(notificationService).sendEmailNew(any(NotificationRequest.class), any(), any(CaseData.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getRepresentativeEmailAddress(),
            true,
            Map.of(
                CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                CommonConstants.FINAL_DECISION_NOTICE, ""),
            new HashMap<>(),
            TemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
    }

    @Test
    void shouldNotifyRepresentativeOfCaseFinalDecisionIssuedWithPost() {
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setRepresentativeAddress(AddressGlobalUK.builder().build());

        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(), anyMap());
        finalDecisionIssuedNotification.sendToRepresentative(data, "CN1");

        verify(notificationService).sendLetterNew(any(NotificationRequest.class), any(), any(CaseData.class));
        verify(notificationHelper).buildLetterNotificationRequest(
            new HashMap<>(),
            TemplateName.CASE_FINAL_DECISION_ISSUED_POST);
    }

    @Test
    void shouldNotifyApplicantOfCaseFinalDecisionIssuedWithEmail() {
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
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

        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        finalDecisionIssuedNotification.sendToApplicant(data, "CN1");

        verify(notificationService).sendEmailNew(any(NotificationRequest.class), any(), any(CaseData.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getApplicantEmailAddress(),
            true,
            Map.of(
                CommonConstants.FINAL_DECISION_GUIDANCE, uuid.toString(),
                CommonConstants.FINAL_DECISION_NOTICE, uuid.toString()),
            new HashMap<>(),
            TemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
    }

    @Test
    void shouldNotifyApplicantOfCaseFinalDecisionIssuedWithPost() {
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setApplicantAddress(AddressGlobalUK.builder().build());
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        finalDecisionIssuedNotification.sendToApplicant(data, "CN1");

        verify(notificationService).sendLetterNew(any(NotificationRequest.class), any(), any(CaseData.class));
        verify(notificationHelper).buildLetterNotificationRequest(
            new HashMap<>(),
            TemplateName.CASE_FINAL_DECISION_ISSUED_POST);
    }

    private CaseData getMockCaseData(LocalDate stayCaseExpDate) {
        final CicCase cicCase = CicCase.builder()
            .fullName("fullName").caseNumber("CN1")
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
