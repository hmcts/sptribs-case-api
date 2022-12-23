package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum HearingType implements HasLabel {

    @JsonProperty("CaseManagement")
    CASE_MANAGEMENT("Case management"),

    @JsonProperty("Final")
    FINAL("Final"),

    @JsonProperty("Interlocutory")
    INTERLOCUTORY("Interlocutory");

    private final String label;
}
