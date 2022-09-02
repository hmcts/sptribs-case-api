package uk.gov.hmcts.sptribs.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.AcknowledgementOfService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.notification.SoleApplicationDisputedNotification;
import uk.gov.hmcts.sptribs.common.notification.SoleApplicationNotDisputedNotification;
import uk.gov.hmcts.sptribs.notification.NotificationDispatcher;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.sptribs.ciccase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.sptribs.ciccase.model.HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE;

@ExtendWith(MockitoExtension.class)
class SendAosNotificationsTest {

    @Mock
    private SoleApplicationNotDisputedNotification soleApplicationNotDisputedNotification;

    @Mock
    private SoleApplicationDisputedNotification soleApplicationDisputedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SendAosNotifications sendAosNotifications;

    @Test
    void shouldSendDisputedNotifications() {
        //Given
        final CaseData caseData = CaseData.builder()
            .acknowledgementOfService(AcknowledgementOfService.builder()
                .howToRespondApplication(DISPUTE_DIVORCE)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(1L);
        caseDetails.setData(caseData);

        //When
        sendAosNotifications.apply(caseDetails);

        //Then
        verify(notificationDispatcher).send(soleApplicationDisputedNotification, caseData, 1L);
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldSendNotDisputedNotifications() {
        //Given
        final CaseData caseData = CaseData.builder()
            .acknowledgementOfService(AcknowledgementOfService.builder()
                .howToRespondApplication(WITHOUT_DISPUTE_DIVORCE)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(1L);
        caseDetails.setData(caseData);

        //When
        sendAosNotifications.apply(caseDetails);

        //Then
        verify(notificationDispatcher).send(soleApplicationNotDisputedNotification, caseData, 1L);
        verifyNoMoreInteractions(notificationDispatcher);
    }
}
