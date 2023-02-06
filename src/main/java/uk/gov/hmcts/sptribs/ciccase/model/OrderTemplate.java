package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum OrderTemplate implements HasLabel {


    @JsonProperty("CIC1_Eligibility")
    CIC1ELIGIBILITY("CIC1_Eligibility"),

    @JsonProperty("CIC2_Quantum")
    CIC2_QUANTUM("CIC2_Quantum"),

    @JsonProperty("CIC3_Rule_27")
    CIC3_RULE_27("CIC3_Rule_27"),

    @JsonProperty("CIC4_Blank_Decision_Notice_1")
    CIC4_BLANK_DECISION_NOTICE_1("CIC4_Blank_Decision_Notice_1"),

    @JsonProperty("CIC6_General_Directions")
    CIC6_GENERAL_DIRECTIONS("CIC6_General_Directions"),

    @JsonProperty("CIC7_ME_Dmi_Reports")
    CIC7_ME_DMI_REPORTS("CIC7_ME_Dmi_Reports"),

    @JsonProperty("CIC8_ME_Joint_Instruction")
    CIC8_ME_JOINT_INSTRUCTION("CIC8_ME_Joint_Instruction"),

    @JsonProperty("CIC10_Strike_Out_Warning")
    CIC10_STRIKE_OUT_WARNING("CIC10_Strike_Out_Warning"),

    @JsonProperty("CIC11_Strike_Out_Decision_Notice")
    CIC11_STRIKE_OUT_DECISION_NOTICE("CIC11_Strike_Out_Decision_Notice"),

    @JsonProperty("CIC12_Decision_Annex")
    CIC12_DECISION_ANNEX("ST-CIC-DEC-ENG-CIC12_Decision_Annex"),

    @JsonProperty("CIC13_Pro_Forma_Summons")
    CIC13_PRO_FORMA_SUMMONS("CIC13_Pro_Forma_Summons");

    private final String label;
}
