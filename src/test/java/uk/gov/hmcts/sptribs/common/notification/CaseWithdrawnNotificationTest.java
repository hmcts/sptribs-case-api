package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.model.CloseReason;
import uk.gov.hmcts.sptribs.caseworker.model.ReinstateReason;
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
public class CaseWithdrawnNotificationTest {
    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private CaseWithdrawnNotification caseWithdrawnNotification;

    @Test
    void shouldNotifySubjectWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testrepr@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        caseWithdrawnNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifySubjectWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setAddress(AddressGlobalUK.builder().build());

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        caseWithdrawnNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendLetter();
    }

    @Test
    void shouldNotifyRespondentWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRespondentName("respondentName");
        data.getCicCase().setRespondentEmail("testrepr@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRespondentCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        caseWithdrawnNotification.sendToRespondent(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifyRepresentativeWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        caseWithdrawnNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendEmail();
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
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        caseWithdrawnNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendLetter();
    }

    private CaseData getMockCaseData() {
        CicCase cicCase = CicCase.builder()
            .fullName("fullName").caseNumber("CN1")
            .build();
        CloseCase closeCase = CloseCase.builder()
            .closeCaseReason(CloseReason.Withdrawn)
            .additionalDetail("additionalDet")
            .build();
        CaseData caseData = CaseData.builder().cicCase(cicCase).closeCase(closeCase).build();

        return caseData;
    }
}
