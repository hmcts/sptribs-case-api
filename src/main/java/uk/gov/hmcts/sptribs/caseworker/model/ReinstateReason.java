
package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ReinstateReason implements HasLabel {
    @JsonProperty("requestFollowingAWithdrawalDecision")
    REQUEST_FOLLOWING_A_WITHDRAWAL_DECISION("Request following a withdrawal decision", "Request following a withdrawal decision"),

    @JsonProperty("RequestFollowingAStrikeOutDecision")
    REQUEST_FOLLOWING_A_STRIKE_OUT_DECISION("Request following a strike out decision", "Request following a strike out decision"),

    @JsonProperty("caseHadBeenClosedInError")
    CASE_HAD_BEEN_CLOSED_IN_ERROR("Case had been closed in error", "Case had been closed in error"),

    @JsonProperty("requestFollowingADecisionFromTheUpperTribunal")
    REQUEST_FOLLOWING_A_DECISION_FROM_THE_UPPER_TRIBUNAL("Request following a decision from the Upper Tribunal",
        "Request following a decision from the Upper Tribunal"),

    @JsonProperty("requestFollowingAnOralHearingApplicationFollowingARule27Decision")
    REQUEST_FOLLOWING_AN_ORAL_HEARING_APPLICATION_FOLLOWING_A_RULE_27_DECISION(
        "Request following an oral hearing application following a Rule 27 decision",
        "Request following an oral hearing application following a Rule 27 decision"),

    @JsonProperty("Request to set aside a tribunal decision following an oral hearing")
    REQUEST_TO_SET_ASIDE_A_TRIBUNAL_DECISION_FOLLOWING_AN_ORAL_HEARING("Request to set aside a tribunal decision following an oral hearing",
        "Request to set aside a tribunal decision following an oral hearing"),

    @JsonProperty("Other")
    OTHER("Other", "Other");

    private String type;
    private final String label;

    public boolean isOther() {
        return OTHER.name().equalsIgnoreCase(this.name());
    }

}
