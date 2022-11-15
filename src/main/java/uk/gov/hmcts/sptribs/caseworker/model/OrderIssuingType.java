package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@RequiredArgsConstructor
@Getter
public enum OrderIssuingType implements HasLabel {

    @JsonProperty("Issue and send an existing draft")
    ISSUE_AND_SEND_AN_EXISTING_DRAFT("Issue and send an existing draft", "Issue and send an existing draft"),


    @JsonProperty("Upload a new order from your computer")
    UPLOAD_A_NEW_ORDER_FROM_YOUR_COMPUTER("Upload a new order from your computer", "Upload a new order from your computer");

    private final String reason;
    private final String label;

    public String getReason() {
        return this.reason;
    }

    public String getLabel() {
        return this.label;
    }

}
