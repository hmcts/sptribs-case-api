package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * The capacity in which the declaration is confirmed — SEND35 declaration.
 */
@Getter
@AllArgsConstructor
public enum StSend35DeclarationCapacity implements HasLabel {

    @JsonProperty("onBehalf")
    ON_BEHALF("I confirm, as the person completing this form on behalf of the child or young person, that the facts stated are true"),

    @JsonProperty("youngPersonAlone")
    YOUNG_PERSON_ALONE("I confirm, as the young person applying alone, that the facts stated are true");

    private final String label;
}
