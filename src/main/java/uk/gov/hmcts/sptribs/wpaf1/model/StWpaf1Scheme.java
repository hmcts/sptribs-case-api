package uk.gov.hmcts.sptribs.wpaf1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * Which compensation scheme the decision was made under.
 *
 * <p>The date of the injury decides it: service before 6 April 2005 is the War Pensions
 * Scheme, on or after that date is the Armed Forces Compensation Scheme. It also decides
 * whether Section 6 applies at all — only an AFCS appellant is asked whether they have
 * sought a Reconsideration.
 */
@Getter
@AllArgsConstructor
public enum StWpaf1Scheme implements HasLabel {

    @JsonProperty("warPension")
    WAR_PENSION("War Pensions Scheme (injury caused or made worse by service before 6 April 2005)"),

    @JsonProperty("afcs")
    AFCS("Armed Forces Compensation Scheme (injury caused by service on or after 6 April 2005)");

    private final String label;
}
