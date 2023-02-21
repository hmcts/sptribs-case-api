package uk.gov.hmcts.sptribs.notification;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationType;
import uk.gov.hmcts.sptribs.common.config.EmailTemplatesConfigCIC;
import uk.gov.hmcts.sptribs.document.CaseDocumentClient;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendLetterResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ATTACHMENT_COUNT;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DECISION_NOTICE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DOC_AVAILABLE;

@Service
@Slf4j
public class NotificationServiceCIC {

    private static final int DOC_ATTACH_LIMIT = 5;

    private NotificationRequest notificationRequest;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private EmailTemplatesConfigCIC emailTemplatesConfig;

    @Autowired
    private IdamService idamService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CaseDocumentClient caseDocumentClient;

    public NotificationResponse sendEmail() {
        SendEmailResponse sendEmailResponse;
        String destinationAddress = notificationRequest.getDestinationAddress();
        TemplateName template = notificationRequest.getTemplate();
        Map<String, Object> templateVars = notificationRequest.getTemplateVars();

        String referenceId = UUID.randomUUID().toString();

        try {
            if (notificationRequest.isHasFileAttachments()) {
                addAttachmentsToTemplateVars(templateVars, notificationRequest.getUploadedDocuments());
            }

            String templateId = emailTemplatesConfig.getTemplatesCIC().get(template.name());

            log.info("Sending email for reference id : {} using template : {}", referenceId, templateId);

            sendEmailResponse =
                notificationClient.sendEmail(
                    templateId,
                    destinationAddress,
                    templateVars,
                    referenceId
                );

            log.info("Successfully sent email with notification id {} and reference {}",
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
        }
    }

    public NotificationResponse sendLetter() {
        TemplateName template = notificationRequest.getTemplate();
        Map<String, Object> templateVars = notificationRequest.getTemplateVars();

        String referenceId = UUID.randomUUID().toString();

        try {
            String templateId = emailTemplatesConfig.getTemplatesCIC().get(template.name());

            log.info("Sending letter for reference id : {} using template : {}", referenceId, templateId);

            SendLetterResponse sendLetterResponse =
                notificationClient.sendLetter(
                    templateId,
                    templateVars,
                    referenceId
                );

            log.info("Successfully sent letter with notification id {} and reference {}",
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

    public void setNotificationRequest(NotificationRequest notificationRequest) {
        this.notificationRequest = notificationRequest;
    }

    @SuppressWarnings("unchecked")
    private void addAttachmentsToTemplateVars(Map<String, Object> templateVars, Map<String, String> uploadedDocuments) {

        final User caseworkerUser = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        final String authorisation = caseworkerUser.getAuthToken();
        String serviceAuthorization = authTokenGenerator.generate();

        uploadedDocuments.forEach((docName, item) -> {

            if (docName.contains(DOC_AVAILABLE)) {
                templateVars.put(docName, item);
            } else {
                final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);
                ResponseEntity<Resource> responseEntity = new ResponseEntity(HttpStatus.OK);

                /*Resource uploadedDocument = caseDocumentClient.getDocumentBinary(authorisation,
                    serviceAuthorization,
                    UUID.fromString(item)).getBody();*/

                if (responseEntity != null) {
                    byte[] uploadedDocumentContents = getUploadedDocumentContents(responseEntity.getBody());
                    templateVars.put(docName, getJsonFileAttachment(uploadedDocumentContents));
                } else {
                    log.info("Document not found with uuid : {}", UUID.fromString(item));
                    templateVars.put(docName, "");
                }
            }
            /*final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);
            ResponseEntity<Resource> responseEntity = new ResponseEntity(HttpStatus.OK);*/
        });
    }

    private byte[] getUploadedDocumentContents(Resource uploadedDocument) {
        /*byte[] uploadedDocumentContents;
        try {
            uploadedDocumentContents = uploadedDocument.getInputStream().readAllBytes();
        } catch (IOException e) {
            uploadedDocumentContents = null;
        }
        return uploadedDocumentContents;*/
        return null;
    }

    private JSONObject getJsonFileAttachment(byte[] fileContents) {
        JSONObject jsonObject = null;
        try {
            if (Objects.nonNull(fileContents)) {
                jsonObject = NotificationClient.prepareUpload(fileContents);
            }
        } catch (NotificationClientException e) {
            log.info("unable to upload", e.getMessage());
        }
        return jsonObject;
    }

    private NotificationResponse getNotificationResponse(final SendEmailResponse sendEmailResponse) {
        Optional<String> reference = sendEmailResponse.getReference();
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
        Optional<String> reference = sendLetterResponse.getReference();
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
}
