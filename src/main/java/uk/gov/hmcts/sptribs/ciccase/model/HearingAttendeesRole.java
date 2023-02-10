package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum HearingAttendeesRole implements HasLabel {

    @JsonProperty("appellant")
    APPELLANT("Appellant"),

    @JsonProperty("appraiser")
    APPRAISER("Appraiser"),

    @JsonProperty("Counsel")
    COUNSEL("Counsel"),

    @JsonProperty("interpreter")
    INTERPRETER("Interpreter"),

    @JsonProperty("layMember")
    LAY_MEMBER("Lay member"),

    @JsonProperty("mainAppellant")
    MAIN_APPELLANT("Main Appellant (on behalf of victim)"),

    @JsonProperty("medicalMember")
    MEDICAL_MEMBER("Medical member"),

    @JsonProperty("observer")
    OBSERVER("Observer"),

    @JsonProperty("presentingOfficer")
    PRESENTING_OFFICER("Presenting Officer"),

    @JsonProperty("representativeLegal")
    REPRESENTATIVE_LEGAL("Representative - legal"),

    @JsonProperty("representativeNonLegal")
    REPRESENTATIVE_NON_LEGAL("Representative - non-legal"),

    @JsonProperty("Tribunal clerk")
    TRIBUNAL_CLERK("Tribunal clerk"),

    @JsonProperty("Tribunal Judge")
    TRIBUNAL_JUDGE("Tribunal Judge"),

    @JsonProperty("victim")
    VICTIM("Victim"),

    @JsonProperty("Witness - General")
    WITNESS_GENERAL("Witness - General"),

    @JsonProperty("witnessPolice")
    WITNESS_POLICE("Witness - Police"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;

}
