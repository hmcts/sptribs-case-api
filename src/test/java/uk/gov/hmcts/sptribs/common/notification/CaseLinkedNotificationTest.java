package uk.gov.hmcts.sptribs.common.notification;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.caseworker.model.CaseStay;
import uk.gov.hmcts.sptribs.caseworker.model.StayReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.time.LocalDate;
import java.util.HashMap;

@ExtendWith(MockitoExtension.class)
class CaseLinkedNotificationTest {
    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private CaseLinkedNotification caseLinkedNotification;

    @Test
    void shouldNotifySubjectOfApplicationReceivedWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testrepr@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        caseLinkedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }



    private CaseData getMockCaseData() {
        CicCase cicCase = CicCase.builder()
            .fullName("fullName").caseNumber("CN1")
            .build();
//
        CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .caseLinkExists("YES")
            .build();

        return caseData;
    }

}
