package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum DecisionTemplate implements HasLabel {
    @JsonProperty("Eligibility")
    ELIGIBILITY("Eligibility", "SPT_CIC1_Eligibility.docx "),

    @JsonProperty("Quantum")
    QUANTUM("Quantum", "SPT_CIC2_Quantum.docx"),

    @JsonProperty("Rule 27")
    RULE_27("Rule 27", "SPT_CIC3_Rule 27.docx"),

    @JsonProperty("Blank Decision Notice 1 ")
    BLANK_DECISION_NOTICE_1("Blank Decision Notice 1 ", "SPT_CIC4_Blank_Decision_Notice.docx"),

    @JsonProperty("Strike Out Decision Notice")
    STRIKE_OUT_DECISION_NOTICE("Strike Out Decision Notice", "SPT_CIC11_Strike_Out_Decision_Notice.docx"),

    @JsonProperty("Decision Annex")
    DECISION_ANNEX("Decision Annex", "SPT_CIC12_Decision_Annex.docx");

    private final String label;
    private final String id;

}
