package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

import static uk.gov.hmcts.sptribs.ciccase.constant.ConditionalOrderCourtConstants.birmingham;
import static uk.gov.hmcts.sptribs.ciccase.constant.ConditionalOrderCourtConstants.buryStEdmunds;

@Getter
@AllArgsConstructor
public enum ConditionalOrderCourt implements HasLabel {

    @JsonProperty("birmingham")
    BIRMINGHAM("birmingham", birmingham),

    @JsonProperty("buryStEdmunds")
    BURY_ST_EDMUNDS("buryStEdmunds", buryStEdmunds);

    private String courtId;
    private String label;
}
