package uk.gov.hmcts.sptribs.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseService;
import uk.gov.hmcts.sptribs.ciccase.util.CasePartyUtil;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.controllers.mapper.CaseworkerCICDocumentMapper;
import uk.gov.hmcts.sptribs.controllers.mapper.CicaCaseMapper;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseResponse;
import uk.gov.hmcts.sptribs.controllers.model.DashboardDocument;
import uk.gov.hmcts.sptribs.controllers.model.DashboardResponse;
import uk.gov.hmcts.sptribs.controllers.model.DocumentResponse;
import uk.gov.hmcts.sptribs.document.DocumentDownloadService;
import uk.gov.hmcts.sptribs.document.model.ContactPartyDocumentDetails;
import uk.gov.hmcts.sptribs.document.model.DocumentDashboardModel;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DownloadedDocumentResponse;
import uk.gov.hmcts.sptribs.document.service.DocumentDownloadStatusService;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.util.List;
import java.util.Map;
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
    private final DocumentDownloadStatusService documentDownloadStatusService;
    private final CaseworkerCICDocumentMapper caseworkerCICDocumentMapper;
    private final CicaCaseService cicaCaseService;
    private final CicaCaseMapper cicaCaseMapper;
    private final IdamService idamService;
    private final ObjectMapper objectMapper;

    @GetMapping(value = "/{ccdReference}/documents")
    @Operation(summary = "Get Documents for CIC case from a CCD reference number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Documents retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid CCD reference"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Postcode or email mismatch"),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DashboardResponse> getDocumentsByCCDReference(
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

        CicaCaseEntity cicaCaseEntity = cicaCaseService.checkIfUserHasAccessWithPostcode(ccdReference, authorisation, postcode);
        validateCaseEntity(cicaCaseEntity, ccdReference);

        CicaCaseResponse response = cicaCaseMapper.toResponse(cicaCaseEntity);

        Party party = resolveParty(authorisation, cicaCaseEntity);

        Set<Long> downloadedDocIds = documentDownloadStatusService.getDownloadedDocumentIds(ccdReference, party);

        DocumentDashboardModel documentDashboardModel = documentsService.getDocumentsOnCase(Long.valueOf(ccdReference));

        DocumentResponse documentResponse = DocumentResponse.builder()
            .contactPartiesDocuments(wrapContactPartyWithDownloadStatus(
                documentDashboardModel.getContactPartiesDocuments(),
                downloadedDocIds))
            .orderAndDecisionDocuments(wrapWithDownloadStatus(
                documentDashboardModel.getOrderAndDecisionDocuments(),
                downloadedDocIds))
            .latestCaseBundleDocuments(wrapWithDownloadStatus(
                documentDashboardModel.getLatestCaseBundleDocument() != null
                    ? List.of(documentDashboardModel.getLatestCaseBundleDocument()) : List.of(),
                downloadedDocIds))
            .build();

        DashboardResponse dashboardResponse = DashboardResponse.builder()
            .cicaCaseResponse(response)
            .documentResponse(documentResponse)
            .build();

        return ResponseEntity.ok()
            .body(dashboardResponse);
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

        CicaCaseEntity cicaCaseEntity = cicaCaseService.checkIfUserHasAccessWithPostcode(ccdReference, authorisation, postcode);
        validateCaseEntity(cicaCaseEntity, ccdReference);

        Party party = resolveParty(authorisation, cicaCaseEntity);

        DownloadedDocumentResponse documentResponse = documentDownloadService.downloadDocument(
            authorisation,
            documentId
        );

        try {
            documentDownloadStatusService.recordDocumentDownload(ccdReference, party, documentId);
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
        List<DocumentEntity> entities,
        Set<Long> downloadedDocIds) {

        if (entities == null) {
            return List.of();
        }

        return entities.stream()
            .map(entity -> DashboardDocument.builder()
                .document(caseworkerCICDocumentMapper.mapDocument(entity))
                .downloaded(downloadedDocIds != null && downloadedDocIds.contains(entity.getId()))
                .build())
            .toList();
    }

    private Party resolveParty(String authorisation, CicaCaseEntity cicaCaseEntity) {
        CICUser user = idamService.retrieveUser(authorisation);
        if (user == null || user.getUserInfo() == null || user.getUserInfo().getSub() == null) {
            throw new UnauthorisedCaseAccessException("Unable to determine user identity from authorisation token");
        }

        CaseData caseData = objectMapper.convertValue(cicaCaseEntity.getData(), CaseData.class);
        Party party = CasePartyUtil.determineParty(caseData, user.getUserInfo().getSub());
        if (party == null) {
            throw new UnauthorisedCaseAccessException("User email does not match any registered party on case");
        }

        return party;
    }

    private void validateCaseEntity(CicaCaseEntity cicaCaseEntity, String ccdReference) {
        if (cicaCaseEntity == null) {
            throw new UnauthorisedCaseAccessException("No case found with CCD reference: " + ccdReference);
        }

        Map<String, ?> caseData = cicaCaseEntity.getData();
        if (caseData == null) {
            throw new UnauthorisedCaseAccessException("Case data is missing for CCD reference: " + ccdReference);
        }
    }

    private List<DashboardDocument> wrapContactPartyWithDownloadStatus(
        List<ContactPartyDocumentDetails> details,
        Set<Long> downloadedDocIds) {

        if (details == null) {
            return List.of();
        }

        return details.stream()
            .map(detail -> DashboardDocument.builder()
                .document(caseworkerCICDocumentMapper.mapContactPartyDocument(detail))
                .downloaded(downloadedDocIds != null && downloadedDocIds.contains(detail.document().getId()))
                .build())
            .toList();
    }
}
