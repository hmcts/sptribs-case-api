package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

@Component
@Slf4j
public class AnonymityAppliedNotification implements PartiesNotification {

    private final NotificationServiceCIC notificationService;
    private final NotificationHelper notificationHelper;
    private final String recipientEmail;

    @Autowired
    public AnonymityAppliedNotification(NotificationServiceCIC notificationService,
                                        NotificationHelper notificationHelper,
                                        @Value("${sptribs.notifications.anonymity.recipient_email:}") String recipientEmail) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
        this.recipientEmail = recipientEmail;
    }

    @Override
    public void sendToTribunal(final CaseData caseData, final String caseNumber) {
        if (!isRecipientConfigured()) {
            log.warn("Skipping anonymity notification because recipient email is not configured");
            return;
        }

        final Map<String, Object> templateVars = buildTemplateVars(caseData, caseNumber);
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            recipientEmail,
            templateVars,
            TemplateName.ANONYMITY_APPLIED_EMAIL
        );

        NotificationResponse response = notificationService.sendEmail(request, caseNumber);
        caseData.getCicCase().setTribunalNotificationResponse(response);
    }

    private Map<String, Object> buildTemplateVars(CaseData caseData, String caseNumber) {
        return notificationHelper.getTribunalCommonVars(caseNumber, caseData);
    }

    private boolean isRecipientConfigured() {
        return recipientEmail != null && !recipientEmail.isBlank();
    }
}
