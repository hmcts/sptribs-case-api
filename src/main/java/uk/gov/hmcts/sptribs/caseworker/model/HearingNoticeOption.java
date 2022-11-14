package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@RequiredArgsConstructor
@Getter
public enum HearingNoticeOption implements HasLabel {

    @JsonProperty("Upload from your computer")
    UPLOAD_FROM_COMPUTER("Upload from your computer", "Upload from your computer"),

    @JsonProperty("Create from a template")
    CREATE_FROM_TEMPLATE("Create from a template", "Create from a template");

    private final String reason;
    private final String label;

    public String getReason() {
        return this.reason;
    }

    public String getLabel() {
        return this.label;
    }

}
