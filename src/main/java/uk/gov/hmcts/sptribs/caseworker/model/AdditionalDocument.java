package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@RequiredArgsConstructor
@Getter
public enum AdditionalDocument implements HasLabel {

    @JsonProperty("Tribunal form")
    TRIBUNAL_FORM("Tribunal form", "Tribunal form"),

    @JsonProperty("Applicant evidence")
    APPLICANT_EVIDENCE("Applicant evidence", "Applicant evidence");

    private final String reason;
    private final String label;

    public String getReason() {
        return this.reason;
    }

    public String getLabel() {
        return this.label;
    }
}
