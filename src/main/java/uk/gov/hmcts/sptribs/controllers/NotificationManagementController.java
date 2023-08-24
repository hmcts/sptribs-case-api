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
import uk.gov.hmcts.sptribs.edgecase.event.Event;
import uk.gov.hmcts.sptribs.model.CaseResponse;
import uk.gov.hmcts.sptribs.notification.model.NotificationResponse;
import uk.gov.hmcts.sptribs.services.CaseManagementService;

@RestController
@RequestMapping("/notification")
public class NotificationManagementController {

    @Autowired
    CaseManagementService caseManagementService;

    @PutMapping("case/{caseId}/update")
    @ApiOperation("Call CCD to update case")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "updated"),
        @ApiResponse(code = 401, message = "Provided Authorization token is missing or invalid"),
        @ApiResponse(code = 500, message = "Internal Server Error"),
        @ApiResponse(code = 404, message = "Case Not found")
    })
    public ResponseEntity<?> updateNotificationDetails(@PathVariable final Long caseId,
                                        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                        @RequestBody final NotificationResponse notificationResponse) {

        CaseResponse updatedCase = caseManagementService.updateNotificationDetails(authorisation, caseId, notificationResponse);
        return ResponseEntity.ok(updatedCase);
    }
}
