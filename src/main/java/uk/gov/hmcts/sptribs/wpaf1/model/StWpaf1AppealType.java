package uk.gov.hmcts.sptribs.wpaf1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * What the appeal is about — WPAF1 question 4.2, more than one may apply.
 *
 * <p>These map onto the statutory appeal categories in the Pensions Appeal Tribunals Act
 * 1943: entitlement is sections 1 to 3, the level of an award is section 5, and the start
 * date, changes to an award and the label are all section 5A.
 */
@Getter
@AllArgsConstructor
public enum StWpaf1AppealType implements HasLabel {

    @JsonProperty("entitlement")
    ENTITLEMENT("Refusal of an award or level of entitlement"),

    @JsonProperty("awardLevel")
    AWARD_LEVEL("The level of an award (percentage assessment, or rate of allowance if less than the maximum or tariff level)"),

    @JsonProperty("awardStartDate")
    AWARD_START_DATE("The date an award starts"),

    @JsonProperty("awardChanges")
    AWARD_CHANGES("Changes to the amount or duration of an award"),

    @JsonProperty("awardLabel")
    AWARD_LABEL("The label (the medical name given for a claimed condition)"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
