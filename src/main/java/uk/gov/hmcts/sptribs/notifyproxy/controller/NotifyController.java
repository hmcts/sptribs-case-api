package uk.gov.hmcts.sptribs.notifyproxy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.sptribs.notifyproxy.model.Notification;
import uk.gov.hmcts.sptribs.notifyproxy.service.NotificationService;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@RestController
public class NotifyController {

    private static Logger log = LoggerFactory.getLogger(NotifyController.class);

    @Autowired
    NotificationService notificationService;

    @PostMapping("/notifications/{caseId}/email")
    public ResponseEntity emailNotification(
        @RequestHeader(AUTHORIZATION) final String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        /*@Valid*/ @RequestBody Notification request) {
        log.info("recipientEmailAddress in request  for /email endpoint {}",request.getNotificationType());
        log.info("reference in request  for /email endpoint {}",request.getReference());
        notificationService.sendEmailNotification(request);
        return new ResponseEntity<>(
            "Notification sent successfully via email", HttpStatus.CREATED);
    }
}
