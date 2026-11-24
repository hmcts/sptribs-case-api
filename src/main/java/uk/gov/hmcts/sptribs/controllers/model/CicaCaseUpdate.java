package uk.gov.hmcts.sptribs.controllers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Only the new information may be submitted; callers cannot overwrite other case fields. */
public record CicaCaseUpdate(
    @JsonProperty("additional_information") String additionalInformation,
    @NotNull List<@NotNull @Valid UploadedDocument> documents
) {
    public record UploadedDocument(
        @JsonProperty("document_url") @NotBlank String documentUrl,
        @JsonProperty("document_binary_url") @NotBlank String documentBinaryUrl,
        @JsonProperty("document_filename") @NotBlank String documentFilename,
        String comment
    ) {
    }
}
