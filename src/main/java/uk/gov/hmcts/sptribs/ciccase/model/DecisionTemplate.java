package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum DecisionTemplate implements HasLabel {

    @JsonProperty("CIC1 - Eligibility")
    ELIGIBILITY("CIC1 - Eligibility", "ST-CIC-DEC-ENG-CIC1_Eligibility"),

    @JsonProperty("CIC2 - Quantum")
    QUANTUM("CIC2 - Quantum", "ST-CIC-DEC-ENG-CIC2_Quantum"),

    @JsonProperty("CIC3 - Rule 27")
    RULE_27("CIC3 - Rule 27", "ST-CIC-DEC-ENG-CIC3_Rule_27"),

    @JsonProperty("CIC4 - Blank Decision Notice")
    BLANK_DECISION_NOTICE("CIC4 - Blank Decision Notice", "ST-CIC-DEC-ENG-CIC4_Blank_Decision_Notice"),

    @JsonProperty("CIC6 - General Directions")
    GENERAL_DIRECTIONS("CIC6 - General Directions", "ST-CIC-DEC-ENG-CIC6_General_Directions"),

    @JsonProperty("CIC7 - ME Dmi Reports")
    ME_DMI_REPORTS("CIC7 - ME Dmi Reports", "ST-CIC-DEC-ENG-CIC7_ME_Dmi_Reports"),

    @JsonProperty("CIC8 - ME Joint Instructions")
    ME_JOINT_INSTRUCTION("CIC8 - ME Joint Instructions", "ST-CIC-DEC-ENG-CIC8_ME_Joint_Instruction"),

    @JsonProperty("CIC10 - Strike Out Warning")
    STRIKE_OUT_WARNING("CIC10 - Strike Out Warning", "ST-CIC-DEC-ENG-CIC10_Strike_Out_Warning"),

    @JsonProperty("CIC11 - Strike Out Decision Notice")
    STRIKE_OUT_DECISION_NOTICE("CIC11 - Strike Out Decision Notice", "ST-CIC-DEC-ENG-CIC11_Strike_Out_Decision_Notice"),

    @JsonProperty("CIC13 - Pro Forma Summons")
    PRO_FORMA_SUMMONS("CIC13 - Pro Forma Summons", "ST-CIC-DEC-ENG-CIC13_Pro_Forma_Summons");

    private final String label;
    private final String id;

}
