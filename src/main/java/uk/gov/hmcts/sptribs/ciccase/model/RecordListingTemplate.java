package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecordListingTemplate {
    @JsonProperty("Hearing invite - CVP")
    HEARING_INVITE_CVP("Hearing invite - CVP"),

    @JsonProperty("Hearing invite - F2F")
    HEARING_INVITE_F2F("Hearing invite - F2F"),

    @JsonProperty("Hearing invite - telephone")
    HEARING_INVITE_TELEPHONE("Hearing invite - telephone");

    private final String label;
}
