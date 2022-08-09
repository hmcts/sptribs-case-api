package uk.gov.hmcts.sptribs.document;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.sptribs.document.model.DocAssemblyRequest;
import uk.gov.hmcts.sptribs.document.model.DocAssemblyResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@FeignClient(name = "doc-assembly-api", primary = false, url = "${doc_assembly.url}")
public interface DocAssemblyClient {
    @PostMapping(
        value = "/api/template-renditions",
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    DocAssemblyResponse generateAndStoreDraftApplication(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        DocAssemblyRequest request
    );
}
