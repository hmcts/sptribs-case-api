package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ContactPartiesCIC implements HasLabel {

    @JsonProperty("SubjectToContact")
    SUBJECTTOCONTACT("Subject"),

    @JsonProperty("RespondentToContact")
    RESPONDENTTOCONTACT("Respondent"),

    @JsonProperty("RepresentativeToContact")
    REPRESENTATIVETOCONTACT("Representative");



    private final String label;

    public boolean isSubject() {
        return SUBJECTTOCONTACT.name().equalsIgnoreCase(this.name());
    }

    public boolean isRepresentativeCIC() {
        return REPRESENTATIVETOCONTACT.name().equalsIgnoreCase(this.name());
    }

    public boolean isApplicantToContact() {
        return RESPONDENTTOCONTACT.name().equalsIgnoreCase(this.name());
    }


}
