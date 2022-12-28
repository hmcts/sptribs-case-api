package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum RespondantCIC implements HasLabel {
    @JsonProperty("RespondantCIC")
    RESPONDANT("Respondant");
    private final String label;
    public boolean isRespondantCIC() {
        return RESPONDANT.name().equalsIgnoreCase(this.name());
    }
}
