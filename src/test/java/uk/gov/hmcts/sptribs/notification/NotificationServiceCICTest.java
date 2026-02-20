package uk.gov.hmcts.sptribs.notification;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.common.config.EmailTemplatesConfigCIC;
import uk.gov.hmcts.sptribs.common.repositories.CorrespondenceRepository;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendLetterResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import static uk.gov.hmcts.sptribs.notification.TemplateName.CASE_ISSUED_RESPONDENT_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceCICTest {
    public static final int TWO_MEGABYTES = 2 * 1024 * 1024;
    private static final String EMAIL_ADDRESS = "simulate-delivered@notifications.service.gov.uk";

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseDocumentClientApi caseDocumentClientAPI;

    @Mock
    private CorrespondenceRepository correspondenceRepository;

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

    @Mock
    private PDFServiceClient pdfServiceClient;

    @Captor
    private ArgumentCaptor<Map<String, Object>> templateVarsArgCaptor;


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
        when(caseDocumentClientAPI.getDocumentBinary(anyString(), anyString(), any(UUID.class))).thenReturn(ResponseEntity.ok(sample));

        when(notificationClient.sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any()
        )).thenReturn(sendEmailResponse);

        when(pdfServiceClient.generateFromHtml(any(), any())).thenReturn(sample);

        UploadResponse expectedResponse = uploadResponseWithSampleDocument();
        when(caseDocumentClientAPI.uploadDocuments(any(), any(), any())).thenReturn(expectedResponse);

        //When
        notificationService.sendEmail(request, TEST_CASE_ID.toString());

        //Then
        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any());

        verify(sendEmailResponse, times(3)).getNotificationId();
        verify(sendEmailResponse, times(2)).getReference();
        verify(correspondenceRepository, times(1)).save(any());
    }

    @Test
    void shouldInvokeNotificationClientToSendEmailWithNoDocumentFound() throws NotificationClientException {
        //Given
        final String templateId = UUID.randomUUID().toString();
        final Map<String, String> templateNameMap = Map.of(APPLICATION_RECEIVED.name(), templateId);
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_RECEIVED.name(), templateId);

        final Map<String, String> uploadedDocuments = new HashMap<>();
        uploadedDocuments.put("FinalDecisionNotice", templateId);
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
        when(caseDocumentClientAPI.getDocumentBinary(anyString(), anyString(), any(UUID.class))).thenReturn(ResponseEntity.ok(null));

        when(notificationClient.sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any()
        )).thenReturn(sendEmailResponse);

        final byte[] sample = new byte[1];

        when(pdfServiceClient.generateFromHtml(any(), any())).thenReturn(sample);

        UploadResponse expectedResponse = uploadResponseWithSampleDocument();
        when(caseDocumentClientAPI.uploadDocuments(any(), any(), any())).thenReturn(expectedResponse);

        //When
        notificationService.sendEmail(request, TEST_CASE_ID.toString());

        //Then
        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any());

        verify(sendEmailResponse, times(3)).getNotificationId();
        verify(sendEmailResponse, times(2)).getReference();

    }

    @Test
    void shouldInvokeNotificationClientToSendEmailWithSenderEmailAndWithNoDocumentFound() throws NotificationClientException {
        //Given
        final String templateId = UUID.randomUUID().toString();
        final Map<String, String> templateNameMap = Map.of(APPLICATION_RECEIVED.name(), templateId);
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_RECEIVED.name(), templateId);

        final Map<String, String> uploadedDocuments = new HashMap<>();
        uploadedDocuments.put("FinalDecisionNotice", templateId);
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
        when(sendEmailResponse.getFromEmail()).thenReturn("testSender@test.com".describeConstable());
        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateNameMap);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClientAPI.getDocumentBinary(anyString(), anyString(), any(UUID.class))).thenReturn(ResponseEntity.ok(null));

        when(notificationClient.sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any()
        )).thenReturn(sendEmailResponse);

        final byte[] sample = new byte[1];

        when(pdfServiceClient.generateFromHtml(any(), any())).thenReturn(sample);

        final Document.DocumentLink documentLink = new Document.DocumentLink();
        documentLink.href = "dmstore-url/doc-id";
        final Document.DocumentLink binaryDocumentLink = new Document.DocumentLink();
        binaryDocumentLink.href = "dmstore-url/doc-id/binary";
        final Document.Links links = new Document.Links();
        links.self = documentLink;
        links.binary = binaryDocumentLink;

        final Document correspondencePDF = new Document();
        correspondencePDF.setLinks(links);

        final LocalDateTime testSentOn = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-y-HH-mm");
        String filename = APPLICATION_RECEIVED.name() + "_" + TEST_CASE_ID + "_" + testSentOn.format(formatter) + ".pdf";
        correspondencePDF.setOriginalDocumentName(filename);

        UploadResponse expectedResponse = new UploadResponse();
        expectedResponse.setDocuments(singletonList(correspondencePDF));

        when(caseDocumentClientAPI.uploadDocuments(any(), any(), any())).thenReturn(expectedResponse);

        //When
        notificationService.sendEmail(request, TEST_CASE_ID.toString());

        //Then
        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any());

        verify(sendEmailResponse, times(2)).getFromEmail();
        verify(sendEmailResponse, times(3)).getNotificationId();
        verify(sendEmailResponse, times(2)).getReference();

    }

    @Test
    void shouldInvokeNotificationClientToSendLetter() throws NotificationClientException {
        //Given
        final String templateId = UUID.randomUUID().toString();
        final Map<String, String> templateVars = new HashMap<>(Map.of(CASE_ISSUED_CITIZEN_POST.name(), templateId));
        templateVars.put("address_line_1", "Buckingham Palace");
        templateVars.put("address_line_4", "London");
        templateVars.put("address_line_5", "United Kingdom");
        templateVars.put("address_line_7", "SW1A 1AA");

        final NotificationRequest request = NotificationRequest.builder()
            .template(CASE_ISSUED_CITIZEN_POST)
            .templateVars(new HashMap<>(templateVars))
            .build();

        when(sendLetterResponse.getReference()).thenReturn(Optional.of(randomUUID().toString()));
        when(sendLetterResponse.getNotificationId()).thenReturn(UUID.randomUUID());
        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(Map.of(CASE_ISSUED_CITIZEN_POST.name(), templateId));

        when(notificationClient.sendLetter(
            eq(templateId),
            any(),
            any()
        )).thenReturn(sendLetterResponse);

        final byte[] sample = new byte[1];

        when(pdfServiceClient.generateFromHtml(any(), any())).thenReturn(sample);

        UploadResponse expectedResponse = uploadResponseWithSampleDocument();
        when(caseDocumentClientAPI.uploadDocuments(any(), any(), any())).thenReturn(expectedResponse);

        //When
        notificationService.sendLetter(request, TEST_CASE_ID.toString());

        //Then
        verify(notificationClient).sendLetter(
            eq(templateId),
            any(),
            any());

        verify(sendLetterResponse, times(3)).getNotificationId();
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

        String testCaseRef = TEST_CASE_ID.toString();

        //When&Then
        assertThatThrownBy(() -> notificationService.sendEmail(request, testCaseRef))
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

        String testCaseRef = TEST_CASE_ID.toString();

        assertThatThrownBy(() -> notificationService.sendEmail(request, testCaseRef))
            .isInstanceOf(NotificationException.class)
            .satisfies(e -> assertAll(
                () -> assertInstanceOf(IOException.class, e.getCause())
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
        when(caseDocumentClientAPI.getDocumentBinary(anyString(), anyString(), any())).thenReturn(ResponseEntity.ok(sample));

        final byte[] newUploadDocument = caseDocumentClientAPI.getDocumentBinary(anyString(),anyString(),any()).getBody();
        assertNotNull(newUploadDocument);
        mockStatic(NotificationClient.class);
        when(NotificationClient.prepareUpload(newUploadDocument)).thenThrow(NotificationClientException.class);

        String testCaseRef = TEST_CASE_ID.toString();

        assertThatThrownBy(() -> notificationService.sendEmail(request, testCaseRef))
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

        String testCaseRef = TEST_CASE_ID.toString();

        assertThatThrownBy(() -> notificationService.sendLetter(request, testCaseRef))
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("some message");

        verify(notificationClient).sendLetter(
            eq(templateId),
            any(),
            any());
    }

    @Test
    void shouldThrowRestClientExceptionWhenClientFailsToGetPDFOfCorrespondence()
        throws RestClientException, NotificationClientException {
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

        //When&Then
        when(idamService.retrieveUser(any())).thenReturn(user);
        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateNameMap);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        final byte[] sample = new byte[1];
        when(caseDocumentClientAPI.getDocumentBinary(anyString(), anyString(), any(UUID.class))).thenReturn(ResponseEntity.ok(sample));

        when(notificationClient.sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any()
        )).thenReturn(sendEmailResponse);

        when(pdfServiceClient.generateFromHtml(any(), any())).thenReturn(sample);

        doThrow(new RestClientException("some message"))
            .when(caseDocumentClientAPI).uploadDocuments(
                any(),
                any(),
                any());

        String testCaseRef = TEST_CASE_ID.toString();

        assertThatThrownBy(() -> notificationService.sendEmail(request, testCaseRef))
            .isInstanceOf(RestClientException.class)
            .hasMessageContaining("some message");

        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any());
    }

    @Test
    void shouldThrowIOExceptionWhenServiceFailsToGetPDFByteArrayOfEmailCorrespondence()
        throws NotificationClientException {
        try (MockedStatic<IOUtils> mockedIoUtils = mockStatic(IOUtils.class)) {
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
                .build();

            //When&Then
            when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateNameMap);

            when(notificationClient.sendEmail(
                eq(templateId),
                eq(EMAIL_ADDRESS),
                any(),
                any()
            )).thenReturn(sendEmailResponse);

            mockedIoUtils.when(() -> IOUtils.toByteArray((java.io.InputStream) any()))
                .thenThrow(new IOException("some message"));

            String testCaseRef = TEST_CASE_ID.toString();
            String testTemplateName = APPLICATION_RECEIVED.name();
            String testDestinationAddress = request.getDestinationAddress();

            assertThatThrownBy(() -> notificationService.saveEmailCorrespondence(testTemplateName,
                sendEmailResponse, testDestinationAddress, testCaseRef))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("some message");

            assertThatThrownBy(() -> notificationService.sendEmail(request, testCaseRef))
                .isInstanceOf(NotificationException.class)
                .hasMessageContaining("some message");

            verify(notificationClient).sendEmail(
                eq(templateId),
                eq(EMAIL_ADDRESS),
                any(),
                any());
        }
    }

    @Test
    void shouldThrowIOExceptionExceptionWhenServiceFailsToGetPDFByteArrayOfLetterCorrespondence()
        throws NotificationClientException {
        try (MockedStatic<IOUtils> mockedIoUtils = mockStatic(IOUtils.class)) {
            //Given
            final String templateId = UUID.randomUUID().toString();
            final Map<String, String> templateVars = new HashMap<>(Map.of(CASE_ISSUED_CITIZEN_POST.name(), templateId));
            templateVars.put("address_line_1", "Buckingham Palace");
            templateVars.put("address_line_2", "");
            templateVars.put("address_line_3", "");
            templateVars.put("address_line_4", "London");
            templateVars.put("address_line_5", "United Kingdom");
            templateVars.put("address_line_6", "");
            templateVars.put("address_line_7", "SW1A 1AA");

            final NotificationRequest request = NotificationRequest.builder()
                .template(CASE_ISSUED_CITIZEN_POST)
                .templateVars(new HashMap<>(templateVars))
                .build();

            //When&Then
            when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(Map.of(CASE_ISSUED_CITIZEN_POST.name(), templateId));

            when(notificationClient.sendLetter(
                eq(templateId),
                any(),
                any()
            )).thenReturn(sendLetterResponse);

            mockedIoUtils.when(() -> IOUtils.toByteArray((java.io.InputStream) any()))
                .thenThrow(new IOException("some message"));

            String testCaseRef = TEST_CASE_ID.toString();
            String testTemplateName = CASE_ISSUED_CITIZEN_POST.name();
            String testDestinationAddress = request.getDestinationAddress();

            assertThatThrownBy(() -> notificationService.saveLetterCorrespondence(testTemplateName,
                sendLetterResponse, testDestinationAddress, testCaseRef))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("some message");

            assertThatThrownBy(() -> notificationService.sendLetter(request, testCaseRef))
                .isInstanceOf(NotificationException.class)
                .hasMessageContaining("some message");

            verify(notificationClient).sendLetter(
                eq(templateId),
                any(),
                any());
        }
    }

    @Test
    void shouldThrowNullArgumentExceptionWhenServiceFailsToGetAddressOfLetterRecipient() throws NotificationClientException {
        //Given
        final String templateId = UUID.randomUUID().toString();
        final Map<String, String> templateVars = Map.of(CASE_ISSUED_CITIZEN_POST.name(), templateId);

        final NotificationRequest request = NotificationRequest.builder()
            .template(CASE_ISSUED_CITIZEN_POST)
            .templateVars(new HashMap<>(templateVars))
            .build();

        //When&Then
        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateVars);

        when(notificationClient.sendLetter(
            eq(templateId),
            any(),
            any()
        )).thenReturn(sendLetterResponse);

        String testCaseRef = TEST_CASE_ID.toString();

        assertThatThrownBy(() -> notificationService.sendLetter(request, testCaseRef))
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("Recipient address must not be null");

        verify(notificationClient).sendLetter(
            eq(templateId),
            any(),
            any());
    }

    @Test
    void shouldSuccessfullySendEmail_noAttachments() throws NotificationClientException {
        final String templateId = UUID.randomUUID().toString();
        final Map<String, String> templateNameMap = Map.of(CASE_ISSUED_RESPONDENT_EMAIL.name(), templateId);
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CASE_ISSUED_RESPONDENT_EMAIL.name(), templateId);

        final Map<String, String> uploadedDocuments = Map.of(
            "CaseDocument1", "",
            "DocumentAvailable1", "no");

        final NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(EMAIL_ADDRESS)
            .template(CASE_ISSUED_RESPONDENT_EMAIL)
            .templateVars(templateVars)
            .hasFileAttachments(true)
            .uploadedDocuments(uploadedDocuments)
            .build();

        final User user = TestDataHelper.getUser();
        when(idamService.retrieveUser(any())).thenReturn(user);
        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateNameMap);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(notificationClient.sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any()
        )).thenReturn(sendEmailResponse);
        when(sendEmailResponse.getReference()).thenReturn(Optional.of(randomUUID().toString()));
        when(sendEmailResponse.getNotificationId()).thenReturn(UUID.randomUUID());

        UploadResponse expectedResponse = uploadResponseWithSampleDocument();
        when(caseDocumentClientAPI.uploadDocuments(any(), any(), any())).thenReturn(expectedResponse);

        //When
        notificationService.sendEmail(request, TEST_CASE_ID.toString());

        //Then
        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any());

        verify(sendEmailResponse, times(3)).getNotificationId();
        verify(sendEmailResponse, times(2)).getReference();
        verify(correspondenceRepository, times(1)).save(any());
    }

    @Test
    void shouldSuccessfullySendEmail_attachmentLessThan2MB() throws NotificationClientException {
        final String templateId = UUID.randomUUID().toString();
        final Map<String, String> templateNameMap = Map.of(CASE_ISSUED_RESPONDENT_EMAIL.name(), templateId);
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CASE_ISSUED_RESPONDENT_EMAIL.name(), templateId);

        String docId = randomUUID().toString();
        final Map<String, String> uploadedDocuments = Map.of(
            "CaseDocument1", docId,
            "DocumentAvailable1", "yes");

        final uk.gov.hmcts.ccd.sdk.type.Document document = uk.gov.hmcts.ccd.sdk.type.Document.builder()
            .filename("test file")
            .url("test.url/" + docId)
            .binaryUrl("test.url/" + docId + "/binary")
            .build();
        final CaseworkerCICDocument cicDocument = CaseworkerCICDocument.builder()
            .date(LocalDate.of(2025, 12, 11))
            .documentCategory(DocumentType.APPLICATION_FOR_AN_EXTENSION_OF_TIME)
            .documentEmailContent("description")
            .documentLink(document)
            .build();

        final NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(EMAIL_ADDRESS)
            .template(CASE_ISSUED_RESPONDENT_EMAIL)
            .templateVars(templateVars)
            .hasFileAttachments(true)
            .uploadedDocuments(uploadedDocuments)
            .build();

        final User user = TestDataHelper.getUser();
        when(idamService.retrieveUser(any())).thenReturn(user);
        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateNameMap);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(notificationClient.sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any()
        )).thenReturn(sendEmailResponse);
        when(sendEmailResponse.getReference()).thenReturn(Optional.of(randomUUID().toString()));
        when(sendEmailResponse.getNotificationId()).thenReturn(UUID.randomUUID());

        final ResponseEntity<byte[]> sample = ResponseEntity.ok(new byte[1]);
        when(caseDocumentClientAPI.getDocumentBinary(anyString(), anyString(), any(UUID.class))).thenReturn(sample);

        UploadResponse expectedResponse = uploadResponseWithSampleDocument();

        when(caseDocumentClientAPI.uploadDocuments(any(), any(), any())).thenReturn(expectedResponse);

        //When
        notificationService.sendEmail(request, List.of(cicDocument), TEST_CASE_ID.toString());

        //Then
        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            templateVarsArgCaptor.capture(),
            any());

        assertThat(templateVarsArgCaptor.getValue())
            .containsEntry(CASE_ISSUED_RESPONDENT_EMAIL.name(), templateId)
            .containsEntry("DocumentAvailable1", "yes");
        assertThat(templateVarsArgCaptor.getValue())
            .extracting("CaseDocument1")
            .isInstanceOf(JSONObject.class);

        verify(sendEmailResponse, times(3)).getNotificationId();
        verify(sendEmailResponse, times(2)).getReference();
        verify(correspondenceRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowExceptionWhenFailingToGetBinary() {
        final String templateId = UUID.randomUUID().toString();
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CASE_ISSUED_RESPONDENT_EMAIL.name(), templateId);

        String docId = randomUUID().toString();
        final Map<String, String> uploadedDocuments = Map.of(
            "CaseDocument1", docId,
            "DocumentAvailable1", "yes");

        final uk.gov.hmcts.ccd.sdk.type.Document document = uk.gov.hmcts.ccd.sdk.type.Document.builder()
            .filename("test file")
            .url("test.url/" + docId)
            .binaryUrl("test.url/" + docId + "/binary")
            .build();
        final CaseworkerCICDocument cicDocument = CaseworkerCICDocument.builder()
            .date(LocalDate.of(2025, 12, 11))
            .documentCategory(DocumentType.APPLICATION_FOR_AN_EXTENSION_OF_TIME)
            .documentEmailContent("description")
            .documentLink(document)
            .build();

        final NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(EMAIL_ADDRESS)
            .template(CASE_ISSUED_RESPONDENT_EMAIL)
            .templateVars(templateVars)
            .hasFileAttachments(true)
            .uploadedDocuments(uploadedDocuments)
            .build();

        final User user = TestDataHelper.getUser();
        when(idamService.retrieveUser(any())).thenReturn(user);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(caseDocumentClientAPI.getDocumentBinary(anyString(), anyString(), any(UUID.class)))
            .thenReturn(ResponseEntity.notFound().build());

        assertThatThrownBy(() -> notificationService.sendEmail(request, List.of(cicDocument), TEST_CASE_ID.toString()))
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("Failed to get document binary for id " + docId);
    }

    @Test
    void shouldSuccessfullySendEmail_attachmentMoreThan2MB() throws NotificationClientException {
        final String templateId = UUID.randomUUID().toString();
        final Map<String, String> templateNameMap = Map.of(CASE_ISSUED_RESPONDENT_EMAIL.name(), templateId);
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CASE_ISSUED_RESPONDENT_EMAIL.name(), templateId);

        String docId = randomUUID().toString();
        final Map<String, String> uploadedDocuments = Map.of(
            "CaseDocument1", docId,
            "DocumentAvailable1", "yes");

        final uk.gov.hmcts.ccd.sdk.type.Document document = uk.gov.hmcts.ccd.sdk.type.Document.builder()
            .filename("test file")
            .url("test.url/" + docId)
            .binaryUrl("test.url/" + docId + "/binary")
            .build();
        final CaseworkerCICDocument cicDocument = CaseworkerCICDocument.builder()
            .date(LocalDate.of(2025, 12, 11))
            .documentCategory(DocumentType.APPLICATION_FOR_AN_EXTENSION_OF_TIME)
            .documentEmailContent("description")
            .documentLink(document)
            .build();

        final NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(EMAIL_ADDRESS)
            .template(CASE_ISSUED_RESPONDENT_EMAIL)
            .templateVars(templateVars)
            .hasFileAttachments(true)
            .uploadedDocuments(uploadedDocuments)
            .build();

        final User user = TestDataHelper.getUser();
        when(idamService.retrieveUser(any())).thenReturn(user);
        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templateNameMap);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        final ResponseEntity<byte[]> sample = ResponseEntity.ok(new byte[TWO_MEGABYTES + 1]);
        when(caseDocumentClientAPI.getDocumentBinary(anyString(), anyString(), any(UUID.class))).thenReturn(sample);

        when(notificationClient.sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            any(),
            any()
        )).thenReturn(sendEmailResponse);
        when(sendEmailResponse.getReference()).thenReturn(Optional.of(randomUUID().toString()));
        when(sendEmailResponse.getNotificationId()).thenReturn(UUID.randomUUID());

        UploadResponse expectedResponse = uploadResponseWithSampleDocument();
        when(caseDocumentClientAPI.uploadDocuments(any(), any(), any())).thenReturn(expectedResponse);

        //When
        notificationService.sendEmail(request, List.of(cicDocument), TEST_CASE_ID.toString());

        //Then
        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            templateVarsArgCaptor.capture(),
            any());

        assertThat(templateVarsArgCaptor.getValue())
            .containsEntry(CASE_ISSUED_RESPONDENT_EMAIL.name(), templateId)
            .containsEntry("DocumentAvailable1", "yes");

        String expectedDocumentDescription = String.format("%nFilename: %s%nDescription: %s%nUpload Date: %s",
            cicDocument.getDocumentLink().getFilename(), cicDocument.getDocumentEmailContent(), cicDocument.getDate());

        assertThat(templateVarsArgCaptor.getValue())
            .extracting("CaseDocument1")
            .isInstanceOf(String.class)
            .isEqualTo(expectedDocumentDescription);

        verify(sendEmailResponse, times(3)).getNotificationId();
        verify(sendEmailResponse, times(2)).getReference();
        verify(correspondenceRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowExceptionWhenSelectedDocumentsDoesNotContainDocumentId_AttachmentOver2MB() {
        final String templateId = UUID.randomUUID().toString();
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CASE_ISSUED_RESPONDENT_EMAIL.name(), templateId);

        String docId = randomUUID().toString();
        final Map<String, String> uploadedDocuments = Map.of(
            "CaseDocument1", docId,
            "DocumentAvailable1", "yes");

        final NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(EMAIL_ADDRESS)
            .template(CASE_ISSUED_RESPONDENT_EMAIL)
            .templateVars(templateVars)
            .hasFileAttachments(true)
            .uploadedDocuments(uploadedDocuments)
            .build();

        final User user = TestDataHelper.getUser();
        when(idamService.retrieveUser(any())).thenReturn(user);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        final ResponseEntity<byte[]> sample = ResponseEntity.ok(new byte[TWO_MEGABYTES + 1]);
        when(caseDocumentClientAPI.getDocumentBinary(anyString(), anyString(), any(UUID.class))).thenReturn(sample);

        String expectedErrorMessage = String.format("Unable to find document details for document id: %s", docId);
        assertThatThrownBy(() -> notificationService.sendEmail(request, List.of(), TEST_CASE_ID.toString()))
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining(expectedErrorMessage);
    }

    private UploadResponse uploadResponseWithSampleDocument() {
        Document.DocumentLink self = new Document.DocumentLink();
        self.href = "dmstore-url/doc-id";
        Document.DocumentLink binary = new Document.DocumentLink();
        binary.href = "dmstore-url/doc-id/binary";
        Document.Links links = new Document.Links();
        links.self = self;
        links.binary = binary;
        Document correspondencePDF = new Document();
        correspondencePDF.setLinks(links);
        UploadResponse response = new UploadResponse();
        response.setDocuments(singletonList(correspondencePDF));
        return response;
    }
}
