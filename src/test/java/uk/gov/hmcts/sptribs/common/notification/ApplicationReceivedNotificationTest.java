package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ApplicationReceivedNotificationTest {
    @Mock
    private NotificationServiceCIC notificationService;

    @InjectMocks
    private ApplicationReceivedNotification applicationReceivedNotification;

    @Test
    void shouldNotifySubjectOfApplicationReceivedWithEmail() throws IOException {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);

        //When
        applicationReceivedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifyApplicantOfApplicationReceivedWithEmail() throws IOException {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setApplicantFullName("appFullName");
        data.getCicCase().setApplicantContactDetailsPreference(ContactPreferenceType.EMAIL);

        //When
        applicationReceivedNotification.sendToApplicant(data, "CN1");

        //Then
        verify(notificationService).setNotificationRequest(any(NotificationRequest.class));

        verify(notificationService).sendEmail();
    }

    @Test
    void shouldNotifyRepresentativeOfApplicationReceivedWithEmail() throws IOException {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);

        //When
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
