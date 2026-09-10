package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * Who signed the declaration — SEND35 signature block.
 */
@Getter
@AllArgsConstructor
public enum StSend35SignatoryRole implements HasLabel {

    @JsonProperty("parentOrCarer")
    PARENT_OR_CARER("Parent or carer"),

    @JsonProperty("youngPerson")
    YOUNG_PERSON("Young person"),

    @JsonProperty("representative")
    REPRESENTATIVE("Representative");

    private final String label;
}
