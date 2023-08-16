package uk.gov.hmcts.sptribs.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.sptribs.services.DocumentManagementService;

@RestController
@RequestMapping("/doc/dss-orchestration")
public class DocumentManagementController {

    @Autowired
    DocumentManagementService documentManagementService;

    @RequestMapping(
        value = "/upload",
        method = RequestMethod.POST,
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation("Call CDAM to upload document")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Uploaded Successfully"),
        @ApiResponse(code = 400, message = "Bad Request while uploading the document"),
        @ApiResponse(code = 401, message = "Provided Authorization token is missing or invalid"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<?> uploadDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                        @RequestParam("caseTypeOfApplication") String caseTypeOfApplication,
                                        @RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(documentManagementService.uploadDocument(authorisation, caseTypeOfApplication, file));
    }

    @DeleteMapping("/{documentId}/delete")
    @ApiOperation("Call CDAM to delete document")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Deleted document successfully"),
        @ApiResponse(code = 400, message = "Bad Request while deleting the document"),
        @ApiResponse(code = 401, message = "Provided Authorization token is missing or invalid"),
        @ApiResponse(code = 404, message = "Document Not found"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<?> deleteDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                        @PathVariable("documentId") String documentId) {

        return ResponseEntity.ok(documentManagementService.deleteDocument(authorisation, documentId));
    }
}
