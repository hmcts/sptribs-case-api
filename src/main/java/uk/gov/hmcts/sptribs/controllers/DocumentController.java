package uk.gov.hmcts.sptribs.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.sptribs.document.DocumentDownloadService;
import uk.gov.hmcts.sptribs.document.model.DownloadedDocumentResponse;

@Tag(name = "Document Controller")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/case/document")
public class DocumentController {

    private final DocumentDownloadService documentDownloadService;

    @GetMapping(value = "/downloadDocument/{documentId}")
    @Operation(summary = "Download a document by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document downloaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid document ID"),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Resource> downloadDocumentById(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "Authorization token", required = true)
        String authorisation,
        @PathVariable
        @NotNull
        @Parameter(description = "The document ID (UUID)", required = true)
        String documentId) {

        log.info("Received request to download document with id: {}", documentId);

        DownloadedDocumentResponse documentResponse = documentDownloadService.downloadDocument(
            authorisation,
            documentId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(documentResponse.mimeType()));
        headers.set("original-file-name", documentResponse.fileName());
        log.info("returning document now with name {}", documentResponse.fileName());

        return ResponseEntity.ok()
            .headers(headers)
            .body(documentResponse.file());
    }
}
