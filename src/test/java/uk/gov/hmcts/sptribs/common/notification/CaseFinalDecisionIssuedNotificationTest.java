package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.caseworker.model.ReinstateReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
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
    void shouldNotifySubjectWithEmail() {
        //Given
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testrepr@outlook.com");

        final UUID uuid = UUID.randomUUID();
        final Document guidanceDocument = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build())
            .documentEmailContent("content")
            .build();
        ListValue<CICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);

        final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder().documents(List.of(documentListValue))
            .finalDecisionGuidance(guidanceDocument).build();
        data.setCaseIssueFinalDecision(caseIssueFinalDecision);

        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyList(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        finalDecisionIssuedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));
        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifySubjectWithEmailWithNoUploadedDocument() {
        //Given
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testrepr@outlook.com");

        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();

        final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
            .finalDecisionDraft(document)
            .finalDecisionGuidance(document)
            .build();
        data.setCaseIssueFinalDecision(caseIssueFinalDecision);

        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyList(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        finalDecisionIssuedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));
        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifySubjectWithEmailThrowsException() {
        //Given
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testrepr@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();

        final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
            .finalDecisionDraft(document)
            .finalDecisionGuidance(document)
            .build();
        data.setCaseIssueFinalDecision(caseIssueFinalDecision);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyList(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        finalDecisionIssuedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));
        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifySubjectWithPost() {
        //Given
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setAddress(AddressGlobalUK.builder().build());
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        finalDecisionIssuedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendLetter();
    }

    @Test
    void shouldNotifyRespondentWithEmail() {
        //Given
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setRespondentName("respondentName");
        data.getCicCase().setRespondentEmail("testrepr@outlook.com");

        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();

        final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
            .finalDecisionDraft(document)
            .finalDecisionGuidance(document)
            .build();
        data.setCaseIssueFinalDecision(caseIssueFinalDecision);

        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyList(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRespondentCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        finalDecisionIssuedNotification.sendToRespondent(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifyRespondentWithEmailWithException() {
        //Given
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setRespondentName("respondentName");
        data.getCicCase().setRespondentEmail("testrepr@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();

        final CaseIssueFinalDecision caseIssueFinalDecision = CaseIssueFinalDecision.builder()
            .finalDecisionDraft(document)
            .finalDecisionGuidance(document)
            .build();
        data.setCaseIssueFinalDecision(caseIssueFinalDecision);

        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyList(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRespondentCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        finalDecisionIssuedNotification.sendToRespondent(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));
        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifyRepresentativeWithEmail() {
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
            .finalDecisionGuidance(document)
            .build();
        data.setCaseIssueFinalDecision(caseIssueFinalDecision);

        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyList(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        finalDecisionIssuedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifyRepresentativeWithEmailWithException() {
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
            .finalDecisionGuidance(document)
            .build();
        data.setCaseIssueFinalDecision(caseIssueFinalDecision);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyList(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        finalDecisionIssuedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));
        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifyRepresentativeWithPost() {
        //Given
        LocalDate expDate = LocalDate.now();
        final CaseData data = getMockCaseData(expDate);
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setRepresentativeAddress(AddressGlobalUK.builder().build());

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(), anyMap());
        finalDecisionIssuedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendLetter();
    }

    private CaseData getMockCaseData(LocalDate stayCaseExpDate) {
        CicCase cicCase = CicCase.builder()
            .fullName("fullName").caseNumber("CN1")
            .build();
        CaseData caseData = CaseData.builder().cicCase(cicCase).build();

        return caseData;
    }
}
