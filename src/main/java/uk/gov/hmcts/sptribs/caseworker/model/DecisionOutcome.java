package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@RequiredArgsConstructor
public enum DecisionOutcome implements HasLabel {

    @JsonProperty("Other - appeal continues")
    OTHER("Other - appeal continues"),

    @JsonProperty("Rule 27")
    RULE_27("Rule 27"),

    @JsonProperty("Withdrawn")
    WITHDRAWN("Withdrawn"),

    @JsonProperty("Strike Out")
    STRIKE_OUT("Strike Out");

    private final String label;
}
