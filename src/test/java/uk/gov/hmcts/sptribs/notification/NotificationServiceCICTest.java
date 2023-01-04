package uk.gov.hmcts.sptribs.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.common.config.EmailTemplatesConfigCIC;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendLetterResponse;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CASE_ISSUED_CITIZEN_POST;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceCICTest {
    private static final String EMAIL_ADDRESS = "simulate-delivered@notifications.service.gov.uk";

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private EmailTemplatesConfigCIC emailTemplatesConfig;

    @InjectMocks
    private NotificationServiceCIC notificationService;

    @Mock
    private SendEmailResponse sendEmailResponse;

    @Mock
    private SendLetterResponse sendLetterResponse;

    @Test
    void shouldInvokeNotificationClientToSendEmail() throws NotificationClientException {
        //Given
        String templateId = UUID.randomUUID().toString();
        Map<String, String> templateVars = Map.of(APPLICATION_RECEIVED.name(), templateId);
        NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(EMAIL_ADDRESS)
            .template(TemplateName.APPLICATION_RECEIVED)
            .templateVars(Map.of(APPLICATION_RECEIVED.name(), templateId))
            .build();
        notificationService.setNotificationRequest(request);

        when(sendEmailResponse.getReference()).thenReturn(Optional.of(randomUUID().toString()));
        when(sendEmailResponse.getNotificationId()).thenReturn(UUID.randomUUID());
        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateVars);

        when(notificationClient.sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any()
        )).thenReturn(sendEmailResponse);

        //When
        notificationService.sendEmail();

        //Then
        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any());

        verify(sendEmailResponse, times(2)).getNotificationId();
        verify(sendEmailResponse, times(2)).getReference();

    }

    @Test
    void shouldInvokeNotificationClientToSendLetter() throws NotificationClientException {
        //Given
        String templateId = UUID.randomUUID().toString();
        Map<String, String> templateVars = Map.of(CASE_ISSUED_CITIZEN_POST.name(), templateId);
        NotificationRequest request = NotificationRequest.builder()
            .template(CASE_ISSUED_CITIZEN_POST)
            .templateVars(Map.of(CASE_ISSUED_CITIZEN_POST.name(), templateId))
            .build();
        notificationService.setNotificationRequest(request);

        when(sendLetterResponse.getReference()).thenReturn(Optional.of(randomUUID().toString()));
        when(sendLetterResponse.getNotificationId()).thenReturn(UUID.randomUUID());
        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateVars);

        when(notificationClient.sendLetter(
            eq(templateId),
            any(),
            any()
        )).thenReturn(sendLetterResponse);

        //When
        notificationService.sendLetter();

        //Then
        verify(notificationClient).sendLetter(
            eq(templateId),
            any(),
            any());

        verify(sendLetterResponse, times(2)).getNotificationId();
        verify(sendLetterResponse, times(2)).getReference();
    }

    @Test
    void shouldThrowNotificationExceptionWhenClientFailsToSendEmail()
        throws NotificationClientException {
        //Given
        String templateId = UUID.randomUUID().toString();
        Map<String, String> templateVars = Map.of(APPLICATION_RECEIVED.name(), templateId);
        NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(EMAIL_ADDRESS)
            .template(TemplateName.APPLICATION_RECEIVED)
            .templateVars(Map.of(APPLICATION_RECEIVED.name(), templateId))
            .build();
        notificationService.setNotificationRequest(request);

        doThrow(new NotificationClientException("some message"))
            .when(notificationClient).sendEmail(
                eq(templateId),
                eq(EMAIL_ADDRESS),
                any(),
                any());

        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateVars);

        //When&Then
        assertThatThrownBy(() -> notificationService.sendEmail())
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("some message");

        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any());
    }

    @Test
    void shouldThrowNotificationExceptionWhenClientFailsToSendLetter()
        throws NotificationClientException {
        //Given
        String templateId = UUID.randomUUID().toString();
        Map<String, String> templateVars = Map.of(CASE_ISSUED_CITIZEN_POST.name(), templateId);
        NotificationRequest request = NotificationRequest.builder()
            .template(CASE_ISSUED_CITIZEN_POST)
            .templateVars(Map.of(CASE_ISSUED_CITIZEN_POST.name(), templateId))
            .build();
        notificationService.setNotificationRequest(request);

        doThrow(new NotificationClientException("some message"))
            .when(notificationClient).sendLetter(
                eq(templateId),
                any(),
                any());

        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateVars);

        //When&Then
        assertThatThrownBy(() -> notificationService.sendLetter())
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("some message");

        verify(notificationClient).sendLetter(
            eq(templateId),
            any(),
            any());
    }
}
