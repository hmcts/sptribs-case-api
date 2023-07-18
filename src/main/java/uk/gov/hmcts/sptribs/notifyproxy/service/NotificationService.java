package uk.gov.hmcts.sptribs.notifyproxy.service;

import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.sptribs.notifyproxy.dtos.NotificationEmailRequest;
import uk.gov.service.notify.SendEmailResponse;

public interface NotificationService {

    SendEmailResponse sendEmailNotification(NotificationEmailRequest emailNotificationRequest, MultiValueMap<String, String> headers);


}
