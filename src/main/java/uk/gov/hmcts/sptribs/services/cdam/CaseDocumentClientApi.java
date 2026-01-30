package uk.gov.hmcts.sptribs.services.cdam;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@FeignClient(name = "sptribs-case-document-am-client-api", url = "${case_document_am.url}",
    configuration = FeignSupportConfig.class)
public interface CaseDocumentClientApi {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String DOCUMENT_ID = "documentId";

    @PostMapping(value = "/cases/documents", produces = APPLICATION_JSON_VALUE,  consumes = MULTIPART_FORM_DATA_VALUE)
    UploadResponse uploadDocuments(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                   @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuth,
                                   @RequestBody DocumentUploadRequest uploadRequest);

    @DeleteMapping(value = "/cases/documents/{documentId}")
    void deleteDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                  @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuth,
                                  @PathVariable(DOCUMENT_ID) UUID documentId,
                                  @RequestParam("permanent") boolean permanent);

    @GetMapping(value = "/cases/documents/{documentId}")
    ResponseEntity<Document> getDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuth,
                                            @PathVariable(DOCUMENT_ID) UUID documentId);

    @GetMapping(value = "/cases/documents/{documentId}/binary")
    ResponseEntity<byte[]> getDocumentBinary(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuth,
                                            @PathVariable(DOCUMENT_ID) UUID documentId);
}
