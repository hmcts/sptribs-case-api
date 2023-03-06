package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.caseworker.model.ReinstateReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
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
public class NewOrderIssuedNotificationTest {
    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private NewOrderIssuedNotification newOrderIssuedNotification;

    @Test
    void shouldNotifySubjectWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testrepr@outlook.com");

        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();
        data.getCicCase().setLastSelectedOrder(document);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        newOrderIssuedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifySubjectWithEmailWithNoUploadedDocument() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testrepr@outlook.com");

        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();
        data.getCicCase().setLastSelectedOrder(document);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        newOrderIssuedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifySubjectWithEmailThrowsException() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testrepr@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();
        data.getCicCase().setLastSelectedOrder(document);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        newOrderIssuedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifySubjectWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setAddress(AddressGlobalUK.builder().build());
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        newOrderIssuedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyRespondentWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRespondentName("respondentName");
        data.getCicCase().setRespondentEmail("testrepr@outlook.com");

        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();
        data.getCicCase().setLastSelectedOrder(document);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRespondentCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        newOrderIssuedNotification.sendToRespondent(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyRespondentWithEmailWithException() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRespondentName("respondentName");
        data.getCicCase().setRespondentEmail("testrepr@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();
        data.getCicCase().setLastSelectedOrder(document);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRespondentCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        newOrderIssuedNotification.sendToRespondent(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyRepresentativeWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();
        data.getCicCase().setLastSelectedOrder(document);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        newOrderIssuedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyRepresentativeWithEmailWithException() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        final UUID uuid = UUID.randomUUID();
        final Document document = Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build();
        data.getCicCase().setLastSelectedOrder(document);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        newOrderIssuedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyRepresentativeWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setRepresentativeAddress(AddressGlobalUK.builder().build());

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(), anyMap());
        newOrderIssuedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).sendLetter(null);
    }

    private CaseData getMockCaseData() {
        CicCase cicCase = CicCase.builder()
            .fullName("fullName").caseNumber("CN1")
            .build();
        CaseData caseData = CaseData.builder().cicCase(cicCase).build();

        return caseData;
    }
}
