package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ReferralReason implements HasLabel {

    @JsonProperty("corrections")
    CORRECTIONS("Corrections"),

    @JsonProperty("listedCase")
    LISTED_CASE("Listed case"),

    @JsonProperty("listedCaseWithin5Days")
    LISTED_CASE_WITHIN_5_DAYS("Listed case (within 5 days)"),

    @JsonProperty("listingDirections")
    LISTING_DIRECTIONS("Listing directions"),

    @JsonProperty("newCase")
    NEW_CASE("New case"),

    @JsonProperty("postponementRequest")
    POSTPONEMENT_REQUEST("Postponement request"),

    @JsonProperty("reinstatementRequest")
    REINSTATEMENT_REQUEST("Reinstatement request"),

    @JsonProperty("rule27Request")
    RULE_27_REQUEST("Rule 27 request"),

    @JsonProperty("setAsideRequest")
    SET_ASIDE_REQUEST("Set aside request"),

    @JsonProperty("stayRequest")
    STAY_REQUEST("Stay request"),

    @JsonProperty("strikeOutRequest")
    STRIKE_OUT_REQUEST("Strike out request"),

    @JsonProperty("timeExtensionRequest")
    TIME_EXTENSION_REQUEST("Time extension request"),

    @JsonProperty("withdrawalRequest")
    WITHDRAWAL_REQUEST("Withdrawal request"),

    @JsonProperty("writtenReasonsRequest")
    WRITTEN_REASONS_REQUEST("Written reasons request"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
