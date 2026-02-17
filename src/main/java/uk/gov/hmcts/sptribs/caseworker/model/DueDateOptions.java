package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@RequiredArgsConstructor
@Getter
public enum DueDateOptions implements HasLabel {

    @JsonProperty("14 days")
    DAY_COUNT_14(14L, "14 days"),

    @JsonProperty("21 days")
    DAY_COUNT_21(21L, "21 days"),

    @JsonProperty("28 days")
    DAY_COUNT_28(28L, "28 days"),

    @JsonProperty("120 days")
    DAY_COUNT_120(120L, "120 days"),

    @JsonProperty("Other")
    OTHER(null, "Other");

    private final Long amount;
    private final String label;

}
