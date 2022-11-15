package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@RequiredArgsConstructor
@Getter
public enum HearingCancellationReason implements HasLabel {

    @JsonProperty("RequestForR27DecisionAndNoTimeForInfill")
    REQUEST_FOR_R27_DECISION_AND_NO_TIME_FOR_INFILL("Request for R27 decision and no time for infill",
        "Request for R27 decision and no time for infill"),

    @JsonProperty("noSuitableCasesThatAreReadyToList")
    NO_SUITABLE_CASES_THAT_ARE_READY_TO_LIST("No suitable cases that are ready to list",
        "No suitable cases that are ready to list"),

    @JsonProperty("consentOrderReceivedAndNoTimeForInfill")
    CONSENT_ORDER_RECEIVED_AND_NO_TIME_FOR_INFILL("Consent Order received and no time for infill",
        "Consent Order received and no time for infill"),

    @JsonProperty("incompletePanel")
    INCOMPLETE_PANEL("Incomplete Panel", "Incomplete Panel"),

    @JsonProperty("caseRejected")
    CASE_REJECTED("Case Rejected", "Case Rejected"),

    @JsonProperty("venueUnavailable")
    VENUE_UNAVAILABLE("Venue Unavailable", "Venue Unavailable"),

    @JsonProperty("Other")
    OTHER("Other", "Other");

    private final String reason;
    private final String label;

    public String getReason() {
        return this.reason;
    }

    public String getLabel() {
        return this.label;
    }

}
