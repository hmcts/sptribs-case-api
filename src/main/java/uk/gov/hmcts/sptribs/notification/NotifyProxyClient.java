package uk.gov.hmcts.sptribs.notification;

import org.apache.http.HttpHeaders;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.sptribs.common.config.ControllerConstants;
import uk.gov.hmcts.sptribs.document.DocumentConstants;
import uk.gov.hmcts.sptribs.notifyproxy.model.Notification;

import java.util.UUID;

@FeignClient(name = "notify-proxy-api", url = "${notify_proxy.url}//notifications")
public interface NotifyProxyClient {

    @PostMapping(value = "/{caseId}/email")
    ResponseEntity<Resource> sendEmailNotification(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                                   @RequestHeader(ControllerConstants.SERVICE_AUTHORIZATION) String serviceAuth,
                                                   @PathVariable final Long  caseId,
                                                   Notification notification);
}
