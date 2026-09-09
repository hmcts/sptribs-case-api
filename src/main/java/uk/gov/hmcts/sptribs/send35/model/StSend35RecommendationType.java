package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * What the tribunal is asked to recommend on — SEND35 question 11.2, both may apply.
 */
@Getter
@AllArgsConstructor
public enum StSend35RecommendationType implements HasLabel {

    @JsonProperty("health")
    HEALTH("Health — Sections C and G of the EHC plan"),

    @JsonProperty("socialCare")
    SOCIAL_CARE("Social care — Sections D and H of the EHC plan");

    private final String label;
}
