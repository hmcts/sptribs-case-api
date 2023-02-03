package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum RespondentCIC implements HasLabel {
    @JsonProperty("RespondentCIC")
    RESPONDENT("Respondent");
    private final String label;
}
