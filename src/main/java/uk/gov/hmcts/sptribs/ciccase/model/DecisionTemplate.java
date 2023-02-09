package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum DecisionTemplate implements HasLabel {
    @JsonProperty("Eligibility")
    ELIGIBILITY("Eligibility", "ST-CIC-DEC-ENG-CIC1_Eligibility"),

    @JsonProperty("Quantum")
    QUANTUM("Quantum", "ST-CIC-DEC-ENG-CIC2_Quantum"),

    @JsonProperty("Rule 27")
    RULE_27("Rule 27", "ST-CIC-DEC-ENG-CIC3_Rule_27"),

    @JsonProperty("Blank Decision Notice")
    BLANK_DECISION_NOTICE("Blank Decision Notice", "ST-CIC-DEC-ENG-CIC4_Blank_Decision_Notice"),

    @JsonProperty("General Directions")
    GENERAL_DIRECTIONS("General Directions", "CIC6_General_Directions"),

    @JsonProperty("Medical Evidence - DMI Reports")
    MEDICAL_EVIDENCE_DMI_REPORTS("Medical Evidence - DMI Reports", "CIC7_ME_Dmi_Reports"),

    @JsonProperty("Medical Evidence - Joint Instruction")
    MEDICAL_EVIDENCE_JOINT_REPORTS("Medical Evidence - Joint Instruction", "CIC8_ME_Joint_Instruction"),

    @JsonProperty("Strike Out Warning Directions Notice")
    STRIKE_OUT_WARNING_DIRECTIONS_NOTICE("Strike Out Warning Directions Notice", "CIC10_Strike_Out_Warning"),

    @JsonProperty("Strike Out Decision Notice")
    STRIKE_OUT_DECISION_NOTICE("Strike Out Decision Notice", "ST-CIC-DEC-ENG-CIC11_Strike_Out_Decision_Notice"),

    @JsonProperty("Pro Forma Summmons")
    PRO_FORMA_SUMMONS("Pro Forma Summmons", "CIC13_Pro_Forma_Summons");

    private final String label;
    private final String id;

}
