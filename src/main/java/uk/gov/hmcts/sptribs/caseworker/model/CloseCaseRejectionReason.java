package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum CloseCaseRejectionReason implements HasLabel {

    @JsonProperty("noJurisdiction")
    NO_JURISDICTION("No jurisdiction"),

    @JsonProperty("deadlineMissed")
    DEADLINE_MISSED("Deadline missed without sufficient explanation"),

    @JsonProperty("vexatiousLitigant")
    VEXATIOUS_LITIGANT("Vexatious litigant"),

    @JsonProperty("duplicateCase")
    DUPLICATE_CASE("Duplicate case"),

    @JsonProperty("createdInError")
    CREATED_IN_ERROR("Case created in error"),

    @JsonProperty("invalidApplication")
    INVALID_APPLICATION("Invalid application"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;

}
