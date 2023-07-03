package uk.gov.hmcts.sptribs.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.notification.DssNotificationHelper;
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
class DssApplicationReceivedNotificationTest {
    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private DssNotificationHelper dssNotificationHelper;
    @InjectMocks
    private DssApplicationReceivedNotification dssApplicationReceivedNotification;


    @Test
    void shouldNotifySubjectOfApplicationReceivedWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setEmail("test@outlook.com");

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CONTACT_NAME, data.getCicCase().getFullName());

        //When
        when(dssNotificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(dssNotificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        dssApplicationReceivedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyRepresentativeOfApplicationReceivedWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeEmailAddress("test@outlook.com");

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CONTACT_NAME, data.getCicCase().getRepresentativeFullName());

        //When
        when(dssNotificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(dssNotificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
        dssApplicationReceivedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    private CaseData getMockCaseData() {
        CicCase cicCase = CicCase.builder().fullName("fullName").caseNumber("CN1").build();
        CaseData caseData = CaseData.builder().cicCase(cicCase).build();

        return caseData;
    }

}
