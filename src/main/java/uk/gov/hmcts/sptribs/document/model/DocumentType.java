package uk.gov.hmcts.sptribs.document.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum DocumentType implements HasLabel {

    // New added doc types

    @JsonProperty("ApplicationForm")
    @JsonAlias({"A Docs"})
    APPLICATION_FORM("A - Application Form", "Application Form", "A"),

    @JsonProperty("First decision")
    @JsonAlias({"A Docs"})
    FIRST_DECISION("A - First decision", "First decision", "A"),

    @JsonProperty("Application for review")
    @JsonAlias({"A Docs"})
    APPLICATION_FOR_REVIEW("A - Application for review", "Application for review", "A"),

    @JsonProperty("Review decision")
    @JsonAlias({"A Docs"})
    REVIEW_DECISION("A - Review decision", "Review decision", "A"),

    @JsonProperty("Notice of Appeal")
    @JsonAlias({"A Docs"})
    NOTICE_OF_APPEAL("A - Notice of Appeal", "Notice of Appeal", "A"),

    @JsonProperty("Evidence/correspondence from the Appellant")
    @JsonAlias({"A Docs"})
    EVIDENCE_CORRESPONDENCE_FROM_THE_APPELLANT("A - Evidence/correspondence from the Appellant",
        "Evidence/correspondence from the Appellant", "A"),

    @JsonProperty("Correspondence from the CICA")
    @JsonAlias({"A Docs"})
    CORRESPONDENCE_FROM_THE_CICA("A - Correspondence from the CICA", "Correspondence from the CICA", "A"),

    @JsonProperty("Direction / decision notices")
    @JsonAlias({"Direction / decision notices", "TD"})
    TRIBUNAL_DIRECTION("TD - Direction / decision notices", "Direction / decision notices", "TD"),

    @JsonProperty("PoliceEvidence")
    @JsonAlias({"B Docs"})
    POLICE_EVIDENCE("B - Police evidence", "Police evidence", "B"),

    @JsonProperty("GP records")
    @JsonAlias({"C Docs"})
    GP_RECORDS("C - GP records", "GP records", "C"),

    @JsonProperty("Hospital records")
    @JsonAlias({"C Docs"})
    HOSPITAL_RECORDS("C - Hospital records", "Hospital records", "C"),

    @JsonProperty("Mental Health records")
    @JsonAlias({"C Docs"})
    MENTAL_HEALTH_RECORDS("C - Mental Health records", "Mental Health records", "C"),

    @JsonProperty("Expert evidence")
    @JsonAlias({"C Docs"})
    EXPERT_EVIDENCE("C - Expert evidence", "Expert evidence", "C"),

    @JsonProperty("Other medical records")
    @JsonAlias({"C Docs"})
    OTHER_MEDICAL_RECORDS("C - Other medical records", "Other medical records", "C"),

    @JsonProperty("DWP records")
    @JsonAlias({"DWP records", "D Docs"})
    DWP_RECORDS("D - DWP records", "DWP records", "D"),

    @JsonProperty("HMRC records")
    @JsonAlias({"HMRC records", "D Docs"})
    HMRC_RECORDS("D - HMRC records", "HMRC records", "D"),

    @JsonProperty("Employment records")
    @JsonAlias({"Employment records", "D Docs"})
    EMPLOYMENT_RECORDS("D - Employment records", "Employment records", "D"),

    @JsonProperty("Schedule of Loss")
    @JsonAlias({"Schedule of Loss", "D Docs"})
    SCHEDULE_OF_LOSS("D - Schedule of Loss", "Schedule of Loss", "D"),

    @JsonProperty("Counter Schedule")
    @JsonAlias({"Counter Schedule", "D Docs"})
    COUNTER_SCHEDULE("D - Counter Schedule", "Counter Schedule", "D"),

    @JsonProperty("Other-D")
    @JsonAlias({"Other-D", "D Docs"})
    OTHER_FINANCIAL("D - Other", "Other-D", "D"),

    @JsonProperty("Care plan")
    @JsonAlias({"E Docs"})
    CARE_PLAN("E - Care plan", "Care plan", "E"),

    @JsonProperty("Local Authority/care records")
    @JsonAlias({"E Docs"})
    LOCAL_AUTHORITY_CARE_RECORDS("E - Local Authority/care records", "Local Authority/care records", "E"),

    @JsonProperty("Other-E")
    @JsonAlias({"E Docs"})
    OTHER_EVIDENCE("E - Other", "Other-E", "E"),

    @JsonProperty("Linked docs")
    @JsonAlias({"L Docs"})
    LINKED_DOCS("L - Linked docs", "Linked docs", "L"),

    @JsonProperty("Witness Statement")
    @JsonAlias({"S Docs"})
    WITNESS_STATEMENT("S - Witness Statement", "Witness Statement", "S"),

    @JsonProperty("Application for an extension of time")
    @JsonAlias({"TG Docs"})
    APPLICATION_FOR_AN_EXTENSION_OF_TIME("TG - Application for an extension of time", "Application for an extension of time", "TG"),

    @JsonProperty("Application for a postponement")
    @JsonAlias({"TG Docs"})
    APPLICATION_FOR_A_POSTPONEMENT("TG - Application for a postponement", "Application for a postponement", "TG"),

    @JsonProperty("Submission from appellant")
    @JsonAlias({"TG Docs"})
    SUBMISSION_FROM_APPELLANT("TG - Submission from appellant", "Submission from appellant", "TG"),

    @JsonProperty("Submission from respondent")
    @JsonAlias({"TG Docs"})
    SUBMISSION_FROM_RESPONDENT("TG - Submission from respondent", "Submission from respondent", "TG"),

    @JsonProperty("Correspondence documents")
    @JsonAlias({"Correspondence Docs"})
    CORRESPONDENCE("Correspondence documents", "Correspondence documents", "CD"),

    @JsonProperty("Other-TG")
    @JsonAlias({"TG Docs"})
    OTHER_GENERAL_EVIDENCE("TG - Other", "Other-TG", "TG"),

    @JsonProperty("DSS Tribunal form uploaded documents")
    @JsonAlias({"DSS Docs"})
    DSS_TRIBUNAL_FORM("DSS Tribunal form uploaded documents", "DSS Tribunal form uploaded documents", "DSS"),

    @JsonProperty("DSS Supporting uploaded documents")
    @JsonAlias({"DSS Docs"})
    DSS_SUPPORTING("DSS Supporting uploaded documents", "DSS Supporting uploaded documents", "DSS"),

    @JsonProperty("DSS Other information documents")
    @JsonAlias({"DSS Docs"})
    DSS_OTHER("DSS Other information documents", "DSS Other information documents", "DSS");


    private final String label;
    private final String type;
    private final String category;

}
