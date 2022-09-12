package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum StayReason implements HasLabel {

    @JsonProperty("waitingOutcomeOfCivilCase")
    AWAITING_OUTCOME_OF_A_CIVIL_CASE("Awaiting outcome of civil case","Awaiting outcome of civil case"),

    @JsonProperty("awaitingOutcomeOfCriminalProceedings")
    AWAITING_OUTCOME_OF_A_CRIMINAL_PROCEEDINGS("Awaiting outcome of criminal proceedings","Awaiting outcome of criminal proceedings"),

    @JsonProperty("awaitingACourtJudgement")
    AWAITING_OUTCOME_OF_A_COURT_JUDGEMENT("Awaiting a court judgement","Awaiting a court judgement"),

    @JsonProperty("unableToProgressDueToSubject’sAge")
    UNABLE_TO_PROGRESS_DUE_TO_SUBJECTS_AGE("Unable to progress due to subject’s age","Unable to progress due to subject’s age"),

    @JsonProperty("unableToProgressAsSubjectUndergoingOrAwaitingTreatment")
    UNABLE_TO_PROGRESS_AS_SUBJECT_UNDERGOING_OR_AWAITING_TREATMENT("Unable to progress as subject undergoing or awaiting treatment",
        "Unable to progress as subject undergoing or awaiting treatment"),

    @JsonProperty("awaitingOutcomeOfLinkedCase")
    AWAITING_OUTCOME_OF_LINKED_CASE("Awaiting outcome of linked case","Awaiting outcome of linked case"),

    @JsonProperty("Other")
    OTHER("Other","Other");

    private String type;
    private final String label;

    public boolean isOther() {
        return OTHER.name().equalsIgnoreCase(this.name());
    }

}
