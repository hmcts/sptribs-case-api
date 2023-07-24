package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum CloseReason implements HasLabel {

    @JsonProperty("caseWithdrawn")
    Withdrawn("caseWithdrawn","Case Withdrawn"),

    @JsonProperty("caseRejected")
    Rejected("Rejected","Case Rejected"),

    @JsonProperty("caseStrikeOut")
    StrikeOut("StrikeOut","Case Strike Out"),

    @JsonProperty("caseConcession")
    Concession("Concession","Case Concession"),

    @JsonProperty("consentOrder")
    ConsentOrder("ConsentOrder", "Consent Order"),

    @JsonProperty("rule27")
    Rule27("Rule27", "Rule 27");

    private final String name;
    private final String label;

}
