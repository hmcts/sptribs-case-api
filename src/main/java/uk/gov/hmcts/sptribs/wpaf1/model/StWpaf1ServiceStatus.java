package uk.gov.hmcts.sptribs.wpaf1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * The appellant's service status — WPAF1 question 1.4.
 *
 * <p>A serving appellant is also asked for their proposed discharge date, which is what the
 * tribunal uses to judge how quickly a hearing is needed.
 */
@Getter
@AllArgsConstructor
public enum StWpaf1ServiceStatus implements HasLabel {

    @JsonProperty("stillServing")
    STILL_SERVING("Still serving"),

    @JsonProperty("leftService")
    LEFT_SERVICE("Left service");

    private final String label;
}
