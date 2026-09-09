package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * Preferred hearing format — SEND35 question 16.1.
 *
 * <p>A paper hearing happens only if the local authority agrees to one; the tribunal
 * decides the format either way, so this is a preference and not an election.
 */
@Getter
@AllArgsConstructor
public enum StSend35HearingType implements HasLabel {

    @JsonProperty("paper")
    PAPER("A paper hearing, decided on the documents and evidence provided"),

    @JsonProperty("attended")
    ATTENDED("A hearing I can attend by video or in person");

    private final String label;
}
