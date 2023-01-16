package uk.gov.hmcts.sptribs.document;

import org.apache.http.HttpHeaders;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.sptribs.common.config.ControllerConstants;

import java.util.UUID;

@FeignClient(name = "case-document-am-api", url = "${case_document_am.url}/cases/documents")
public interface CaseDocumentClient {

    @DeleteMapping(value = "/{documentId}")
    void deleteDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                        @RequestHeader(ControllerConstants.SERVICE_AUTHORIZATION) String serviceAuth,
                        @PathVariable(DocumentConstants.DOCUMENT_ID) UUID documentId,
                        @RequestParam("permanent") boolean permanent);

    @GetMapping(value = "/{documentId}/binary")
    ResponseEntity<Resource> getDocumentBinary(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                               @RequestHeader(ControllerConstants.SERVICE_AUTHORIZATION) String serviceAuth,
                                               @PathVariable(DocumentConstants.DOCUMENT_ID) UUID documentId);
}
