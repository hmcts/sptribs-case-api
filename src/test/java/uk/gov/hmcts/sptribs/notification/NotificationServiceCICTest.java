package uk.gov.hmcts.sptribs.notification;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.common.config.EmailTemplatesConfigCIC;
import uk.gov.hmcts.sptribs.common.repositories.CorrespondenceRepository;
import uk.gov.hmcts.sptribs.document.DocumentUtil;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.notification.model.Party;
import uk.gov.hmcts.sptribs.notification.persistence.CorrespondenceEntity;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendLetterResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CASE_ISSUED_CITIZEN_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CASE_ISSUED_CITIZEN_POST;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CASE_ISSUED_RESPONDENT_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceCICTest {
    private static final int TWO_MEGABYTES = 2 * 1024 * 1024;
    private static final String EMAIL_ADDRESS = "simulate-delivered@notifications.service.gov.uk";
    private static final String TEMPLATE_ID = UUID.randomUUID().toString();

    private static final byte[] SMALL_FILE = new byte[1];
    private static final byte[] LARGE_FILE = new byte[(int) TWO_MEGABYTES + 1];

    private static final Map<String, Object> BASE_TEMPLATE_VARS = new HashMap<>(Map.of(
        CommonConstants.CIC_CASE_SUBJECT_NAME, "fullName",
        CommonConstants.CIC_CASE_NUMBER, "1234567891011121",
        CommonConstants.TRIBUNAL_NAME, CommonConstants.CIC));

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

    @Captor
    private ArgumentCaptor<CorrespondenceEntity> correspondenceCaptor;

    private static final Map<String, String> TEMPLATE_NAME_MAP = Map.of(APPLICATION_RECEIVED.name(),
        UUID.randomUUID().toString(),
        CASE_ISSUED_RESPONDENT_EMAIL.name(),
        UUID.randomUUID().toString(),
        CASE_ISSUED_CITIZEN_POST.name(),
        UUID.randomUUID().toString());

    private static final Map<String, String> DOCUMENT_TEMPLATE_VARS = Map.of("DocumentAvailable1", "yes",
        "DocumentAvailable2", "no",
        "CaseDocument1", UUID.randomUUID().toString(),
        "CaseDocument2", "");

    private List<CaseworkerCICDocument> singleDocumentAttached() {
        List<ListValue<CaseworkerCICDocument>> documentList = TestDataHelper.getCaseworkerCICDocumentList("test.docx");
        return documentList.stream().map(ListValue::getValue).toList();
    }

    @Nested
    class WhenCitizenDashboardDisabled {
        @BeforeEach
        void setUpFlag() {
            ReflectionTestUtils.setField(notificationService, "citizenDashboardEnabled", false);
        }

        @Nested
        class SendEmailJourney {
            @Test
            void sendEmailSuccessfullyWithNoAttachments() throws NotificationClientException {
                stubPdfGenerationAndUpload();
                stubTemplate(APPLICATION_RECEIVED);
                stubSuccessfulEmailSend();

                NotificationRequest request = emailRequestBuilder().build();
                notificationService.sendEmail(request, TEST_CASE_ID.toString(), Party.APPLICANT);

                verify(notificationClient).sendEmail(eq(TEMPLATE_ID), eq(EMAIL_ADDRESS), any(), anyString());
                verify(correspondenceRepository).save(any());
            }

            @Test
            void attachesDocumentInlineWhenUnderTwoMegabytes() throws NotificationClientException {
                stubAuth();
                stubPdfGenerationAndUpload();
                stubTemplate(CASE_ISSUED_RESPONDENT_EMAIL);
                stubSuccessfulEmailSend();

                List<CaseworkerCICDocument> attachedDocument = singleDocumentAttached();
                String documentUuid = DocumentUtil.getDocumentUuidFromCaseworkerCICDocument(attachedDocument.getFirst());
                stubDocumentBinary(documentUuid, SMALL_FILE);

                Map<String, String> templateDocumentVars = new HashMap<>();
                templateDocumentVars.put("DocumentAvailable1", "yes");
                templateDocumentVars.put("CaseDocument1", documentUuid);

                NotificationRequest request = emailRequestBuilder()
                    .template(CASE_ISSUED_RESPONDENT_EMAIL)
                    .hasFileAttachments(true)
                    .templateDocumentVars(templateDocumentVars)
                    .build();

                notificationService.sendEmail(request, TEST_CASE_ID.toString(), Party.RESPONDENT);

                verify(notificationClient).sendEmail(eq(TEMPLATE_ID), eq(EMAIL_ADDRESS), templateVarsArgCaptor.capture(), anyString());
                assertThat(templateVarsArgCaptor.getValue()).containsEntry("DocumentAvailable1", "yes");
                assertThat(templateVarsArgCaptor.getValue()).extracting("CaseDocument1").isInstanceOf(JSONObject.class);
            }

            @Test
            void addsDocumentDetailsWhenOverTwoMegabytes() throws NotificationClientException {
                stubAuth();
                stubPdfGenerationAndUpload();
                stubTemplate(CASE_ISSUED_RESPONDENT_EMAIL);
                stubSuccessfulEmailSend();

                List<CaseworkerCICDocument> attachedDocs = singleDocumentAttached();
                String documentUuid = DocumentUtil.getDocumentUuidFromCaseworkerCICDocument(attachedDocs.getFirst());
                stubDocumentBinary(documentUuid, LARGE_FILE);

                Map<String, String> templateDocumentVars = new HashMap<>();
                templateDocumentVars.put("DocumentAvailable1", "yes");
                templateDocumentVars.put("CaseDocument1", documentUuid);

                NotificationRequest request = emailRequestBuilder()
                    .template(CASE_ISSUED_RESPONDENT_EMAIL)
                    .hasFileAttachments(true)
                    .templateDocumentVars(templateDocumentVars)
                    .build();

                notificationService.sendEmail(request, TEST_CASE_ID.toString(), Party.RESPONDENT);

                verify(notificationClient).sendEmail(eq(TEMPLATE_ID), eq(EMAIL_ADDRESS), templateVarsArgCaptor.capture(), anyString());
                String detail = templateVarsArgCaptor.getValue().get("CaseDocument1").toString();
                assertThat(detail).contains("Filename:")
                    .contains("Description:")
                    .contains("Upload Date:");
            }

            @Test
            void throwsWhenDocumentNotFoundInSelectedDocumentsOverTwoMegabytes() {
                stubAuth();
                String missingDocumentUuid = UUID.randomUUID().toString();
                stubDocumentBinary(missingDocumentUuid, LARGE_FILE);

                Map<String, String> templateDocumentVars = new HashMap<>();
                templateDocumentVars.put("DocumentAvailable1", "yes");
                templateDocumentVars.put("CaseDocument1", missingDocumentUuid);

                NotificationRequest request = emailRequestBuilder()
                    .template(CASE_ISSUED_RESPONDENT_EMAIL)
                    .templateDocumentVars(templateDocumentVars)
                    .build();

                assertThatThrownBy(() -> notificationService.sendEmail(request, TEST_CASE_ID.toString(), null))
                    .isInstanceOf(NotificationException.class)
                    .hasMessageContaining("Unable to find document details for document id: " + missingDocumentUuid);
            }

            @Test
            void putsEmptyStringWhenDocumentIdIsBlank() throws NotificationClientException {
                stubPdfGenerationAndUpload();
                stubTemplate(APPLICATION_RECEIVED);
                stubSuccessfulEmailSend();

                Map<String, String> templateDocumentVars = new HashMap<>();
                templateDocumentVars.put("CaseDocument1", "");
                templateDocumentVars.put("DocumentAvailable1", "no");

                NotificationRequest request = emailRequestBuilder()
                    .templateDocumentVars(templateDocumentVars)
                    .build();

                notificationService.sendEmail(request, TEST_CASE_ID.toString(), Party.APPLICANT);

                verify(notificationClient).sendEmail(eq(TEMPLATE_ID), eq(EMAIL_ADDRESS), templateVarsArgCaptor.capture(), anyString());
                assertThat(templateVarsArgCaptor.getValue()).containsEntry("CaseDocument1", "");
                verify(caseDocumentClientAPI, never()).getDocumentBinary(any(), any(), any());
            }

            @Test
            void putsEmptyStringWhenBinaryIsNull() throws NotificationClientException {
                stubAuth();
                stubPdfGenerationAndUpload();
                stubTemplate(APPLICATION_RECEIVED);
                stubSuccessfulEmailSend();

                String documentUuid = UUID.randomUUID().toString();
                when(caseDocumentClientAPI.getDocumentBinary(anyString(), anyString(), eq(UUID.fromString(documentUuid))))
                    .thenReturn(ResponseEntity.ok(null));

                Map<String, String> templateDocumentVars = new HashMap<>();
                templateDocumentVars.put("DocumentAvailable1", "yes");
                templateDocumentVars.put("CaseDocument1", documentUuid);

                NotificationRequest request = emailRequestBuilder().templateDocumentVars(templateDocumentVars).build();
                notificationService.sendEmail(request, TEST_CASE_ID.toString(), Party.APPLICANT);

                verify(notificationClient).sendEmail(eq(TEMPLATE_ID), eq(EMAIL_ADDRESS), templateVarsArgCaptor.capture(), anyString());
                assertThat(templateVarsArgCaptor.getValue()).containsEntry("CaseDocument1", "");
            }

            @Test
            void throwsWhenDocumentBinaryFetchFails() {
                stubAuth();
                String documentUuid = UUID.randomUUID().toString();
                when(caseDocumentClientAPI.getDocumentBinary(anyString(), anyString(), eq(UUID.fromString(documentUuid))))
                    .thenReturn(ResponseEntity.notFound().build());

                Map<String, String> templateDocumentVars = new HashMap<>();
                templateDocumentVars.put("DocumentAvailable1", "yes");
                templateDocumentVars.put("CaseDocument1", documentUuid);

                NotificationRequest request = emailRequestBuilder().templateDocumentVars(templateDocumentVars).build();

                assertThatThrownBy(() -> notificationService.sendEmail(request, TEST_CASE_ID.toString(), null))
                    .isInstanceOf(NotificationException.class)
                    .hasMessageContaining("Failed to get document binary for id " + documentUuid);
            }

            @Test
            void wrapsNotificationClientExceptionOnSendFailure()  throws NotificationClientException {
                stubTemplate(APPLICATION_RECEIVED);
                doThrow(new NotificationClientException("some message"))
                    .when(notificationClient).sendEmail(eq(TEMPLATE_ID), eq(EMAIL_ADDRESS), any(), anyString());

                NotificationRequest request = emailRequestBuilder().build();

                assertThatThrownBy(() -> notificationService.sendEmail(request, TEST_CASE_ID.toString(), null))
                    .isInstanceOf(NotificationException.class)
                    .hasMessageContaining("some message");
            }

            @Test
            void wrapsRestClientExceptionWhenCorrespondenceUploadFails() throws NotificationClientException {
                stubTemplate(APPLICATION_RECEIVED);
                when(notificationClient.sendEmail(eq(TEMPLATE_ID), eq(EMAIL_ADDRESS), any(),anyString())).thenReturn(sendEmailResponse);
                doThrow(new RestClientException("upload failed"))
                    .when(caseDocumentClientAPI).uploadDocuments(any(), any(), any());

                NotificationRequest request = emailRequestBuilder().build();

                assertThatThrownBy(() -> notificationService.sendEmail(request, TEST_CASE_ID.toString(), null))
                    .isInstanceOf(RestClientException.class)
                    .hasMessageContaining("upload failed");
            }

            @Test
            void wrapsFeignExceptionOnSendFailure() throws NotificationClientException {
                stubTemplate(APPLICATION_RECEIVED);
                doThrow(mock(FeignException.class))
                    .when(notificationClient).sendEmail(eq(TEMPLATE_ID), eq(EMAIL_ADDRESS), any(), anyString());

                NotificationRequest request = emailRequestBuilder().build();

                assertThatThrownBy(() -> notificationService.sendEmail(request, TEST_CASE_ID.toString(), null))
                    .isInstanceOf(NotificationException.class);
            }

            @Test
            void savesCorrespondenceWithCorrectPartyAndDefaultSender() throws NotificationClientException {
                stubPdfGenerationAndUpload();
                stubTemplate(APPLICATION_RECEIVED);
                stubSuccessfulEmailSend();

                NotificationRequest request = emailRequestBuilder().build();
                notificationService.sendEmail(request, TEST_CASE_ID.toString(), Party.APPLICANT);

                verify(correspondenceRepository).save(correspondenceCaptor.capture());
                CorrespondenceEntity savedCorrespondence = correspondenceCaptor.getValue();
                assertThat(savedCorrespondence.getReceivingParty()).isEqualTo(Party.APPLICANT);
                assertThat(savedCorrespondence.getSentFrom()).isEqualTo("Criminal Injuries Compensation Tribunal");
                assertThat(savedCorrespondence.getSentTo()).isEqualTo(EMAIL_ADDRESS);
                assertThat(savedCorrespondence.getCorrespondenceType()).isEqualTo("Email");
            }

            @Test
            void savesCorrespondenceWithResponseSentFromEmail() throws NotificationClientException {
                stubPdfGenerationAndUpload();
                stubTemplate(APPLICATION_RECEIVED);
                stubSuccessfulEmailSend();
                when(sendEmailResponse.getFromEmail()).thenReturn(Optional.of("responseEmail@example.com"));

                NotificationRequest request = emailRequestBuilder().build();
                notificationService.sendEmail(request, TEST_CASE_ID.toString(), Party.APPLICANT);

                verify(correspondenceRepository).save(correspondenceCaptor.capture());
                assertThat(correspondenceCaptor.getValue().getSentFrom()).isEqualTo("responseEmail@example.com");
            }

            @Test
            void wrapsIOExceptionWhenReadingResources() throws NotificationClientException {
                try (MockedStatic<IOUtils> mockedIoUtils = mockStatic(IOUtils.class)) {
                    stubTemplate(APPLICATION_RECEIVED);
                    when(notificationClient.sendEmail(eq(TEMPLATE_ID), eq(EMAIL_ADDRESS), any(),anyString())).thenReturn(sendEmailResponse);
                    mockedIoUtils.when(() -> IOUtils.toByteArray((InputStream) any()))
                        .thenThrow(new IOException("failed to read resource"));

                    NotificationRequest request = emailRequestBuilder().build();

                    assertThatThrownBy(() -> notificationService.sendEmail(request, TEST_CASE_ID.toString(), Party.APPLICANT))
                        .isInstanceOf(NotificationException.class)
                        .hasMessageContaining("failed to read resource");
                }
            }
        }

        @Nested
        class SendLetterJourney {
            @Test
            void sendsLetterSuccessfully() throws NotificationClientException {
                stubPdfGenerationAndUpload();
                stubTemplate(CASE_ISSUED_CITIZEN_POST);
                stubSuccessfulLetterSend();

                NotificationRequest request = letterRequestBuilder().build();
                notificationService.sendLetter(request, TEST_CASE_ID.toString());

                verify(notificationClient).sendLetter(eq(TEMPLATE_ID), any(), anyString());
                verify(correspondenceRepository).save(any());
            }

            @Test
            void shouldFallbackToDestinationAddressWhenNoAddressLinesPresent() throws NotificationClientException {
                stubPdfGenerationAndUpload();
                stubTemplate(CASE_ISSUED_CITIZEN_POST);
                stubSuccessfulLetterSend();

                NotificationRequest request = NotificationRequest.builder()
                    .destinationAddress("21 Jump Street")
                    .template(CASE_ISSUED_CITIZEN_POST)
                    .templateVars(new HashMap<>())
                    .build();

                notificationService.sendLetter(request, TEST_CASE_ID.toString());
                verify(correspondenceRepository).save(correspondenceCaptor.capture());
                assertThat(correspondenceCaptor.getValue().getSentTo()).isEqualTo("21 Jump Street");
            }

            @Test
            void shouldSaveAddressWithSeparatedCommas() throws NotificationClientException {
                stubPdfGenerationAndUpload();
                stubTemplate(CASE_ISSUED_CITIZEN_POST);
                stubSuccessfulLetterSend();

                Map<String, Object> templateVars = new HashMap<>();
                templateVars.put("address_line_1", "10 Downing Street");
                templateVars.put("address_line_2", "");
                templateVars.put("address_line_4", "London");
                templateVars.put("address_line_7", "SW1A 2AA");

                NotificationRequest request = NotificationRequest.builder()
                    .template(CASE_ISSUED_CITIZEN_POST)
                    .templateVars(templateVars)
                    .build();

                notificationService.sendLetter(request, TEST_CASE_ID.toString());
                verify(correspondenceRepository).save(correspondenceCaptor.capture());
                assertThat(correspondenceCaptor.getValue().getSentTo()).isEqualTo("10 Downing Street, London, SW1A 2AA");
            }

            @Test
            void throwsWhenNoAddressAvailable() throws NotificationClientException {
                stubTemplate(CASE_ISSUED_CITIZEN_POST);

                NotificationRequest request = letterRequestBuilder()
                    .template(CASE_ISSUED_CITIZEN_POST)
                    .templateVars(new HashMap<>())
                    .build();

                assertThatThrownBy(() -> notificationService.sendLetter(request, TEST_CASE_ID.toString()))
                    .isInstanceOf(NotificationException.class)
                    .hasMessageContaining("Recipient address");
            }

            @Test
            void wrapsNotificationClientExceptionOnSendFailure() throws NotificationClientException {
                stubTemplate(CASE_ISSUED_CITIZEN_POST);
                doThrow(new NotificationClientException("some message"))
                    .when(notificationClient).sendLetter(eq(TEMPLATE_ID), any(), anyString());

                NotificationRequest request = letterRequestBuilder().build();

                assertThatThrownBy(() -> notificationService.sendLetter(request, TEST_CASE_ID.toString()))
                    .isInstanceOf(NotificationException.class)
                    .hasMessageContaining("some message");
            }

            @Test
            void wrapsIOExceptionOnLetterCorrespondence() throws NotificationClientException {
                try (MockedStatic<IOUtils> mockedIOUtils = mockStatic(IOUtils.class)) {
                    stubTemplate(CASE_ISSUED_CITIZEN_POST);
                    when(sendLetterResponse.getBody()).thenReturn("test body");
                    when(notificationClient.sendLetter(eq(TEMPLATE_ID), any(), anyString())).thenReturn(sendLetterResponse);

                    mockedIOUtils.when(() -> IOUtils.toByteArray((InputStream) any()))
                        .thenThrow(new IOException("failed to read resource"));

                    NotificationRequest request = letterRequestBuilder().build();

                    assertThatThrownBy(() -> notificationService.sendLetter(request, TEST_CASE_ID.toString()))
                        .isInstanceOf(NotificationException.class)
                        .hasMessageContaining("failed to read resource");
                }
            }

            @Test
            void wrapsFeignExceptionWhenCorrespondenceUploadFails() throws NotificationClientException {
                stubTemplate(CASE_ISSUED_CITIZEN_POST);
                when(sendLetterResponse.getBody()).thenReturn("test body");
                when(notificationClient.sendLetter(eq(TEMPLATE_ID), any(), anyString())).thenReturn(sendLetterResponse);

                doThrow(mock(FeignException.class))
                    .when(caseDocumentClientAPI).uploadDocuments(any(), any(), any());

                NotificationRequest request = letterRequestBuilder().build();

                assertThatThrownBy(() -> notificationService.sendLetter(request, TEST_CASE_ID.toString()))
                    .isInstanceOf(NotificationException.class);
            }
        }
    }

    @Nested
    class WhenCitizenDashboardEnabled {
        @BeforeEach
        void setDashboardFlag() {
            ReflectionTestUtils.setField(notificationService, "citizenDashboardEnabled", true);
        }

        @Nested
        class SendEmailJourney {
            @Test
            void addSimpleDocumentDetailsAlways() throws NotificationClientException {
                stubAuth();
                stubPdfGenerationAndUpload();
                stubTemplate(CASE_ISSUED_CITIZEN_EMAIL);
                stubSuccessfulEmailSend();

                List<CaseworkerCICDocument> attachedDocument = singleDocumentAttached();
                String documentUuid = DocumentUtil.getDocumentUuidFromCaseworkerCICDocument(attachedDocument.getFirst());
                stubDocumentBinary(documentUuid, SMALL_FILE);

                Map<String, String> templateDocumentVars = new HashMap<>();
                templateDocumentVars.put("DocumentAvailable1", "yes");
                templateDocumentVars.put("CaseDocument1", documentUuid);

                NotificationRequest request = emailRequestBuilder()
                    .template(CASE_ISSUED_CITIZEN_EMAIL)
                    .hasFileAttachments(true)
                    .templateDocumentVars(templateDocumentVars)
                    .build();

                notificationService.sendEmail(request, attachedDocument, TEST_CASE_ID.toString(), Party.SUBJECT);

                verify(notificationClient).sendEmail(eq(TEMPLATE_ID), eq(EMAIL_ADDRESS), templateVarsArgCaptor.capture(), anyString());
                Object details = templateVarsArgCaptor.getValue().get("CaseDocument1");
                assertThat(details).isNotInstanceOf(JSONObject.class)
                    .isInstanceOf(String.class);
                assertThat(details.toString()).contains("Filename:").doesNotContain("Upload Date:");
            }

            @Test
            void addDescriptionWhenDocumentHasEmailContent() throws NotificationClientException {
                stubAuth();
                stubPdfGenerationAndUpload();
                stubTemplate(CASE_ISSUED_CITIZEN_EMAIL);
                stubSuccessfulEmailSend();

                List<CaseworkerCICDocument> attachedDocument = singleDocumentAttached();
                String documentUuid = DocumentUtil.getDocumentUuidFromCaseworkerCICDocument(attachedDocument.getFirst());
                stubDocumentBinary(documentUuid, SMALL_FILE);

                Map<String, String> templateDocumentVars = new HashMap<>();
                templateDocumentVars.put("DocumentAvailable1", "yes");
                templateDocumentVars.put("CaseDocument1", documentUuid);

                NotificationRequest request = emailRequestBuilder()
                    .template(CASE_ISSUED_CITIZEN_EMAIL)
                    .hasFileAttachments(true)
                    .templateDocumentVars(templateDocumentVars)
                    .build();

                notificationService.sendEmail(request, attachedDocument, TEST_CASE_ID.toString(), Party.SUBJECT);

                verify(notificationClient).sendEmail(eq(TEMPLATE_ID), eq(EMAIL_ADDRESS), templateVarsArgCaptor.capture(), anyString());
                assertThat(templateVarsArgCaptor.getValue().get("CaseDocument1").toString()).contains("Description:");
            }
        }
    }

    //setup stub methods
    private void stubAuth() {
        when(idamService.retrieveUser(anyString())).thenReturn(TestDataHelper.getUser());
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
    }

    private void stubPdfGenerationAndUpload() {
        when(pdfServiceClient.generateFromHtml(any(), any())).thenReturn(SMALL_FILE);
        when(caseDocumentClientAPI.uploadDocuments(any(), any(), any())).thenReturn(uploadResponseWithSampleDocument());
    }

    private void stubTemplate(TemplateName templateName) {
        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(Map.of(templateName.name(), TEMPLATE_ID));
    }

    private void stubSuccessfulEmailSend() throws NotificationClientException {
        when(sendEmailResponse.getReference()).thenReturn(Optional.of(randomUUID().toString()));
        when(sendEmailResponse.getNotificationId()).thenReturn(UUID.randomUUID());
        when(notificationClient.sendEmail(eq(TEMPLATE_ID), eq(EMAIL_ADDRESS), any(),anyString())).thenReturn(sendEmailResponse);
    }

    private void stubSuccessfulLetterSend() throws NotificationClientException {
        when(sendLetterResponse.getReference()).thenReturn(Optional.of(UUID.randomUUID().toString()));
        when(sendLetterResponse.getNotificationId()).thenReturn(UUID.randomUUID());
        when(notificationClient.sendLetter(eq(TEMPLATE_ID), any(), anyString())).thenReturn(sendLetterResponse);
    }

    private void stubDocumentBinary(String documentUuid, byte[] content) {
        when(caseDocumentClientAPI.getDocumentBinary(anyString(), anyString(), eq(UUID.fromString(documentUuid))))
            .thenReturn(ResponseEntity.ok(content));
    }

    private NotificationRequest.NotificationRequestBuilder emailRequestBuilder() {
        return NotificationRequest.builder()
            .destinationAddress(EMAIL_ADDRESS)
            .template(APPLICATION_RECEIVED)
            .templateVars(new HashMap<>(BASE_TEMPLATE_VARS))
            .hasFileAttachments(false)
            .templateDocumentVars(new HashMap<>());
    }

    private NotificationRequest.NotificationRequestBuilder letterRequestBuilder() {
        Map<String, Object> letterVars = new HashMap<>();
        letterVars.put("address_line_1", "Buckingham Palace");
        letterVars.put("address_line_4", "London");
        letterVars.put("address_line_7", "SW1A 1AA");
        return NotificationRequest.builder()
            .template(CASE_ISSUED_CITIZEN_POST)
            .templateVars(letterVars);
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
