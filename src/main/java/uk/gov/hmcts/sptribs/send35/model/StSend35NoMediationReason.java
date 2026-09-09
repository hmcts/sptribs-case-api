package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * Why there is no mediation certificate — SEND35 question 12.2.
 *
 * <p>A Section I-only appeal needs no certificate. Any other reason is for a judge to
 * accept or reject, so it has to be explained in full.
 */
@Getter
@AllArgsConstructor
public enum StSend35NoMediationReason implements HasLabel {

    @JsonProperty("sectionIOnly")
    SECTION_I_ONLY("My appeal is only about which school, college or education provider the child or young person should attend"),

    @JsonProperty("otherReason")
    OTHER_REASON("Another reason");

    private final String label;
}
