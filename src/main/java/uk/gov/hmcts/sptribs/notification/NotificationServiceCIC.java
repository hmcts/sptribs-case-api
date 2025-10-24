package uk.gov.hmcts.sptribs.notification;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationType;
import uk.gov.hmcts.sptribs.common.config.EmailTemplatesConfigCIC;
import uk.gov.hmcts.sptribs.common.repositories.CorrespondenceRepository;
import uk.gov.hmcts.sptribs.document.DocumentClient;
import uk.gov.hmcts.sptribs.document.SerializableDocument;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.notification.persistence.CorrespondenceEntity;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendLetterResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DOC_AVAILABLE;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.BEARER_PREFIX;

@Service
@Slf4j
public class NotificationServiceCIC {

    @Autowired
    private final CorrespondenceRepository correspondenceRepository;

    @Autowired
    private final NotificationClient notificationClient;

    @Autowired
    private final EmailTemplatesConfigCIC emailTemplatesConfig;

    @Autowired
    private final IdamService idamService;

    @Autowired
    private final HttpServletRequest request;

    @Autowired
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    private final DocumentClient caseDocumentClient;

    @Autowired
    private final CaseDocumentClientApi caseDocumentClientApi;

    @Autowired
    private final PDFServiceClient pdfServiceClient;

    public NotificationServiceCIC(NotificationClient notificationClient,
                                  EmailTemplatesConfigCIC emailTemplatesConfig,
                                  IdamService idamService, HttpServletRequest request,
                                  AuthTokenGenerator authTokenGenerator,
                                  DocumentClient caseDocumentClient,
                                  CorrespondenceRepository correspondenceRepository,
                                  CaseDocumentClientApi caseDocumentClientApi,
                                  PDFServiceClient pdfServiceClient) {

        this.notificationClient = notificationClient;
        this.emailTemplatesConfig = emailTemplatesConfig;
        this.idamService = idamService;
        this.request = request;
        this.authTokenGenerator = authTokenGenerator;
        this.caseDocumentClient = caseDocumentClient;
        this.correspondenceRepository = correspondenceRepository;
        this.caseDocumentClientApi = caseDocumentClientApi;
        this.pdfServiceClient = pdfServiceClient;
    }

    public void saveEmailCorrespondence(String templateName,
                                        SendEmailResponse sendEmailResponse,
                                        String sentTo,
                                        String caseReferenceNumber) {

        Long longCaseRef = Long.parseLong(caseReferenceNumber.replace("-", ""));
        final LocalDateTime sentAt = LocalDateTime.now();
        String sentFrom = "Criminal Injuries Compensation Tribunal";

        if (sendEmailResponse.getFromEmail().isPresent()) {
            sentFrom = sendEmailResponse.getFromEmail().get();
        }

        try {
            CorrespondenceEntity correspondence = CorrespondenceEntity.builder()
                .id(sendEmailResponse.getNotificationId())
                .eventType(templateName)
                .caseReferenceNumber(longCaseRef)
                .sentAt(sentAt.atOffset(java.time.ZoneOffset.UTC))
                .sentFrom(sentFrom)
                .sentTo(sentTo)
                .documentUrl(this.getPDF(sendEmailResponse, null, longCaseRef, sentAt, sentFrom, sentTo, templateName))
                .correspondenceType("Email")
                .build();

            correspondenceRepository.save(correspondence);
        } catch (java.io.IOException e) {
            log.error("Failed to store pdf document", e);
        }

    }

    public void saveLetterCorrespondence(String templateName,
                                         SendLetterResponse sendLetterResponse,
                                         String sentTo,
                                         String caseReferenceNumber) {

        Long longCaseRef = Long.parseLong(caseReferenceNumber.replace("-", ""));
        final LocalDateTime sentAt = LocalDateTime.now();
        String sentFrom = "Criminal Injuries Compensation Tribunal";

        try {
            CorrespondenceEntity correspondence = CorrespondenceEntity.builder()
                .id(sendLetterResponse.getNotificationId())
                .eventType(templateName)
                .caseReferenceNumber(longCaseRef)
                .sentAt(sentAt.atOffset(java.time.ZoneOffset.UTC))
                .sentFrom(sentFrom)
                .sentTo(sentTo)
                .documentUrl(this.getPDF(null, sendLetterResponse, longCaseRef, sentAt, sentFrom, sentTo, templateName))
                .correspondenceType("Letter")
                .build();

            correspondenceRepository.save(correspondence);
        } catch (java.io.IOException e) {
            log.error("Failed to store pdf document", e);
        }
    }

    public NotificationResponse sendEmail(NotificationRequest notificationRequest, String caseReferenceNumber) {
        final SendEmailResponse sendEmailResponse;
        final String destinationAddress = notificationRequest.getDestinationAddress();
        final TemplateName template = notificationRequest.getTemplate();
        final Map<String, Object> templateVars = notificationRequest.getTemplateVars();
        final String templateName = template.name();

        final String referenceId = UUID.randomUUID().toString();

        try {
            if (notificationRequest.isHasFileAttachments()) {
                addAttachmentsToTemplateVars(templateVars, notificationRequest.getUploadedDocuments());
            }

            String templateId = emailTemplatesConfig.getTemplatesCIC().get(template.name());

            sendEmailResponse =
                notificationClient.sendEmail(
                    templateId,
                    destinationAddress,
                    templateVars,
                    referenceId
                );

            this.saveEmailCorrespondence(
                templateName,
                sendEmailResponse,
                destinationAddress,
                caseReferenceNumber
            );

            log.debug("Successfully sent email with notification id {} and reference {}",
                sendEmailResponse.getNotificationId(),
                sendEmailResponse.getReference().orElse(referenceId)
            );

            return getNotificationResponse(sendEmailResponse);
        } catch (NotificationClientException notificationClientException) {
            log.error("Failed to send email. Reference ID: {}. Reason: {}",
                referenceId,
                notificationClientException.getMessage(),
                notificationClientException
            );
            throw new NotificationException(notificationClientException);
        } catch (IOException ioException) {
            log.error("Issue with attach documents to Notification. Failed to send email. Reference ID: {}. Reason: {}",
                referenceId,
                ioException.getMessage(),
                ioException
            );
            throw new NotificationException(ioException);
        }
    }

    public NotificationResponse sendLetter(NotificationRequest notificationRequest, String caseReferenceNumber) {
        final TemplateName template = notificationRequest.getTemplate();
        final Map<String, Object> templateVars = notificationRequest.getTemplateVars();
        final String templateName = template.name();

        final String referenceId = UUID.randomUUID().toString();

        try {
            final String templateId = emailTemplatesConfig.getTemplatesCIC().get(template.name());

            SendLetterResponse sendLetterResponse =
                notificationClient.sendLetter(
                    templateId,
                    templateVars,
                    referenceId
                );

            this.saveLetterCorrespondence(
                templateName,
                sendLetterResponse,
                notificationRequest.getDestinationAddress(),
                caseReferenceNumber
            );

            log.debug("Successfully sent letter with notification id {} and reference {}",
                sendLetterResponse.getNotificationId(),
                sendLetterResponse.getReference().orElse(referenceId)
            );
            return getLetterNotificationResponse(sendLetterResponse);
        } catch (NotificationClientException notificationClientException) {
            log.error("Failed to send letter. Reference ID: {}. Reason: {}",
                referenceId,
                notificationClientException.getMessage(),
                notificationClientException
            );
            throw new NotificationException(notificationClientException);
        }
    }

    private void addAttachmentsToTemplateVars(Map<String, Object> templateVars, Map<String, String> uploadedDocuments) throws IOException {

        final User user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        final String authorisation = user.getAuthToken().startsWith(BEARER_PREFIX)
            ? user.getAuthToken() : BEARER_PREFIX + user.getAuthToken();
        final String serviceAuthorization = authTokenGenerator.generate();
        final String serviceAuthorizationLatest = serviceAuthorization.startsWith(BEARER_PREFIX)
            ? serviceAuthorization.substring(7) : serviceAuthorization;

        for (Map.Entry<String, String> document : uploadedDocuments.entrySet()) {
            final String docName = document.getKey();
            final String item = document.getValue();

            if (docName.contains(DOC_AVAILABLE)) {
                templateVars.put(docName, item);
            } else {
                if (StringUtils.isNotEmpty(item)) {
                    byte[] uploadedDocument = caseDocumentClient
                        .getDocumentBinary(authorisation, serviceAuthorizationLatest, UUID.fromString(item)).getBody();

                    if (uploadedDocument != null) {
                        log.debug("Document available for: {}", docName);
                        templateVars.put(docName, getJsonFileAttachment(uploadedDocument));
                    } else {
                        templateVars.put(docName, "");
                    }
                } else {
                    log.info("Document not available for: {}", docName);
                    templateVars.put(docName, "");
                }
            }
        }
    }

    private JSONObject getJsonFileAttachment(byte[] fileContents) {
        JSONObject jsonObject = null;
        try {
            if (Objects.nonNull(fileContents)) {
                jsonObject = NotificationClient.prepareUpload(fileContents);
            }
        } catch (NotificationClientException e) {
            log.error("Unable to upload file to Notification - {}", e.getMessage());
            throw new NotificationException(e);
        }
        return jsonObject;
    }

    private NotificationResponse getNotificationResponse(final SendEmailResponse sendEmailResponse) {
        final Optional<String> reference = sendEmailResponse.getReference();
        String clientReference = null;
        if (reference.isPresent()) {
            clientReference = reference.get();
        }

        return NotificationResponse.builder()
            .id(sendEmailResponse.getNotificationId().toString())
            .clientReference(clientReference)
            .notificationType(NotificationType.EMAIL)
            .updatedAtTime(LocalDateTime.now())
            .createdAtTime(LocalDateTime.now())
            .status("Received")
            .build();
    }

    private NotificationResponse getLetterNotificationResponse(final SendLetterResponse sendLetterResponse) {
        final Optional<String> reference = sendLetterResponse.getReference();
        String clientReference = null;
        if (reference.isPresent()) {
            clientReference = reference.get();
        }

        return NotificationResponse.builder()
            .id(sendLetterResponse.getNotificationId().toString())
            .clientReference(clientReference)
            .notificationType(NotificationType.POST)
            .updatedAtTime(LocalDateTime.now())
            .createdAtTime(LocalDateTime.now())
            .status("Received")
            .build();
    }

    private SerializableDocument getPDF(SendEmailResponse sendEmailResponse,
                                        SendLetterResponse sendLetterResponse,
                                        Long longCaseRef,
                                        LocalDateTime sentAt,
                                        String sentFrom,
                                        String sentTo,
                                        String templateName) throws IOException {
        Map<String, Object> placeholders = new HashMap<>();
        String correspondenceType = "Email";
        if (sendEmailResponse != null) {
            placeholders.put("body", sendEmailResponse.getBody());
            placeholders.put("subject", sendEmailResponse.getSubject());
        } else if (sendLetterResponse != null) {
            placeholders.put("body", sendLetterResponse.getBody());
            placeholders.put("subject", sendLetterResponse.getSubject());
            correspondenceType = "Letter";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-y-HH-mm");
        placeholders.put("sentOn", sentAt.format(formatter));
        placeholders.put("from", sentFrom);
        placeholders.put("to", sentTo);

        byte[] template;
        try {
            template = IOUtils.toByteArray(Objects.requireNonNull(
                getClass().getResourceAsStream("/templates/sent_notification.html")
            ));
        } catch (IOException e) {
            log.info("Failed to get resource with exception {}", e.getMessage());
            throw e;
        }

        byte[] pdf = pdfServiceClient.generateFromHtml(template, placeholders);

        final String formattedSentOn = sentAt.format(formatter)
            .replace(" ", "_").replace(":", "-");

        String correspondenceDocumentFilename = templateName + "_" + longCaseRef + "_" + formattedSentOn + ".pdf";

        final InMemoryMultipartFile inMemoryMultipartFile = new InMemoryMultipartFile(correspondenceDocumentFilename, pdf);

        try {
            log.info("Storing file {} of type {} into secure docstore",
                correspondenceDocumentFilename, correspondenceType);

            String serviceToken = authTokenGenerator.generate();
            String authorizationHeader = request.getHeader(AUTHORIZATION);

            DocumentUploadRequest documentUploadRequest = new DocumentUploadRequest(
                Classification.RESTRICTED.toString(),
                "CriminalInjuriesCompensation",
                "ST_CIC",
                singletonList(inMemoryMultipartFile)
            );

            UploadResponse uploadResponse = caseDocumentClientApi.uploadDocuments(authorizationHeader, serviceToken, documentUploadRequest);
            Document uploadedPDF = new Document();
            uploadedPDF.setBinaryUrl(uploadResponse.getDocuments().getFirst().links.binary.href);
            uploadedPDF.setFilename(correspondenceDocumentFilename);
            uploadedPDF.setUrl(uploadResponse.getDocuments().getFirst().links.self.href);

            return SerializableDocument.builder()
                    .url(uploadedPDF.getUrl())
                    .filename(uploadedPDF.getFilename())
                    .binaryUrl(uploadedPDF.getBinaryUrl())
                    .build();
        } catch (RestClientException e) {
            log.error("Failed to store pdf document but carrying on [" + correspondenceDocumentFilename + "]", e);
            return null;
        }
    }
}
