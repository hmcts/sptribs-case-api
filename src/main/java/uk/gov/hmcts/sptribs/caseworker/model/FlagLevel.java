package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@RequiredArgsConstructor
@Getter
public enum FlagLevel implements HasLabel {

    @JsonProperty("PartyLevel")
    PARTY_LEVEL("PartyLevel", "Party"),

    @JsonProperty("CaseLevel")
    CASE_LEVEL("CaseLevel", "Case");


    private final String type;
    private final String label;

    public boolean isPartyLevel() {
        return PARTY_LEVEL.name().equalsIgnoreCase(this.name());
    }

    public boolean isCaseLevel() {
        return CASE_LEVEL.name().equalsIgnoreCase(this.name());
    }
}
