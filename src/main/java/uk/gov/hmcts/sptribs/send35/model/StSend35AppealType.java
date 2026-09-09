package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * What the appeal is about — SEND35 question 8.1, more than one may apply.
 *
 * <p>A refusal to assess, or a refusal to agree to assess, is not appealable on this
 * form: that is SEND35A.
 */
@Getter
@AllArgsConstructor
public enum StSend35AppealType implements HasLabel {

    @JsonProperty("refusedToMakePlan")
    REFUSED_TO_MAKE_PLAN("The local authority refused to make an EHC plan"),

    @JsonProperty("refusedReassessment")
    REFUSED_REASSESSMENT("The local authority refused to secure a reassessment of EHC needs"),

    @JsonProperty("planContent")
    PLAN_CONTENT("I disagree with something written in Section B, F or I of the EHC plan"),

    @JsonProperty("planNoLongerNecessary")
    PLAN_NO_LONGER_NECESSARY("The local authority decided a plan is no longer necessary");

    private final String label;
}
