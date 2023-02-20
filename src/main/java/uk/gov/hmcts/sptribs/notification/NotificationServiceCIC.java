package uk.gov.hmcts.sptribs.notification;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
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
                addAttachmentsToTemplateVars(templateVars, notificationRequest.getUploadedDocumentIds());
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
        } catch (IOException ioException) {
            log.error("Issue with attach documents to Notification. Failed to send email. Reference ID: {}. Reason: {}",
                referenceId,
                ioException.getMessage(),
                ioException
            );
            throw new NotificationException(ioException);
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

    private void addAttachmentsToTemplateVars(Map<String, Object> templateVars, List<String> uploadedDocumentIds) throws IOException {
        int count = 0;

        final User caseworkerUser = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        final String authorisation = caseworkerUser.getAuthToken();
        String serviceAuthorization = authTokenGenerator.generate();

        for (String item : uploadedDocumentIds) {
            count++;

            Resource uploadedDocument = caseDocumentClient.getDocumentBinary(authorisation,
                serviceAuthorization,
                UUID.fromString(item)).getBody();

            if (uploadedDocument != null) {
                log.info("Document found with uuid : {}", UUID.fromString(item));
                byte[] uploadedDocumentContents = uploadedDocument.getInputStream().readAllBytes();
                templateVars.put(DOC_AVAILABLE + count, "yes");
                templateVars.put(DECISION_NOTICE + count, getJsonFileAttachment(uploadedDocumentContents));
            } else {
                log.info("Document not found with uuid : {}", UUID.fromString(item));
                templateVars.put(DOC_AVAILABLE + count, "no");
                templateVars.put(DECISION_NOTICE + count, "");
            }
        }

        while (count < DOC_ATTACH_LIMIT) {
            count++;
            templateVars.put(DOC_AVAILABLE + count, "no");
            templateVars.put(DECISION_NOTICE + count, "");
        }
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

    public void setNotificationRequest(NotificationRequest notificationRequest) {
        this.notificationRequest = notificationRequest;
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
