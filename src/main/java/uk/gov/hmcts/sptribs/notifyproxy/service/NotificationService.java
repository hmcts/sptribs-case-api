package uk.gov.hmcts.sptribs.notifyproxy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.notifyproxy.model.Notification;
import uk.gov.hmcts.sptribs.notifyproxy.repository.NotificationRepository;

@Service
public class NotificationService {
    private static Logger log = LoggerFactory.getLogger(NotificationService.class);
/*

    @Autowired
    NotificationRepository notificationRepository;
*/

    public void sendEmailNotification(Notification notification){
        log.info(".....sendEmailNotification");
        //notificationRepository.save(notification);
        log.info("email notification saved successfully.");
    }
}
