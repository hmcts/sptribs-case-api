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
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseService;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseResponse;
import uk.gov.hmcts.sptribs.idam.IdamService;

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
    private final IdamService idamService;

    @GetMapping(value = "/{ccdReference}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get case by CCD reference",
        description = "Retrieves a case by its CCD (Criminal case data) reference number. "
            + "Reference numbers must be 16 digits long."
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
            description = "Invalid CCD reference format",
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
            description = "No case found with the given CCD reference",
            content = @Content
            ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
            )
    })
    public ResponseEntity<CicaCaseResponse> getCaseByCCDReference(
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
            description = "The CCD reference number. ",
            required = true,
            example = "1740-1387-0445-3399"
        )
        String ccdReference
    ) {
        log.info("Received request to get case by CCD reference: {}", ccdReference);


        User user = idamService.retrieveUser(authorisation);
        //do we want to show a dashboard of nothing or actually give them a message
        // saying they cant see because email is not in case, etc
        System.out.println(user.getUserDetails().getEmail());

        //check db with cica number and check the case data if the email exists in there.

        //if true return required docs, if false return auth error.

        CicaCaseResponse response = cicaCaseService.getCaseByCCDReference(ccdReference);

        log.info("Successfully retrieved case with CCD reference: {}", ccdReference);
        return ResponseEntity.ok(response);
    }
}




