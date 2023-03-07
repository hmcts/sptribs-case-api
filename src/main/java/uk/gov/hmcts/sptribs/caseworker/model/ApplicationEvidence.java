package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@RequiredArgsConstructor
@Getter
public enum ApplicationEvidence implements HasLabel {

    @JsonProperty("Police evidence")
    POLICE_EVIDENCE("Police evidence", "Police evidence"),

    @JsonProperty("GP records")
    GP_RECORDS("GP records", "GP records"),

    @JsonProperty("Hospital records")
    HOSPITAL_RECORDS("Hospital records", "Hospital records"),

    @JsonProperty("Mental Health records")
    MENTAL_HEALTH_RECORDS("Mental Health records", "Mental Health records"),

    @JsonProperty("Expert evidence")
    EXPERT_EVIDENCE("Expert evidence", "Expert evidence"),

    @JsonProperty("Other medical records")
    OTHER_MEDIAL_RECORDS("Other medical records", "Other medical records"),

    @JsonProperty("Application for an extension of time")
    APPLICATION_FOR_AN_EXTENSION_OF_TIME("Application for an extension of time", "Application for an extension of time"),

    @JsonProperty("Application for a postponement")
    APPLICATION_FOR_A_POSTPONEMENT("Application for a postponement", "Application for a postponements"),

    @JsonProperty("Submission from appellant")
    SUBMISSION_FROM_APPELLANT("Submission from appellant", "Submission from appellant"),

    @JsonProperty("Submission from respondent")
    SUBMISSION_FROM_RESPONDENT("Submission from respondent", "Submission from respondent"),

    @JsonProperty("Other")
    OTHER("Other", "Other");

    private final String reason;
    private final String label;

    public String getReason() {
        return this.reason;
    }

    public String getLabel() {
        return this.label;
    }
}
