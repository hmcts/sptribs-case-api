package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnonymityAppliedNotificationTest {

    @Mock
    private NotificationServiceCIC notificationServiceCIC;

    @Mock
    private NotificationHelper notificationHelper;

    private AnonymityAppliedNotification anonymityAppliedNotification;

    @BeforeEach
    void setUp() {
        anonymityAppliedNotification = new AnonymityAppliedNotification(
            notificationServiceCIC,
            notificationHelper,
            "anonymity@test.com"
        );
    }

    @Test
    void shouldSendAnonymityAppliedEmail() {
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Test Subject")
                .anonymisationDate(LocalDate.of(2026, 6, 24))
                .build())
            .build();

        Map<String, Object> templateVars = new HashMap<>();
        when(notificationHelper.getTribunalCommonVars(eq("1234-5678-9012-3456"), eq(caseData))).thenReturn(templateVars);
        when(notificationHelper.buildEmailNotificationRequest(eq("anonymity@test.com"), anyMap(),
            eq(TemplateName.ANONYMITY_APPLIED_EMAIL))).thenReturn(NotificationRequest.builder().build());
        when(notificationServiceCIC.sendEmail(any(NotificationRequest.class), eq("1234-5678-9012-3456")))
            .thenReturn(NotificationResponse.builder().id("1").build());

        anonymityAppliedNotification.sendToTribunal(caseData, "1234-5678-9012-3456");

        verify(notificationServiceCIC).sendEmail(any(NotificationRequest.class), eq("1234-5678-9012-3456"));
    }

    @Test
    void shouldNotSendEmailWhenRecipientNotConfigured() {
        anonymityAppliedNotification = new AnonymityAppliedNotification(
            notificationServiceCIC,
            notificationHelper,
            " "
        );

        CaseData caseData = CaseData.builder().cicCase(CicCase.builder().build()).build();

        anonymityAppliedNotification.sendToTribunal(caseData, "1234-5678-9012-3456");

        verify(notificationHelper, never()).getTribunalCommonVars(any(), any());
        verify(notificationServiceCIC, never()).sendEmail(any(), any());
    }
}
