package uk.gov.hmcts.sptribs.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationType;
import uk.gov.hmcts.sptribs.common.config.EmailTemplatesConfigCIC;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class NotificationServiceCIC {

    private NotificationRequest notificationRequest;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private EmailTemplatesConfigCIC emailTemplatesConfig;

    public NotificationResponse sendEmail() {
        SendEmailResponse sendEmailResponse = null;
        String destinationAddress = notificationRequest.getDestinationAddress();
        EmailTemplateName template = notificationRequest.getTemplate();
        Map<String, Object> templateVars = notificationRequest.getTemplateVars();

        String referenceId = UUID.randomUUID().toString();

        try {
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

}
