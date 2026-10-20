package uk.gov.hmcts.sptribs.wpaf1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * Who signed — WPAF1 signature blocks.
 *
 * <p>The same three roles sign both the UK Special Forces confirmation and the declaration.
 */
@Getter
@AllArgsConstructor
public enum StWpaf1SignatoryRole implements HasLabel {

    @JsonProperty("appellant")
    APPELLANT("Appellant"),

    @JsonProperty("representative")
    REPRESENTATIVE("Representative"),

    @JsonProperty("appointee")
    APPOINTEE("Appointee");

    private final String label;
}
