package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;

@ExtendWith(MockitoExtension.class)
public class ApplicationReceivedNotificationTest {
    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private ApplicationReceivedNotification applicationReceivedNotification;

    @Test
    void shouldNotifySubjectOfApplicationReceivedWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("test@outlook.com");

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CONTACT_NAME, data.getCicCase().getFullName());

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        applicationReceivedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifyApplicantOfApplicationReceivedWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setApplicantFullName("appFullName");
        data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setApplicantEmailAddress("test@outlook.com");

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CONTACT_NAME, data.getCicCase().getApplicantFullName());

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        applicationReceivedNotification.sendToApplicant(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifyRepresentativeOfApplicationReceivedWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setRepresentativeEmailAddress("test@outlook.com");

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CONTACT_NAME, data.getCicCase().getRepresentativeFullName());

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        applicationReceivedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendEmail();
    }

    private CaseData getMockCaseData() {
        CicCase cicCase = CicCase.builder().fullName("fullName").caseNumber("CN1").build();
        CaseData caseData = CaseData.builder().cicCase(cicCase).build();

        return caseData;
    }

}
