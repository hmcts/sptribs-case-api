package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@RequiredArgsConstructor
@Getter
public enum PostponeReason implements HasLabel {

    @JsonProperty("Appellant is out of country")
    APPELLANT_IS_OUT_OF_COUNTRY("Appellant is out of country", "Appellant is out of country"),

    @JsonProperty("Appellant seeking legal advice")
    APPELLANT_SEEKING_LEGAL_ADVICE("Appellant seeking legal advice", "Appellant seeking legal advice"),

    @JsonProperty("Appellant unable to attend face to face, change of hearing format requested")
    APPELLANT_UNABLE_TO_ATTEND_FACE_TO_FACE("Appellant unable to attend face to face, change of hearing format requested",
        "Appellant unable to attend face to face, change of hearing format requested"),

    @JsonProperty("Appellant unavailable (holiday/work/appointment/unwell)")
    APPELLANT_UNAVAILABLE("Appellant unavailable (holiday/work/appointment/unwell)",
        "Appellant unavailable (holiday/work/appointment/unwell)"),

    @JsonProperty("Bereavement")
    BEREAVEMENT("Bereavement", "Bereavement"),

    @JsonProperty("Case stayed due to Civil proceedings")
    CASESTAYED_DUE_TO_CIVIL_PROCEEDINGS("Case stayed due to Civil proceedings", "Case stayed due to Civil proceedings"),

    @JsonProperty("CICA requests case be heard by a single Judge as a Rule 27 decision")
    CICA_REQUESTS_CASE_BE_HEARD_BY_A_SINGLE_JUDGE_AS_A_RULE_27_DECISION(
        "CICA requests case be heard by a single Judge as a Rule 27 decision",
        "CICA requests case be heard by a single Judge as a Rule 27 decision"),

    @JsonProperty("CICA seeking Counsel")
    CICA_SEEKING_COUNCIL("CICA seeking Counsel", "CICA seeking Counsel"),

    @JsonProperty("Extension granted")
    EXTENSION_GRANTED("Extension granted", "Extension granted"),

    @JsonProperty("Face to face hearing required")
    FACE_TO_FACE_HEARING_REQUIRED("Face to face hearing required", "Face to face hearing required"),

    @JsonProperty("Last minute submissions i.e. 1-2 weeks prior to hearing")
    LAST_MINUTE_SUBMISSION("Last minute submissions i.e. 1-2 weeks prior to hearing",
        "Last minute submissions i.e. 1-2 weeks prior to hearing"),

    @JsonProperty("Linked cases - to be heard together")
    LINKED_CASE_TO_BE_HEARD_TOGETHER("Linked cases - to be heard together", "Linked cases - to be heard together"),

    @JsonProperty("Member excluded - listed in error")
    MEMBER_EXCLUDED_LISTED_IN_ERROR("Member excluded - listed in error", "Member excluded - listed in error"),

    @JsonProperty("Representative/Solicitor cannot make contact with Appellant")
    REPRESENTATIVE_CANNOT_MAKE_CONTACT_WITH_APPELLANT("Representative/Solicitor cannot make contact with Appellant",
        "Representative/Solicitor cannot make contact with Appellant"),

    @JsonProperty("Representative/Solicitor seeking further evidence")
    REPRESENTATIVE_SEEKING_FURTHER_EVIDENCE("Representative/Solicitor seeking further evidence",
        "Representative/Solicitor seeking further evidence"),

    @JsonProperty("Representative/Solicitor unavailable (holiday/work/appointment/unwell)")
    REPRESENTATIVE_UNAVAILABLE("Representative/Solicitor unavailable (holiday/work/appointment/unwell)",
        "Representative/Solicitor unavailable (holiday/work/appointment/unwell)"),

    @JsonProperty("Tribunal members unavailable (holiday/work/appointment/unwell)")
    TRIBUNAL_MEMBER_UNAVAILABLE("Tribunal members unavailable (holiday/work/appointment/unwell)",
        "Tribunal members unavailable (holiday/work/appointment/unwell)"),

    @JsonProperty("Tribunal members deemed listing time directed inadequate")
    TRIBUNAL_MEMBER_DEEMED_LISTING_TIME_DIRECTED_INADEQUATE("Tribunal members deemed listing time directed inadequate",
        "Tribunal members deemed listing time directed inadequate"),

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
