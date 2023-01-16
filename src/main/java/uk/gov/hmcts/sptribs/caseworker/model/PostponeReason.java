package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@RequiredArgsConstructor
@Getter
public enum PostponeReason implements HasLabel {
    @JsonProperty("Adjourned to face to face")
    ADJOURNED_TO_FACE_TO_FACE("Adjourned to face to face", "Adjourned to face to face"),

    @JsonProperty("Adjourned to Video")
    ADJOURNED_TO_VIDEO("Adjourned to Video", "Adjourned to Video"),

    @JsonProperty("Admin error")
    ADMIN_ERROR("Admin error", "Admin error"),

    @JsonProperty("Appellant did not attend")
    APPELLANT_DID_NOT_ATTEND("Appellant did not attend", "Appellant did not attend"),

    @JsonProperty("Appellant not ready to proceed")
    APPELLANT_NOT_READY_TO_PROCEED("Appellant not ready to proceed", "Appellant not ready to proceed"),

    @JsonProperty("Complex case")
    COMPLEX_CASE("Complex case", "Complex case"),

    @JsonProperty("For Legal Rep/No Sol")
    FOR_LEGAL_REP_NO_SOL("For Legal Rep/No Sol", "For Legal Rep/No Sol"),

    @JsonProperty("For Other Parties to Attend")
    FOR_OTHER_PARTIES_TO_ATTEND("For Other Parties to Attend", "For Other Parties to Attend"),

    @JsonProperty("Further evidence received at hearing")
    FURTHER_EVIDENCE_RECEIVED_AT_HEARING("Further evidence received at hearing", "Further evidence received at hearing"),

    @JsonProperty("Further Loss of Earnings information required - Appellant")
    FURTHER_LOSS_OF_EARNINGS_INFORMATION_REQUIRED_APPELLANT("Further Loss of Earnings information required - Appellant",
        "Further Loss of Earnings information required - Appellant"),

    @JsonProperty("Further Loss of Earnings information required - Respondent")
    FURTHER_LOSS_OF_EARNINGS_INFORMATION_REQUIRED_RESPONDENT("Further Loss of Earnings information required - Respondent",
        "Further Loss of Earnings information required - Respondent"),

    @JsonProperty("Further medical evidence required - Appellant")
    FURTHER_MEDICAL_EVIDENCE_REQUIRED_APPELLANT("Further medical evidence required - Appellant",
        "Further medical evidence required - Appellant"),

    @JsonProperty("Further medical evidence required - Respondent")
    FURTHER_MEDICAL_EVIDENCE_REQUIRED_RESPONDENT("Further medical evidence required - Respondent",
        "Further medical evidence required - Respondent"),

    @JsonProperty("Further police evidence required - Appellant")
    FURTHER_POLICE_EVIDENCE_REQUIRED_APPELLANT("Further police evidence required - Appellant",
        "Further police evidence required - Appellant"),

    @JsonProperty("Further police evidence required - HMCTS (Summons)")
    FURTHER_POLICE_EVIDENCE_REQUIRED_HMCTS_SUMMONS("Further police evidence required - HMCTS (Summons)",
        "Further police evidence required - HMCTS (Summons)"),

    @JsonProperty("Further police evidence required - Respondent")
    FURTHER_POLICE_EVIDENCE_REQUIRED_RESPONDENT("Further police evidence required - Respondent",
        "Further police evidence required - Respondent"),

    @JsonProperty("Interpreter required")
    INTERPRETER_REQUIRED("Interpreter required", "Interpreter required"),

    @JsonProperty("Lack of time")
    LACK_OF_TIME("Lack of time", "Lack of time"),

    @JsonProperty("Member Unable to Attend")
    MEMBER_UNABLE_TO_ATTEND("Member Unable to Attend", "Member Unable to Attend"),

    @JsonProperty("PO did not attend")
    PO_DID_NOT_ATTEND("PO did not attend", "PO did not attend"),

    @JsonProperty("Poor Evidence")
    POOR_EVIDENCE("Poor Evidence", "Poor Evidence"),

    @JsonProperty("Venue room - minimum standards not reached")
    VENUE_ROOM_MINIMUM_STANDARDS_NOT_REACHED("Venue room - minimum standards not reached",
        "Venue room - minimum standards not reached"),

    @JsonProperty("Witness did not attend")
    WITNESS_DID_NOT_ATTEND("Witness did not attend", "Witness did not attend"),

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
