package uk.gov.hmcts.sptribs.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.model.CaseResponse;
import uk.gov.hmcts.sptribs.services.CaseManagementService;
import uk.gov.hmcts.sptribs.services.model.Event;

@RestController
@RequestMapping("/case/dss-orchestration")
public class CaseManagementController {

    @Autowired
    CaseManagementService caseManagementService;

    @PostMapping("/create")
    @ApiOperation("Call CCD to create case")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "created"),
        @ApiResponse(code = 401, message = "Provided Authorization token is missing or invalid"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<?> createCase(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                        @RequestBody final DssCaseData dssCaseData) {
        CaseData caseData = new CaseData();
        caseData.setDssCaseData(dssCaseData);
        CaseResponse createdCase = caseManagementService.createCase(authorisation, caseData);
        return ResponseEntity.ok(createdCase);
    }

    @PutMapping("/{caseId}/update")
    @ApiOperation("Call CCD to update case")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "updated"),
        @ApiResponse(code = 401, message = "Provided Authorization token is missing or invalid"),
        @ApiResponse(code = 500, message = "Internal Server Error"),
        @ApiResponse(code = 404, message = "Case Not found")
    })
    public ResponseEntity<?> updateCase(@PathVariable final Long caseId,
                                        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                        @RequestParam final Event event,
                                        @RequestBody final DssCaseData dssCaseData) {
        CaseData caseData = new CaseData();
        caseData.setDssCaseData(dssCaseData);
        CaseResponse updatedCase = caseManagementService.updateCase(authorisation, event, caseData, caseId);
        return ResponseEntity.ok(updatedCase);
    }

    @GetMapping("/fetchCaseDetails/{caseId}")
    @ApiOperation("Call CCD to fetch the citizen case details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "updated"),
        @ApiResponse(code = 401, message = "Provided Authorization token is missing or invalid"),
        @ApiResponse(code = 500, message = "Internal Server Error"),
        @ApiResponse(code = 404, message = "Case Not found")
    })
    public ResponseEntity<?> fetchCaseDetails(@PathVariable final Long caseId,
                                              @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        CaseResponse caseResponse = caseManagementService.fetchCaseDetails(authorization,caseId);
        return ResponseEntity.ok(caseResponse);
    }
}
