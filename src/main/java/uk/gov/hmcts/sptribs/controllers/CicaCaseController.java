package uk.gov.hmcts.sptribs.controllers;

import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseService;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
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
        summary = "Ensure the authenticated case member has the creator role",
        description = "Verifies the caller belongs to the case before checking their CCD roles. "
            + "Assigns [CREATOR] only if absent. The caller cannot choose another user or role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Creator role assigned or already present", content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid reference or missing required header", content = @Content),
        @ApiResponse(responseCode = "401", description = "Invalid authentication token", content = @Content),
        @ApiResponse(responseCode = "403", description = "User or service cannot access the case", content = @Content),
        @ApiResponse(responseCode = "404", description = "Case not found", content = @Content),
        @ApiResponse(responseCode = "409", description = "CCD role assignment conflict", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content),
        @ApiResponse(responseCode = "502", description = "CCD could not be reached", content = @Content),
        @ApiResponse(responseCode = "503", description = "Downstream service unavailable", content = @Content)
    })
    public ResponseEntity<Void> ensureCreatorRole(
        @RequestHeader(AUTHORIZATION)
        @NotBlank
        @Parameter(description = "User's IDAM access token", required = true)
        String authorisation,

        @RequestHeader(SERVICE_AUTHORIZATION)
        @NotBlank
        @Parameter(description = "S2S token from the frontend service", required = true)
        String serviceAuthorisation,

        @PathVariable
        @NotBlank(message = "CCD reference cannot be blank")
        @Pattern(regexp = "^\\d{16}$", message = "CCD reference must be 16 digits long")
        @Parameter(description = "The 16-digit CCD reference number", required = true)
        String ccdReference
    ) {
        try {
            cicaCaseService.ensureCreatorRole(ccdReference, authorisation);
            return ResponseEntity.noContent().build();
        } catch (FeignException exception) {
            int status = exception.status();
            int responseStatus = status >= 400 && status < 600 ? status : BAD_GATEWAY.value();
            log.warn("Creator role assignment failed: downstream_status={} response_status={}", status, responseStatus);
            // Downstream error messages and bodies may contain case data or credentials.
            return ResponseEntity.status(responseStatus).build();
        }
    }
}
