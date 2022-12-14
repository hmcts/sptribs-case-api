package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum CloseReason implements HasLabel {

    @JsonProperty("caseWithdrawn")
    Withdrawn("Case Withdrawn","Case Withdrawn"),

    @JsonProperty("caseRejected")
    Rejection("Case Rejected","Case Rejected"),

    @JsonProperty("caseStrikeOut")
    StrikeOut("Case Strike Out","Case Strike Out"),

    @JsonProperty("caseConcession")
    Concession("Case Concession","Case Concession"),

    @JsonProperty("caseDischarge")
    Discharge("Case Discharge", "Case Discharge");

    private final String type;
    private final String label;

}
