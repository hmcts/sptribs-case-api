package uk.gov.hmcts.sptribs.controllers;

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
import org.springframework.http.HttpHeaders;
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
import uk.gov.hmcts.sptribs.ciccase.model.SubjectVerificationRequest;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectVerificationResponse;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseService;

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
            description = "Forbidden - Service not authorised or user not associated with the case",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "Case not found or expired",
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
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").build();
    }

    @PostMapping(value = "/{ccdReference}/subject-verifications", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Verify subject name and date of birth for an associated case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verification result (no case data returned)"),
        @ApiResponse(responseCode = "400", description = "Invalid reference or subject details"),
        @ApiResponse(responseCode = "401", description = "User token invalid or expired"),
        @ApiResponse(responseCode = "403", description = "User or service not authorised"),
        @ApiResponse(responseCode = "500", description = "Verification unavailable")
    })
    public ResponseEntity<SubjectVerificationResponse> verifySubject(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable @Pattern(regexp = "^\\d{16}$", message = "CCD reference must be 16 digits long") String ccdReference,
        @Valid @RequestBody SubjectVerificationRequest request
    ) {
        boolean verified = cicaCaseService.verifySubject(ccdReference, authorisation, request.subjectFullName(),
            request.subjectDateOfBirth().toString());
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store")
            .body(new SubjectVerificationResponse(verified));
    }
}
