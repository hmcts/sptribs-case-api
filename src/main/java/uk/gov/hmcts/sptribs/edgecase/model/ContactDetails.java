package uk.gov.hmcts.sptribs.edgecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ContactDetails implements HasLabel {
    @JsonProperty("email")
    EMAIL("email"),

    @JsonProperty("phone")
    PHONE("phone");

    private final String label;
}
