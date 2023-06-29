package uk.gov.hmcts.sptribs.ciccase.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum PartiesCICWithOutSubject implements HasLabel {

    @JsonProperty("RepresentativeCIC")
    REPRESENTATIVE("Representative"),

    @JsonProperty("ApplicantCIC")
    APPLICANT("Applicant");

    private final String label;

}
