package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum TribunalCIC implements HasLabel {
    @JsonProperty("TribunalCIC")
    TRIBUNAL("Tribunal");
    private final String label;
}
