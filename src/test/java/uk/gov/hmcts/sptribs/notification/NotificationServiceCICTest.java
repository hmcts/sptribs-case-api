package uk.gov.hmcts.sptribs.notification;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.common.config.EmailTemplatesConfigCIC;
import uk.gov.hmcts.sptribs.document.DocumentClient;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendLetterResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mockStatic;
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
    private DocumentClient caseDocumentClient;

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
        final String templateId = UUID.randomUUID().toString();
        final Map<String, String> templateNameMap = Map.of(APPLICATION_RECEIVED.name(), templateId);
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_RECEIVED.name(), templateId);

        final Map<String, String> uploadedDocuments = new HashMap<>();
        uploadedDocuments.put("FinalDecisionNotice", templateId);
        uploadedDocuments.put("FinalDecisionNotice1", "");
        uploadedDocuments.put("DocumentAvailable1", "no");

        final NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(EMAIL_ADDRESS)
            .template(TemplateName.APPLICATION_RECEIVED)
            .templateVars(templateVars)
            .hasFileAttachments(true)
            .uploadedDocuments(uploadedDocuments)
            .build();

        final User user = TestDataHelper.getUser();

        when(idamService.retrieveUser(any())).thenReturn(user);
        when(sendEmailResponse.getReference()).thenReturn(Optional.of(randomUUID().toString()));
        when(sendEmailResponse.getNotificationId()).thenReturn(UUID.randomUUID());
        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateNameMap);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        final byte[] sample = new byte[1];
        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any())).thenReturn(ResponseEntity.ok(sample));

        when(notificationClient.sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any()
        )).thenReturn(sendEmailResponse);

        //When
        notificationService.sendEmail(request);

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
    void shouldInvokeNotificationClientToSendEmailWithNoDocumentFound() throws NotificationClientException {
        //Given
        final String templateId = UUID.randomUUID().toString();
        final Map<String, String> templateNameMap = Map.of(APPLICATION_RECEIVED.name(), templateId);
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_RECEIVED.name(), templateId);

        final Map<String, String> uplodedDocuments = new HashMap<>();
        uplodedDocuments.put("FinalDecisionNotice", templateId);
        final NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(EMAIL_ADDRESS)
            .template(TemplateName.APPLICATION_RECEIVED)
            .templateVars(templateVars)
            .hasFileAttachments(true)
            .uploadedDocuments(uplodedDocuments)
            .build();

        final User user = TestDataHelper.getUser();

        when(idamService.retrieveUser(any())).thenReturn(user);
        when(sendEmailResponse.getReference()).thenReturn(Optional.of(randomUUID().toString()));
        when(sendEmailResponse.getNotificationId()).thenReturn(UUID.randomUUID());
        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateNameMap);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any())).thenReturn(ResponseEntity.ok(null));

        when(notificationClient.sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any()
        )).thenReturn(sendEmailResponse);

        //When
        notificationService.sendEmail(request);

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
        final String templateId = UUID.randomUUID().toString();
        final Map<String, String> templateVars = Map.of(CASE_ISSUED_CITIZEN_POST.name(), templateId);
        final NotificationRequest request = NotificationRequest.builder()
            .template(CASE_ISSUED_CITIZEN_POST)
            .templateVars(Map.of(CASE_ISSUED_CITIZEN_POST.name(), templateId))
            .build();

        when(sendLetterResponse.getReference()).thenReturn(Optional.of(randomUUID().toString()));
        when(sendLetterResponse.getNotificationId()).thenReturn(UUID.randomUUID());
        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateVars);

        when(notificationClient.sendLetter(
            eq(templateId),
            any(),
            any()
        )).thenReturn(sendLetterResponse);

        //When
        notificationService.sendLetter(request);

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
        final String templateId = UUID.randomUUID().toString();
        final Map<String, String> templateNameMap = Map.of(APPLICATION_RECEIVED.name(), templateId);
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_RECEIVED.name(), templateId);

        final NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(EMAIL_ADDRESS)
            .template(TemplateName.APPLICATION_RECEIVED)
            .templateVars(templateVars)
            .hasFileAttachments(false)
            .uploadedDocuments(new HashMap<>())
            .build();

        doThrow(new NotificationClientException("some message"))
            .when(notificationClient).sendEmail(
                eq(templateId),
                eq(EMAIL_ADDRESS),
                any(),
                any());

        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateNameMap);

        //When&Then
        assertThatThrownBy(() -> notificationService.sendEmail(request))
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("some message");

        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any());
    }

    @Test
    void shouldThrowNotificationClientExceptionWhenIssueWithAttachDocFailsToSendEmail()
        throws NotificationClientException {

        //Given
        final String templateId = UUID.randomUUID().toString();
        final Map<String, String> templateNameMap = Map.of(APPLICATION_RECEIVED.name(), templateId);
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_RECEIVED.name(), templateId);

        final NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(EMAIL_ADDRESS)
            .template(TemplateName.APPLICATION_RECEIVED)
            .templateVars(templateVars)
            .hasFileAttachments(false)
            .uploadedDocuments(new HashMap<>())
            .build();

        doThrow(new NotificationException(new IOException()))
            .when(notificationClient).sendEmail(
                eq(templateId),
                eq(EMAIL_ADDRESS),
                any(),
                any());

        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateNameMap);

        //When&Then
        assertThatThrownBy(() -> notificationService.sendEmail(request))
            .isInstanceOf(NotificationException.class)
            .satisfies(e -> assertAll(
                () -> assertTrue(e.getCause() instanceof IOException)
            ));

        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any());
    }

    @Test
    void shouldThrowNotificationExceptionWhileFileUploadToSendEmail() throws NotificationClientException {

        //Given
        final String templateId = UUID.randomUUID().toString();
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_RECEIVED.name(), templateId);

        final Map<String, String> uploadedDocuments = new HashMap<>();
        uploadedDocuments.put("FinalDecisionNotice", templateId);
        uploadedDocuments.put("FinalDecisionNotice1", "");
        uploadedDocuments.put("DocumentAvailable1", "no");

        final NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(EMAIL_ADDRESS)
            .template(TemplateName.APPLICATION_RECEIVED)
            .templateVars(templateVars)
            .hasFileAttachments(true)
            .uploadedDocuments(uploadedDocuments)
            .build();

        final byte[] sample = new byte[1];
        final User user = TestDataHelper.getUser();

        when(idamService.retrieveUser(any())).thenReturn(user);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any())).thenReturn(ResponseEntity.ok(sample));

        final byte[] newUploadDocument = caseDocumentClient.getDocumentBinary(anyString(),anyString(),any()).getBody();
        assertNotNull(newUploadDocument);
        mockStatic(NotificationClient.class);
        when(NotificationClient.prepareUpload(newUploadDocument)).thenThrow(NotificationClientException.class);

        //When&Then
        assertThatThrownBy(() -> notificationService.sendEmail(request))
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("uk.gov.service.notify.NotificationClientException");
    }

    @Test
    void shouldThrowNotificationExceptionWhenClientFailsToSendLetter()
        throws NotificationClientException {
        //Given
        final String templateId = UUID.randomUUID().toString();
        final Map<String, String> templateVars = Map.of(CASE_ISSUED_CITIZEN_POST.name(), templateId);
        final NotificationRequest request = NotificationRequest.builder()
            .template(CASE_ISSUED_CITIZEN_POST)
            .templateVars(Map.of(CASE_ISSUED_CITIZEN_POST.name(), templateId))
            .build();

        doThrow(new NotificationClientException("some message"))
            .when(notificationClient).sendLetter(
                eq(templateId),
                any(),
                any());

        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateVars);

        //When&Then
        assertThatThrownBy(() -> notificationService.sendLetter(request))
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("some message");

        verify(notificationClient).sendLetter(
            eq(templateId),
            any(),
            any());
    }
}
