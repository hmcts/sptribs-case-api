package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@RequiredArgsConstructor
@Getter
public enum CreateAndSendIssuingType implements HasLabel {

    @JsonProperty("NewOrder")
    CREATE_AND_SEND_NEW_ORDER("NewOrder", "Create and send a new order"),

    @JsonProperty("UploadOrder")
    UPLOAD_A_NEW_ORDER_FROM_YOUR_COMPUTER("UploadOrder", "Upload a new order from your computer");

    private final String reason;
    private final String label;

}
