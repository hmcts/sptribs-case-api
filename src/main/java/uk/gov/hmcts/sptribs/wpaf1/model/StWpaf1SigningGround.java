package uk.gov.hmcts.sptribs.wpaf1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * Why an appointee is signing instead of the appellant.
 *
 * <p>These are the only two grounds on which an appointee may sign the notice of appeal for
 * someone else, so the ground is captured rather than assumed.
 */
@Getter
@AllArgsConstructor
public enum StWpaf1SigningGround implements HasLabel {

    @JsonProperty("underSixteen")
    UNDER_SIXTEEN("The appellant is under 16"),

    @JsonProperty("infirmity")
    INFIRMITY("The appellant cannot sign because of mental or physical infirmity");

    private final String label;
}
