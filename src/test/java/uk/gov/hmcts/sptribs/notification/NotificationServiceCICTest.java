package uk.gov.hmcts.sptribs.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.common.config.EmailTemplatesConfigCIC;
import uk.gov.hmcts.sptribs.document.CaseDocumentClient;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendLetterResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CASE_ISSUED_CITIZEN_POST;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceCICTest {
    private static final String EMAIL_ADDRESS = "simulate-delivered@notifications.service.gov.uk";

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private Resource resource;

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
    void shouldInvokeNotificationClientToSendEmail() throws NotificationClientException, IOException {
        //Given
        String templateId = UUID.randomUUID().toString();
        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);
        final Map<String, String> templateNameMap = Map.of(APPLICATION_RECEIVED.name(), templateId);
        final UUID uuid = UUID.randomUUID();
        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_RECEIVED.name(), templateId);

        NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(EMAIL_ADDRESS)
            .template(TemplateName.APPLICATION_RECEIVED)
            .templateVars(templateVars)
            .hasFileAttachments(true)
            .uploadedDocumentIds(List.of(uuid.toString()))
            .build();
        notificationService.setNotificationRequest(request);

        User user = TestDataHelper.getUser();
        when(idamService.retrieveUser(any())).thenReturn(user);
        when(sendEmailResponse.getReference()).thenReturn(Optional.of(randomUUID().toString()));
        when(sendEmailResponse.getNotificationId()).thenReturn(UUID.randomUUID());
        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateNameMap);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(firstFile));
        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any())).thenReturn(ResponseEntity.ok(resource));

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
        throws NotificationClientException, IOException {

        //Given
        String templateId = UUID.randomUUID().toString();
        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);
        final Map<String, String> templateNameMap = Map.of(APPLICATION_RECEIVED.name(), templateId);
        final UUID uuid = UUID.randomUUID();
        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_RECEIVED.name(), templateId);

        NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(EMAIL_ADDRESS)
            .template(TemplateName.APPLICATION_RECEIVED)
            .templateVars(templateVars)
            .hasFileAttachments(false)
            .uploadedDocumentIds(new ArrayList<>())
            .build();
        notificationService.setNotificationRequest(request);

        doThrow(new NotificationClientException("some message"))
            .when(notificationClient).sendEmail(
                eq(templateId),
                eq(EMAIL_ADDRESS),
                any(),
                any());

        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateNameMap);

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
    void shouldThrowNotificationExceptionWhileFileUploadToSendEmail()
        throws IOException {

        //Given
        String templateId = UUID.randomUUID().toString();
        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);
        final Map<String, String> templateNameMap = Map.of(APPLICATION_RECEIVED.name(), templateId);
        final UUID uuid = UUID.randomUUID();
        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_RECEIVED.name(), templateId);

        NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(EMAIL_ADDRESS)
            .template(TemplateName.APPLICATION_RECEIVED)
            .templateVars(templateVars)
            .hasFileAttachments(true)
            .uploadedDocumentIds(List.of(uuid.toString()))
            .build();
        notificationService.setNotificationRequest(request);

        User user = TestDataHelper.getUser();
        when(idamService.retrieveUser(any())).thenReturn(user);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any())).thenReturn(ResponseEntity.ok(resource));

        doThrow(new IOException("some message"))
            .when(resource).getInputStream();

        //When&Then
        assertThatThrownBy(() -> notificationService.sendEmail())
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("some message");

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
