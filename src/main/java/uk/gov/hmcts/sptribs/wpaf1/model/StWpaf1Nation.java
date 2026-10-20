package uk.gov.hmcts.sptribs.wpaf1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * Where the appellant lives — the jurisdiction question.
 *
 * <p>The War Pensions and Armed Forces Compensation Chamber covers England and Wales only.
 * Scotland is the Pensions Appeal Tribunal for Scotland and Northern Ireland is PAT NI, both
 * separate tribunals with their own addresses; the citizen journey sends those appellants to
 * a dead end and creates no case. All four are kept so the answer is recorded as the fact it
 * is rather than inferred from the case existing.
 */
@Getter
@AllArgsConstructor
public enum StWpaf1Nation implements HasLabel {

    @JsonProperty("england")
    ENGLAND("England"),

    @JsonProperty("wales")
    WALES("Wales"),

    @JsonProperty("scotland")
    SCOTLAND("Scotland"),

    @JsonProperty("northernIreland")
    NORTHERN_IRELAND("Northern Ireland");

    private final String label;
}
