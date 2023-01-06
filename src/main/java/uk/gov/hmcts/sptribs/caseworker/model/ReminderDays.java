package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@RequiredArgsConstructor
@Getter
public enum ReminderDays implements HasLabel {

    @JsonProperty("1 day")
    DAY_COUNT_1("1 day", "1 day"),

    @JsonProperty("3 days")
    DAY_COUNT_3("3 days", "3 days"),

    @JsonProperty("5 days")
    DAY_COUNT_5("5 days", "5 days"),

    @JsonProperty("7 days")
    DAY_COUNT_7("7 days", "7 days");

    private final String type;
    private final String label;

    public String getLabel() {
        return this.label;
    }

}
