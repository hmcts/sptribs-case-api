package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@RequiredArgsConstructor
@Getter
public enum OrderIssuingType implements HasLabel {

    @JsonProperty("DraftOrder")
    ISSUE_AND_SEND_AN_EXISTING_DRAFT("DraftOrder", "Issue and send an existing draft"),


    @JsonProperty("UploadOrder")
    UPLOAD_A_NEW_ORDER_FROM_YOUR_COMPUTER("UploadOrder", "Upload a new order from your computer");

    private final String reason;
    private final String label;

    public String getReason() {
        return this.reason;
    }

    public String getLabel() {
        return this.label;
    }

}
