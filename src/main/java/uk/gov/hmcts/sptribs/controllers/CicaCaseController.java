package uk.gov.hmcts.sptribs.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseService;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseResponse;

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

    @GetMapping(value = "/{cicaReference}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get case by CICA reference",
        description = "Retrieves a case by its CICA (Criminal Injuries Compensation Authority) reference number. "
            + "Reference numbers must start with X or G followed by digits (e.g., X12345, G98765)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Case found successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CicaCaseResponse.class)
            )
            ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid CICA reference format",
            content = @Content
            ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing authorization token",
            content = @Content
            ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Service not authorized",
            content = @Content
            ),
        @ApiResponse(
            responseCode = "404",
            description = "No case found with the given CICA reference",
            content = @Content
            ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
            )
    })
    public ResponseEntity<CicaCaseResponse> getCaseByCicaReference(
        @RequestHeader(AUTHORIZATION)
        @Parameter(description = "User's IDAM access token", required = true)
        String authorisation,

        @RequestHeader(SERVICE_AUTHORIZATION)
        @Parameter(description = "S2S token from the frontend service", required = true)
        String serviceAuthorisation,

        @PathVariable
        @NotBlank(message = "CICA reference cannot be blank")
        @Pattern(regexp = "^[XGxg]\\d+$", message = "CICA reference must start with X or G followed by digits")
        @Parameter(
            description = "The CICA reference number (e.g., X12345 or G98765). "
                + "Reference numbers always begin with the letter X or G.",
            required = true,
            example = "X12345"
        )
        String cicaReference
    ) {
        log.info("Received request to get case by CICA reference: {}", cicaReference);

        CicaCaseResponse response = cicaCaseService.getCaseByCicaReference(cicaReference);

        log.info("Successfully retrieved case with CICA reference: {}", cicaReference);
        return ResponseEntity.ok(response);
    }
}




