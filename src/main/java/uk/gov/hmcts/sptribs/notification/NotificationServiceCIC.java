package uk.gov.hmcts.sptribs.notification;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
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
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
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
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DOC_AVAILABLE;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceCIC {

    private final CorrespondenceRepository correspondenceRepository;

    private final NotificationClient notificationClient;

    private final EmailTemplatesConfigCIC emailTemplatesConfig;

    private final IdamService idamService;

    private final HttpServletRequest request;

    private final AuthTokenGenerator authTokenGenerator;

    private final CaseDocumentClientApi caseDocumentClientApi;

    private final PDFServiceClient pdfServiceClient;

    private static final int FIRST_ADDRESS_LINE = 1;

    private static final int LAST_ADDRESS_LINE = 7;

    private static final long TWO_MEGABYTES = 2_048_000;

    public void saveEmailCorrespondence(String templateName,
                                        SendEmailResponse sendEmailResponse,
                                        String sentTo,
                                        String caseReferenceNumber) throws IOException, RestClientException {

        Long longCaseRef = Long.parseLong(caseReferenceNumber.replace("-", ""));
        final OffsetDateTime sentOn = OffsetDateTime.now(ZoneOffset.UTC);
        String sentFrom = "Criminal Injuries Compensation Tribunal";

        if (sendEmailResponse.getFromEmail().isPresent()) {
            sentFrom = sendEmailResponse.getFromEmail().get();
        }

        try {
            Document correspondencePDF = this.getPDF(sendEmailResponse, null, longCaseRef, sentOn, sentFrom, sentTo, templateName);
            CorrespondenceEntity correspondence = CorrespondenceEntity.builder()
                .id(sendEmailResponse.getNotificationId())
                .eventType(templateName)
                .caseReferenceNumber(longCaseRef)
                .sentOn(sentOn)
                .sentFrom(sentFrom)
                .sentTo(sentTo)
                .documentUrl(correspondencePDF.getUrl())
                .documentFilename(correspondencePDF.getFilename())
                .documentBinaryUrl(correspondencePDF.getBinaryUrl())
                .correspondenceType("Email")
                .build();
            correspondenceRepository.save(correspondence);
        } catch (java.io.IOException | RestClientException e) {
            log.error("Failed to store pdf document", e);
            throw e;
        }

    }

    public void saveLetterCorrespondence(String templateName,
                                         SendLetterResponse sendLetterResponse,
                                         String sentTo,
                                         String caseReferenceNumber) throws IOException, RestClientException {

        Long longCaseRef = Long.parseLong(caseReferenceNumber.replace("-", ""));
        final OffsetDateTime sentOn = OffsetDateTime.now(ZoneOffset.UTC);
        String sentFrom = "Criminal Injuries Compensation Tribunal";
        try {
            Document correspondencePDF = this.getPDF(null, sendLetterResponse, longCaseRef, sentOn, sentFrom, sentTo, templateName);
            CorrespondenceEntity correspondence = CorrespondenceEntity.builder()
                .id(sendLetterResponse.getNotificationId())
                .eventType(templateName)
                .caseReferenceNumber(longCaseRef)
                .sentOn(sentOn)
                .sentFrom(sentFrom)
                .sentTo(sentTo)
                .documentUrl(correspondencePDF.getUrl())
                .documentFilename(correspondencePDF.getFilename())
                .documentBinaryUrl(correspondencePDF.getBinaryUrl())
                .correspondenceType("Letter")
                .build();
            correspondenceRepository.save(correspondence);
        } catch (java.io.IOException | RestClientException e) {
            log.error("Failed to store pdf document", e);
            throw e;
        }
    }

    public NotificationResponse sendEmail(NotificationRequest notificationRequest, String caseReferenceNumber) {
        return sendEmail(notificationRequest, Collections.emptyList(), caseReferenceNumber);
    }

    public NotificationResponse sendEmail(NotificationRequest notificationRequest,
                                          List<CaseworkerCICDocument> selectedDocuments,
                                          String caseReferenceNumber) {
        final SendEmailResponse sendEmailResponse;
        final String destinationAddress = notificationRequest.getDestinationAddress();
        final TemplateName template = notificationRequest.getTemplate();
        final Map<String, Object> templateVars = notificationRequest.getTemplateVars();
        final String templateName = template.name();

        final String referenceId = UUID.randomUUID().toString();

        try {
            if (notificationRequest.isHasFileAttachments()) {
                addAttachmentsToTemplateVars(templateVars, notificationRequest.getUploadedDocuments(), selectedDocuments);
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
        } catch (NotificationClientException | IOException e) {
            log.error("Failed to send email. Reference ID: {}. Reason: {}",
                referenceId,
                e.getMessage(),
                e
            );
            throw new NotificationException(e);
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

            StringBuilder address = new StringBuilder();

            for (int i = FIRST_ADDRESS_LINE; i <= LAST_ADDRESS_LINE; i++) {
                Object addressLineValue = templateVars.get("address_line_" + i);
                if ((addressLineValue != null)
                    && !addressLineValue.toString().isEmpty()) {
                    address.append(addressLineValue.toString().trim());
                }
                if ((i != 7)
                    && (addressLineValue != null)
                    && !addressLineValue.toString().isEmpty()) {
                    address.append(", ");
                }
            }

            String formattedAddress = !address.isEmpty()
                ? address.toString()
                : Objects.toString(notificationRequest.getDestinationAddress(), "");

            if (formattedAddress.isEmpty()) {
                throw new NullArgumentException("Recipient address");
            }

            saveLetterCorrespondence(
                templateName,
                sendLetterResponse,
                formattedAddress,
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
        } catch (IOException ioException) {
            log.error("Failed to get PDF of correspondence. Reference ID: {}. Reason: {}",
                referenceId,
                ioException.getMessage(),
                ioException
            );
            throw new NotificationException(ioException);
        } catch (NullArgumentException nullArgumentException) {
            log.error("Failed to send letter due to missing data. Reference ID: {}. Reason: {}",
                referenceId,
                nullArgumentException.getMessage(),
                nullArgumentException
            );
            throw new NotificationException(nullArgumentException);
        }
    }

    private void addAttachmentsToTemplateVars(Map<String, Object> templateVars,
                                              Map<String, String> uploadedDocuments,
                                              List<CaseworkerCICDocument> selectedDocuments) {
        for (Map.Entry<String, String> uploadDocumentEntry : uploadedDocuments.entrySet()) {
            final String docName = uploadDocumentEntry.getKey();
            final String item = uploadDocumentEntry.getValue();

            if (docName.contains(DOC_AVAILABLE)) {
                templateVars.put(docName, item);
            } else {
                addLinkOrDocumentDetails(templateVars, selectedDocuments, item, docName);
            }
        }
    }

    private void addLinkOrDocumentDetails(Map<String, Object> templateVars,
                                          List<CaseworkerCICDocument> selectedDocuments,
                                          String item,
                                          String docName) {
        final User user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        final String authorisation = user.getAuthToken();
        final String serviceAuthorization = authTokenGenerator.generate();

        if (StringUtils.isNotEmpty(item)) {
            ResponseEntity<byte[]> documentBinaryResponse =
                caseDocumentClientApi.getDocumentBinary(authorisation, serviceAuthorization, UUID.fromString(item));
            if (!documentBinaryResponse.getStatusCode().is2xxSuccessful()) {
                throw new NotificationException(
                    new Exception(String.format("Failed to get document binary for id %s", item)));
            }

            byte[] uploadedDocument = documentBinaryResponse.getBody();
            if (uploadedDocument != null) {
                log.debug("Document available for: {}", docName);

                if (uploadedDocument.length <= TWO_MEGABYTES) {
                    templateVars.put(docName, getJsonFileAttachment(uploadedDocument));
                } else {
                    addDocumentDetails(templateVars, selectedDocuments, item, docName);
                }
            } else {
                templateVars.put(docName, "");
            }
        } else {
            log.info("Document not available for: {}", docName);
            templateVars.put(docName, "");
        }
    }

    private static void addDocumentDetails(Map<String, Object> templateVars,
                                           List<CaseworkerCICDocument> selectedDocuments,
                                           String item,
                                           String docName) {
        CaseworkerCICDocument document = selectedDocuments.stream()
            .filter(doc -> doc.getDocumentLink().getBinaryUrl().contains(item))
            .findFirst()
            .orElseThrow(() -> new NotificationException(
                new Exception(String.format("Unable to find document details for document id: %s", item))));

        String documentNotification = String.format("%nFilename: %s%nDescription: %s%nUpload Date: %s",
            document.getDocumentLink().getFilename(), document.getDocumentEmailContent(), document.getDate());
        templateVars.put(docName, documentNotification);
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

    private Document getPDF(SendEmailResponse sendEmailResponse,
                                        SendLetterResponse sendLetterResponse,
                                        Long longCaseRef,
                                        OffsetDateTime sentOn,
                                        String sentFrom,
                                        String sentTo,
                                        String templateName) throws IOException, RestClientException {
        Map<String, Object> placeholders = new HashMap<>();
        String correspondenceType = "Email";
        if (sendEmailResponse != null) {
            placeholders.put("body", sendEmailResponse.getBody());
            placeholders.put("subject", sendEmailResponse.getSubject());
        } else {
            placeholders.put("body", sendLetterResponse.getBody());
            placeholders.put("subject", sendLetterResponse.getSubject());
            correspondenceType = "Letter";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-y-HH-mm");
        placeholders.put("sentOn", sentOn.format(formatter));
        placeholders.put("from", sentFrom);
        placeholders.put("to", sentTo);

        byte[] template;
        try (InputStream inputStream = getClass().getResourceAsStream("/templates/sent_notification.html")) {
            template = IOUtils.toByteArray(Objects.requireNonNull(inputStream));
        } catch (IOException e) {
            log.info("Failed to get resource with exception {}", e.getMessage());
            throw e;
        }

        byte[] pdf = pdfServiceClient.generateFromHtml(template, placeholders);

        final String formattedSentOn = sentOn.format(formatter)
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

            return uploadedPDF;
        } catch (RestClientException e) {
            log.error("Failed to store correspondence document [" + correspondenceDocumentFilename + "]", e);
            throw e;
        }
    }
}
