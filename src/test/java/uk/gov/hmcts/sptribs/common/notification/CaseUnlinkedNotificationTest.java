package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseUnlinkedNotificationTest {
    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private CaseUnlinkedNotification caseUnlinkedNotification;

    @Test
    void shouldNotifySubjectOfApplicationReceivedWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testrepr@outlook.com");
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        //When
        caseUnlinkedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifySubjectOfApplicationReceivedWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setAddress(AddressGlobalUK.builder().build());
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());

        //When
        caseUnlinkedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyApplicantOfApplicationReceivedWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setApplicantFullName("appFullName");
        data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setApplicantEmailAddress("testrepr@outlook.com");
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        //When
        caseUnlinkedNotification.sendToApplicant(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyApplicantOfApplicationReceivedWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setApplicantFullName("appFullName");
        data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setApplicantAddress(AddressGlobalUK.builder().build());
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());

        //When
        caseUnlinkedNotification.sendToApplicant(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyRepresentativeOfApplicationReceivedWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        //When
        caseUnlinkedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyRepresentativeOfApplicationReceivedWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setRepresentativeAddress(AddressGlobalUK.builder().build());
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());

        //When
        caseUnlinkedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
    }

    private CaseData getMockCaseData() {
        CicCase cicCase = CicCase.builder()
            .fullName("fullName").caseNumber("CN1")
            .build();
        return CaseData.builder().cicCase(cicCase).build();
    }

}