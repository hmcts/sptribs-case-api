package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * Gender of the child or young person — SEND35 question 1.1.
 *
 * <p>The paper form leaves this as free text. Offering options with a
 * "prefer not to say" is the GOV.UK Design System pattern and keeps the data usable;
 * see docs/service-design.md in the prototype frontend for the reasoning.
 */
@Getter
@AllArgsConstructor
public enum StSend35Gender implements HasLabel {

    @JsonProperty("female")
    FEMALE("Female"),

    @JsonProperty("male")
    MALE("Male"),

    @JsonProperty("other")
    OTHER("Other"),

    @JsonProperty("preferNotToSay")
    PREFER_NOT_TO_SAY("Prefer not to say");

    private final String label;
}
