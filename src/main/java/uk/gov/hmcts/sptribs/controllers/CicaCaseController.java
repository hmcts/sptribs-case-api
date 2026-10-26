package uk.gov.hmcts.sptribs.controllers;

import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseService;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseUpdateService;
import uk.gov.hmcts.sptribs.ciccase.service.CicaRoleService;
import uk.gov.hmcts.sptribs.controllers.model.DssCaseUpdateRequest;
import uk.gov.hmcts.sptribs.controllers.model.UpdateCaseResponse;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@Tag(name = "CICA Case Controller")
@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/cases/cica")
public class CicaCaseController {

    private final CicaCaseService cicaCaseService;
    private final CicaRoleService cicaRoleService;
    private final CicaCaseUpdateService cicaCaseUpdateService;

    @GetMapping(value = "/{ccdReference}/access", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Check if user has access to the case",
        description = "Verifies if the user has access to the given CCD reference, throwing an exception if unauthorized."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Access verification completed successfully",
            content = @Content),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid CCD reference format",
            content = @Content),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing authorization token",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Service not authorized",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "No case found with the given CCD reference",
            content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
    })
    public ResponseEntity<Void> checkIfUserHasAccess(
        @RequestHeader(AUTHORIZATION)
        @Parameter(description = "User's IDAM access token", required = true)
        String authorisation,

        @RequestHeader(SERVICE_AUTHORIZATION)
        @Parameter(description = "S2S token from the frontend service", required = true)
        String serviceAuthorisation,

        @PathVariable
        @NotBlank(message = "CCD reference cannot be blank")
        @Pattern(regexp = "^\\d{16}$", message = "CCD reference must be 16 digits long")
        @Parameter(
            description = "The CCD reference number.",
            required = true,
            example = "1740138704453399"
        )
        String ccdReference
    ) {
        log.info("Received request to check if user has access to case: {}", ccdReference);
        cicaCaseService.checkIfUserHasAccess(ccdReference, authorisation);
        log.info("Access check completed successfully for case: {}", ccdReference);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{ccdReference}/role-assignments")
    @Operation(
        summary = "Assign the creator role to the authenticated citizen",
        description = "Verifies that the authenticated citizen belongs to the case before assigning their own creator role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Creator role assigned", content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid CCD reference", content = @Content),
        @ApiResponse(responseCode = "403", description = "User or service cannot access the case", content = @Content),
        @ApiResponse(responseCode = "404", description = "Case not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Role assignment failed", content = @Content)
    })
    public ResponseEntity<Void> assignCreatorRole(
        @RequestHeader(AUTHORIZATION)
        @Parameter(description = "User's IDAM access token", required = true)
        String authorisation,

        @RequestHeader(SERVICE_AUTHORIZATION)
        @Parameter(description = "S2S token from the frontend service", required = true)
        String serviceAuthorisation,

        @PathVariable
        @Pattern(regexp = "^\\d{16}$", message = "CCD reference must be 16 digits long")
        @Parameter(description = "The CCD reference number", required = true, example = "1740138704453399")
        String ccdReference
    ) {
        cicaCaseService.checkIfUserHasAccess(ccdReference, authorisation);
        cicaRoleService.assignCreatorRole(ccdReference, authorisation);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{ccdReference}/updates", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Update a CICA case using the citizen DSS update event",
        description = "Verifies case membership and attempts the CCD update with the caller's token. "
            + "If CCD reports that the case is inaccessible due to a missing case role, assigns the creator role "
            + "and retries the event start once. The update is submitted only once."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Case updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid case reference or request body", content = @Content),
        @ApiResponse(responseCode = "401", description = "CCD denied the update", content = @Content),
        @ApiResponse(responseCode = "403", description = "User or service cannot access the case", content = @Content),
        @ApiResponse(responseCode = "404", description = "Case or CCD event not found", content = @Content),
        @ApiResponse(responseCode = "409", description = "CCD update conflict", content = @Content),
        @ApiResponse(responseCode = "502", description = "CCD could not be reached", content = @Content)
    })
    public ResponseEntity<UpdateCaseResponse> updateCase(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable
        @Pattern(regexp = "^\\d{16}$", message = "CCD reference must be 16 digits long") String ccdReference,
        @Valid @RequestBody DssCaseUpdateRequest update
    ) {
        try {
            var updatedCase = cicaCaseUpdateService.updateCase(ccdReference, authorisation, update.data());
            ResponseEntity<UpdateCaseResponse> response = ResponseEntity.status(CREATED)
                .body(new UpdateCaseResponse(updatedCase.getReference(), updatedCase.getState()));
            log.info("Case update response prepared: status={}", CREATED.value());
            return response;
        } catch (FeignException exception) {
            int status = exception.status();
            int responseStatus = status >= 400 && status < 600 ? status : BAD_GATEWAY.value();
            Throwable cause = exception.getCause();
            log.warn("Case update failed: exception_type={} cause_type={} downstream_status={} response_status={}",
                exception.getClass().getName(), cause == null ? "none" : cause.getClass().getName(),
                status, responseStatus);
            // CCD error bodies may contain case data; return the failure status without exposing them.
            return ResponseEntity.status(responseStatus).build();
        }
    }
}
