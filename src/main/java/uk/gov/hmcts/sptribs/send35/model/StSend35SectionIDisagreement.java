package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * The nature of the Section I disagreement — SEND35 question 9.1.
 */
@Getter
@AllArgsConstructor
public enum StSend35SectionIDisagreement implements HasLabel {

    @JsonProperty("disagreeWithNamed")
    DISAGREE_WITH_NAMED("I disagree with the school, college or education provider named in the EHC plan"),

    @JsonProperty("noneNamed")
    NONE_NAMED("The local authority has not named a school, college or education provider in the EHC plan");

    private final String label;
}
