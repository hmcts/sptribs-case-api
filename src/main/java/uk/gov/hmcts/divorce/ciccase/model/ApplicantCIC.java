package uk.gov.hmcts.divorce.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ApplicantCIC implements HasLabel {
    @JsonProperty("ApplicantCIC")
    APPLICANT_CIC("Applicant (If different from subject).");

    private final String label;

    public boolean isApplicantCIC() {
        return APPLICANT_CIC.name().equalsIgnoreCase(this.name());
    }
}
