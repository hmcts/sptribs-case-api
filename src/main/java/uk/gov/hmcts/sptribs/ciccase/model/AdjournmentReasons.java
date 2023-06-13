package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum AdjournmentReasons implements HasLabel {

    @JsonProperty("Adjourned to face to face")
    ADJOURNED_FACE_TO_FACE("Adjourned to face to face"),

    @JsonProperty(" Adjourned to Video")
    ADJOURNED_TO_VIDEO(" Adjourned to Video"),

    @JsonProperty("Admin error")
    ADMIN_ERROR("Admin error"),

    @JsonProperty("Appellant did not attend")
    APPELLANT_DID_NOT_ATTEND("Appellant did not attend"),

    @JsonProperty("Appellant did not have bundle")
    APPELLANT_DID_NOT_HAVE_BUNDLE("Appellant did not have bundle"),

    @JsonProperty(" Appellant not ready to proceed")
    APPELLANT_NOT_READY_TO_PROCEED(" Appellant not ready to proceed"),

    @JsonProperty("Complex case")
    complex_case("Complex case"),

    @JsonProperty("Failure to comply with directions")
    failure_to_comply_with_directions("Failure to comply with directions"),

    @JsonProperty(" For Legal Rep/No Sol")
    FOR_LEGALE_REP_NO_SOL(" For Legal Rep/No Sol"),

    @JsonProperty(" For Other Parties to Attend")
    FOR_OTHER_PARTIES_TO_ATTEND("For Other Parties to Attend"),

    @JsonProperty(" Further evidence received at hearing")
    FURTHER_EVIDENCE_RECEIVED_AT_COURT("Further evidence received at hearing"),

    @JsonProperty("  Further evidence supplied but not before Tribunal at hearing")
    FURTHER_EVIDENCE_SUPPLIED_BUT_NOT_BEFORE_HEARING("Further evidence supplied but not before Tribunal at hearing"),

    @JsonProperty("Further Loss of Earnings information required - Appellant")
    FURTHER_LOSS_OF_EARNINGS_INFORMATION_REQUIRED_APPELLANT("Further Loss of Earnings information required - Appellant"),

    @JsonProperty("Further Loss of Earnings information required - Respondent")
    FURTHER_LOSS_OF_EARNINGS_INFORMATION_REQUIRED_RESPONDANT("Further Loss of Earnings information required - Respondent"),

    @JsonProperty("  Further medical evidence required - Appellant")
    FURTHER_MEDICAL_EVIDENCE_REQUIRED_APPELLANT("  Further medical evidence required - Appellant"),

    @JsonProperty("  Further medical evidence required - Respondent")
    FURTHER_MEDICAL_EVIDENCE_REQUIRED_RESPONDANT("  Further medical evidence required - Respondent"),

    @JsonProperty("  Further police evidence required - Respondent")
    FURTHER_POLICEL_EVIDENCE_REQUIRED_RESPONDANT("  Further police evidence required - Respondent"),

    @JsonProperty("  Further police evidence required - Appellant")
    FURTHER_POLICEL_EVIDENCE_REQUIRED_APPELLANT("  Further police evidence required - Appellant"),

    @JsonProperty("  Further police evidence required - HMCTS (Summons)")
    FURTHER_POLICEL_EVIDENCE_REQUIRED_HMCTS_SUMMONS("  Further police evidence required - HMCTS (Summons)"),

    @JsonProperty("  Insufficient time")
    INSUFFICIENT_TIME("   Insufficient time"),


    @JsonProperty("Interpreter required")
    INTERPRETER_REQUIRED("Interpreter required"),


    @JsonProperty("Member Unable to Attend")
    MEMBER_UNABLE_TO_ATTEND("Member Unable to Attend"),

    @JsonProperty("PO did not attend")
    PO_DID_NOT_ATTEND("PO did not attend"),


    @JsonProperty(" Poor Evidence")
    POOR_EVIDENCE(" Poor Evidence"),

    @JsonProperty("Venue not suitable")
    VENUE_NOT_SUITABLE("Venue not suitable"),

    @JsonProperty("Witness did not attend")
    WITNESS_DID_NOT_ATTEND("Witness did not attend"),


    @JsonProperty("Other")
    OTHER("Other");


    private final String label;


}

