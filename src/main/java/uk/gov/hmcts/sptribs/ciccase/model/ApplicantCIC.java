package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ApplicantCIC implements HasLabel {

    @JsonProperty("ApplicantCIC")
    APPLICANT_CIC("Applicant (if different from subject)");

    private final String label;

}
