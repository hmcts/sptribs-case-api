package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@RequiredArgsConstructor
@Getter
public enum YesNo implements HasLabel {

    @JsonProperty("Yes")
    YES("Yes", "Yes"),

    @JsonProperty("No")
    NO("No", "No");

    private final String type;
    private final String label;

    public String getLabel() {
        return this.label;
    }

}
