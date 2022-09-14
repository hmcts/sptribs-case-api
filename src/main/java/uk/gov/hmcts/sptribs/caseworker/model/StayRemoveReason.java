package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum StayRemoveReason implements HasLabel {
    @JsonProperty("receivedOutcomeOfCivilCase")
    RECEIVED_OUTCOME_OF_CIVIL_CASE("Received outcome of civil case","Received outcome of civil case"),

    @JsonProperty("receviedOutcomeOfCriminalProceedings")
    RECEIVED_OUTCOME_OF_CRIMINAL_PROCEEDINGS("Received outcome of criminal proceedings","Received outcome of criminal proceedings"),

    @JsonProperty("receivedACourtJudgement")
    RECEIVED_A_COURT_JUDGEMENT("Received a court judgement","Received a court judgement"),

    @JsonProperty("applicantHasReachedRequiredAge")
    APPLICANT_HAS_REACHED_REQUIRED_AGE("Applicant has reached required age","Applicant has reached required age"),

    @JsonProperty("subjectHasReceivedTheirMedicalTreatment")
    SUBJECT_HAS_RECEIVED_THEIR_MEDICAL_TREATMENT("Subject has received their medical treatment",
        "Subject has received their medical treatment"),

    @JsonProperty("receivedOutcomeOfLinkedCase")
    RECEIVED_OUTCOME_OF_LINKED_CASE("Received outcome of linked case","Received outcome of linked case"),

    @JsonProperty("Other")
    OTHER("Other","Other");

    private String type;
    private final String label;

    public boolean isOther() {
        return OTHER.name().equalsIgnoreCase(this.name());
    }
}
