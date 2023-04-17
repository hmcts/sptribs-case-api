package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

@RequiredArgsConstructor
@Getter
public enum ApplicationEvidence implements HasLabel {

    @JsonProperty("Police evidence")
    POLICE_EVIDENCE("Police evidence", "Police evidence", DocumentType.POLICE_EVIDENCE),

    @JsonProperty("GP records")
    GP_RECORDS("GP records", "GP records", DocumentType.GP_RECORDS),

    @JsonProperty("Hospital records")
    HOSPITAL_RECORDS("Hospital records", "Hospital records", DocumentType.HOSPITAL_RECORDS),

    @JsonProperty("Mental Health records")
    MENTAL_HEALTH_RECORDS("Mental Health records", "Mental Health records", DocumentType.MENTAL_HEALTH_RECORDS),

    @JsonProperty("Expert evidence")
    EXPERT_EVIDENCE("Expert evidence", "Expert evidence", DocumentType.EXPERT_EVIDENCE),

    @JsonProperty("Other medical records")
    OTHER_MEDIAL_RECORDS("Other medical records", "Other medical records", DocumentType.OTHER_MEDICAL_RECORDS),

    @JsonProperty("Application for an extension of time")
    APPLICATION_FOR_AN_EXTENSION_OF_TIME("Application for an extension of time",
        "Application for an extension of time", DocumentType.APPLICATION_FOR_AN_EXTENSION_OF_TIME),

    @JsonProperty("Application for a postponement")
    APPLICATION_FOR_A_POSTPONEMENT("Application for a postponement",
        "Application for a postponements", DocumentType.APPLICATION_FOR_A_POSTPONEMENT),

    @JsonProperty("Submission from appellant")
    SUBMISSION_FROM_APPELLANT("Submission from appellant", "Submission from appellant", DocumentType.SUBMISSION_FROM_APPELLANT),

    @JsonProperty("Submission from respondent")
    SUBMISSION_FROM_RESPONDENT("Submission from respondent",
        "Submission from respondent", DocumentType.SUBMISSION_FROM_RESPONDENT),

    @JsonProperty("Other")
    OTHER("Other", "Other", DocumentType.OTHER_GENERAL_EVIDENCE);

    private final String reason;
    private final String label;
    private final DocumentType documentType;

    public String getReason() {
        return this.reason;
    }

    public String getLabel() {
        return this.label;
    }

    public DocumentType getDocumentType() {
        return this.documentType;
    }
}
