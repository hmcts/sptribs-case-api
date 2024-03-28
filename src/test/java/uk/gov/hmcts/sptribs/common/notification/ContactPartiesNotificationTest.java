package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getDynamicMultiSelectDocumentList;

@ExtendWith(MockitoExtension.class)
class ContactPartiesNotificationTest {
    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private ContactPartiesNotification contactPartiesNotification;

    @Test
    void shouldNotifySubjectOfCaseIssuedWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(getDynamicMultiSelectDocumentList()).build();
        data.setContactPartiesDocuments(contactPartiesDocuments);
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifySubjectOfCaseIssuedWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setAddress(
            new AddressGlobalUK("11", "JOHN", "STREET", "WINCHESTER", "COUNTY", "TW4 5BH", "UK")
        );

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyApplicantOfCaseIssuedWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(getDynamicMultiSelectDocumentList()).build();
        data.setContactPartiesDocuments(contactPartiesDocuments);
        data.getCicCase().setApplicantFullName("appFullName");
        data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.EMAIL);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToApplicant(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyApplicantOfCaseIssuedWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setApplicantFullName("appFullName");
        data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setApplicantAddress(
            new AddressGlobalUK("11", "JOHN", "STREET", "WINCHESTER", "COUNTY", "TW4 5BH", "UK")
        );

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToApplicant(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyRepresentativeOfCaseIssuedWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(getDynamicMultiSelectDocumentList()).build();
        data.setContactPartiesDocuments(contactPartiesDocuments);
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyRepresentativeOfCaseIssuedWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setRepresentativeAddress(
            new AddressGlobalUK("11", "JOHN", "STREET", "WINCHESTER", "COUNTY", "TW4 5BH", "UK")
        );

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyRespondentOfCaseIssuedWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("respFullName");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToRespondent(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyRespondentOfCaseIssuedWithEmailWithAttachments() {
        //Given
        final CaseData data = getMockCaseData();
        ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(getDynamicMultiSelectDocumentList()).build();
        data.setContactPartiesDocuments(contactPartiesDocuments);
        data.getCicCase().setRepresentativeFullName("respFullName");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToRespondent(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyTribunalOfCaseIssuedWithEmailWithAttachments() {
        //Given
        final CaseData data = getMockCaseData();
        ContactPartiesDocuments contactPartiesDocuments = ContactPartiesDocuments.builder()
            .documentList(getDynamicMultiSelectDocumentList()).build();
        data.setContactPartiesDocuments(contactPartiesDocuments);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        contactPartiesNotification.sendToTribunal(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    private CaseData getMockCaseData() {
        CicCase cicCase = CicCase.builder().fullName("fullName").caseNumber("CN1").build();

        return CaseData.builder().cicCase(cicCase).build();
    }
}
