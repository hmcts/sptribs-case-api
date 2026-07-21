package uk.gov.hmcts.sptribs.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseService;
import uk.gov.hmcts.sptribs.controllers.mapper.CaseworkerCICDocumentMapper;
import uk.gov.hmcts.sptribs.controllers.model.DashboardDocument;
import uk.gov.hmcts.sptribs.controllers.model.DocumentResponse;
import uk.gov.hmcts.sptribs.document.DocumentDownloadService;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentDashboardModel;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DownloadedDocumentResponse;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Tag(name = "Document Controller")
@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/cases/CIC")
public class DocumentController {

    private final DocumentDownloadService documentDownloadService;
    private final DocumentsService documentsService;
    private final CaseworkerCICDocumentMapper caseworkerCICDocumentMapper;
    private final CicaCaseService cicaCaseService;

    @GetMapping(value = "/{ccdReference}/documents")
    @Operation(summary = "Get Documents for CIC case from a CCD reference number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Documents retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid CCD reference"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Postcode or email mismatch"),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DocumentResponse> getDocumentsByCCDReference(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "Authorization token", required = true)
        String authorisation,
        @RequestHeader(value = "X-Postcode")
        @Parameter(description = "Postcode for verification", required = true)
        String postcode,
        @PathVariable
        @NotBlank(message = "CCD reference cannot be blank")
        @Pattern(regexp = "^\\d{16}$", message = "CCD reference must be 16 digits long")
        @Parameter(
            description = "The CCD reference number.",
            required = true,
            example = "1740138704453399"
        )
        String ccdReference) {

        log.info("Received request to get documents with CCD reference = {}", ccdReference);

        cicaCaseService.checkIfUserHasAccessWithPostcode(ccdReference, authorisation, postcode);

        Set<Long> downloadedDocIds = documentsService.getDownloadedDocumentIds(authorisation, ccdReference, postcode);

        DocumentDashboardModel documentDashboardModel = documentsService.getDocumentsOnCase(Long.valueOf(ccdReference));

        DocumentResponse documentResponse = DocumentResponse.builder()
            .contactPartiesDocuments(wrapWithDownloadStatus(
                caseworkerCICDocumentMapper.map(documentDashboardModel.getContactPartiesDocuments()),
                documentDashboardModel.getContactPartiesDocuments(),
                downloadedDocIds))
            .orderAndDecisionDocuments(wrapWithDownloadStatus(
                caseworkerCICDocumentMapper.map(documentDashboardModel.getOrderAndDecisionDocuments()),
                documentDashboardModel.getOrderAndDecisionDocuments(),
                downloadedDocIds))
            .latestCaseBundleDocuments(wrapWithDownloadStatus(
                caseworkerCICDocumentMapper.mapEntityToList(documentDashboardModel.getLatestCaseBundleDocument()),
                documentDashboardModel.getLatestCaseBundleDocument() != null
                    ? List.of(documentDashboardModel.getLatestCaseBundleDocument()) : List.of(),
                downloadedDocIds))
            .build();

        return ResponseEntity.ok()
            .body(documentResponse);
    }

    @GetMapping(value = "/{ccdReference}/documents/{documentId}/download")
    @Operation(summary = "Download a document by its ID and verify against case reference and postcode")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document downloaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid CCD reference or document ID"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Postcode mismatch or document does not belong to the case"),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Resource> downloadDocumentByCaseAndId(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "Authorization token", required = true)
        String authorisation,
        @RequestHeader(value = "X-Postcode")
        @Parameter(description = "Postcode for verification", required = true)
        String postcode,
        @PathVariable
        @NotBlank(message = "CCD reference cannot be blank")
        @Pattern(regexp = "^\\d{16}$", message = "CCD reference must be 16 digits long")
        @Parameter(
            description = "The CCD reference number.",
            required = true,
            example = "1740138704453399"
        )
        String ccdReference,
        @PathVariable
        @NotNull
        @Parameter(description = "The document ID (UUID)", required = true)
        String documentId) {

        log.info("Received request to download document with id: {} for CCD reference: {}", documentId, ccdReference);

        cicaCaseService.checkIfUserHasAccessWithPostcode(ccdReference, authorisation, postcode);

        DownloadedDocumentResponse documentResponse = documentDownloadService.downloadDocument(
            authorisation,
            documentId
        );

        try {
            documentsService.recordDocumentDownload(authorisation, ccdReference, postcode, documentId);
        } catch (Exception e) {
            log.error("Failed to record document download status for doc: {}, case: {}", documentId, ccdReference, e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(documentResponse.mimeType()));
        headers.set("original-file-name", documentResponse.fileName());
        log.info("returning document now with name {}", documentResponse.fileName());

        return ResponseEntity.ok()
            .headers(headers)
            .body(documentResponse.file());
    }

    private List<DashboardDocument> wrapWithDownloadStatus(
        List<CaseworkerCICDocument> mappedDocs,
        List<DocumentEntity> entities,
        Set<Long> downloadedDocIds) {

        if (mappedDocs == null) {
            return List.of();
        }

        return mappedDocs.stream()
            .map(doc -> {
                boolean downloaded = false;
                if (doc.getDocumentLink() != null && doc.getDocumentLink().getUrl() != null) {
                    String docUrl = doc.getDocumentLink().getUrl();
                    Optional<DocumentEntity> matchingEntity = entities.stream()
                        .filter(e -> docUrl.equals(e.getDocumentUrl()))
                        .findFirst();
                    if (matchingEntity.isPresent() && downloadedDocIds.contains(matchingEntity.get().getId())) {
                        downloaded = true;
                    }
                }
                return DashboardDocument.builder()
                    .document(doc)
                    .downloaded(downloaded)
                    .build();
            })
            .toList();
    }
}

