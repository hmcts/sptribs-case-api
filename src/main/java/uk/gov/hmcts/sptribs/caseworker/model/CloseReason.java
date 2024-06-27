package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.sptribs.ciccase.model.State;

@Getter
@AllArgsConstructor
public enum CloseReason implements HasLabel {

    @JsonProperty("caseWithdrawn")
    Withdrawn(State.Withdrawn,"Case Withdrawn"),

    @JsonProperty("caseRejected")
    Rejected(State.Rejected,"Case Rejected"),

    @JsonProperty("caseStrikeOut")
    StrikeOut(State.StrikeOut,"Case Strike Out"),

    @JsonProperty("caseConcession")
    Concession(State.Concession,"Case Concession"),

    @JsonProperty("consentOrder")
    ConsentOrder(State.ConsentOrder, "Consent Order"),

    @JsonProperty("rule27")
    Rule27(State.Rule27, "Rule 27"),

    @JsonProperty("deathOfAppellant")
    DeathOfAppellant(State.DeathOfAppellant, "Death of Appellant");

    private final State type;
    private final String label;

}
