package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum OrderTemplate implements HasLabel {


    @JsonProperty("CIC6_General_Directions")
    CIC6_GENERAL_DIRECTIONS("CIC6 General Direction", "CIC6_General_Directions"),

    @JsonProperty("CIC7_ME_Dmi_Reports")
    CIC7_ME_DMI_REPORTS("CIC7 me DMI reports", "CIC7_ME_Dmi_Reports"),

    @JsonProperty("CIC8_ME_Joint_Instruction")
    CIC8_ME_JOINT_INSTRUCTION("CIC8 me joint instruction", "CIC8_ME_Joint_Instruction"),

    @JsonProperty("CIC10_Strike_Out_Warning")
    CIC10_STRIKE_OUT_WARNING("CIC10 strike out warning", "CIC10_Strike_Out_Warning"),

    @JsonProperty("CIC13_Pro_Forma_Summons")
    CIC13_PRO_FORMA_SUMMONS("CIC13 pro forma summons", "CIC13_Pro_Forma_Summons");

    private final String label;
    private String id;
}
