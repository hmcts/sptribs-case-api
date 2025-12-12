package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum AdminAction implements HasLabel {
    @JsonProperty("AdminActionRequired")
    ADMIN_ACTION_REQUIRED("Immediate admin action to be taken");

    private final String label;
}
