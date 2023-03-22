package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum CloseCaseRejectionReason implements HasLabel {

    @JsonProperty("createdInError")
    CREATED_IN_ERROR("Case created in error"),

    @JsonProperty("deadlineMissed")
    DEADLINE_MISSED("Deadline missed without sufficient explanation"),

    @JsonProperty("duplicateCase")
    DUPLICATE_CASE("Duplicate case"),

    @JsonProperty("vexatiousLitigant")
    VEXATIOUS_LITIGANT("Vexatious litigant"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;

}
