package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * Which parts of the EHC plan are disputed — SEND35 question 8.3, more than one may apply.
 *
 * <p>Sections B, F and I are the only appealable parts. Health (C and G) and social care
 * (D and H) are recommendations rather than appeals, and are captured separately.
 */
@Getter
@AllArgsConstructor
public enum StSend35PlanSection implements HasLabel {

    @JsonProperty("sectionB")
    SECTION_B("Section B — the child or young person's special educational needs"),

    @JsonProperty("sectionF")
    SECTION_F("Section F — the educational help or provision required"),

    @JsonProperty("sectionI")
    SECTION_I("Section I — the school, college or education provider named");

    private final String label;
}
