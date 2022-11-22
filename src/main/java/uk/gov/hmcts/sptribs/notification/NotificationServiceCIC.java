package uk.gov.hmcts.sptribs.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.common.config.EmailTemplatesConfigCIC;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendLetterResponse;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class NotificationServiceCIC {

    private NotificationRequest notificationRequest;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private EmailTemplatesConfigCIC emailTemplatesConfig;

    public void sendEmail() {
        String destinationAddress = notificationRequest.getDestinationAddress();
        EmailTemplateName template = notificationRequest.getTemplate();
        Map<String, String> templateVars = notificationRequest.getTemplateVars();

        String referenceId = UUID.randomUUID().toString();

        try {
            String templateId = emailTemplatesConfig.getTemplatesCIC().get(template.name());

            log.info("Sending email for reference id : {} using template : {}", referenceId, templateId);

            SendEmailResponse sendEmailResponse =
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

        } catch (NotificationClientException notificationClientException) {
            log.error("Failed to send email. Reference ID: {}. Reason: {}",
                referenceId,
                notificationClientException.getMessage(),
                notificationClientException
            );
            throw new NotificationException(notificationClientException);
        }
    }

    public void sendLetter() {
        EmailTemplateName template = notificationRequest.getTemplate();
        Map<String, String> templateVars = notificationRequest.getTemplateVars();

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
}
