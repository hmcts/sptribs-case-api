package uk.gov.hmcts.sptribs.notification;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.sptribs.common.config.ControllerConstants;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@FeignClient(name = "notify-proxy-api", url = "${notify_proxy.url}")
public interface NotifyProxyClient {

    @PostMapping(value = "/notifications/email",
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<Resource>  sendEmailNotification(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                                   @RequestHeader(ControllerConstants.SERVICE_AUTHORIZATION) String serviceAuth,
                                                   @RequestHeader MultiValueMap<String, String> headers,
                                                   NotificationRequest notificationEmailRequest);
}
