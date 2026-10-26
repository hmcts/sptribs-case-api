package uk.gov.hmcts.sptribs.controllers.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// The frontend sends a CCD event envelope. The server starts its own event and uses only the case data.
@JsonIgnoreProperties(ignoreUnknown = true)
public record DssCaseUpdateRequest(@Valid @NotNull DssCaseDataRequest data) {
}
