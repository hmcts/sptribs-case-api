package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum FinalDecisionTemplate implements HasLabel {

    @JsonProperty("Eligibility")
    ELIGIBILITY("Eligibility", "ST-CIC-DEC-ENG-CIC1_Eligibility"),

    @JsonProperty("Quantum")
    QUANTUM("Quantum", "ST-CIC-DEC-ENG-CIC2_Quantum"),

    @JsonProperty("Rule 27")
    RULE_27("Rule 27", "ST-CIC-DEC-ENG-CIC3_Rule_27"),

    @JsonProperty("Blank Decision Notice 1 ")
    BLANK_DECISION_NOTICE_1("Blank Decision Notice 1 ", "ST-CIC-DEC-ENG-CIC4_Blank_Decision_Notice"),

    @JsonProperty("Strike Out Decision Notice")
    STRIKE_OUT_DECISION_NOTICE("Strike Out Decision Notice", "ST-CIC-DEC-ENG-CIC11_Strike_Out_Decision_Notice");

    private final String label;
    private final String id;

}
