package uk.gov.hmcts.sptribs.document.bundling.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.sptribs.document.bundling.model.Callback;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@FeignClient(name = "bundling-client", url = "${bundling.api.baseUrl}")
public interface BundlingClient {
    @PostMapping(value = "/api/new-bundle",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    BundleResponse createBundle(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestHeader(AUTHORIZATION) final String authorisation,
        @RequestBody Callback callback
    );

}
