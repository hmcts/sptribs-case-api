package uk.gov.hmcts.sptribs.systemupdate.schedule.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import lombok.extern.slf4j.Slf4j;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.NotificationList;

@Slf4j
@Component
public class SystemGetCorrespondence implements Runnable {

    @Autowired
    private NotificationServiceCIC notificationService;

    @Override
    public void run() {
        try {
            NotificationList emailNotifications = this.notificationService.getNotifications("email");
            log.info("Fetched {} email notifications", emailNotifications.getNotifications().size());

            NotificationList letterNotifications = this.notificationService.getNotifications("letter");
            log.info("Fetched {} letter notifications", letterNotifications.getNotifications().size());

            NotificationList smsNotifications = this.notificationService.getNotifications("sms");
            log.info("Fetched {} SMS notifications", smsNotifications.getNotifications().size());
        } catch (NotificationClientException e) {
            log.error("Failed to fetch notifications: {}", e.getMessage());
        }
    }

}
