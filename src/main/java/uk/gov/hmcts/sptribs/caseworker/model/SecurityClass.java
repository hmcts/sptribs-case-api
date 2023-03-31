package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@RequiredArgsConstructor
@Getter
public enum SecurityClass implements HasLabel {

    @JsonProperty("PUBLIC")
    PUBLIC("PUBLIC", new String[] {
        "hmcts-judiciary"
    }),

    @JsonProperty("PRIVATE")
    PRIVATE("PRIVATE", new String[] {
        "caseworker-st_cic-senior-judge",
        "caseworker-st_cic-hearing-centre-team-leader",
        "caseworker-st_cic-senior-caseworker"
    }),

    @JsonProperty("Restricted")
    RESTRICTED("RESTRICTED", new String[] {
        "caseworker-st_cic-senior-judge"
    });

    private final String label;
    private final String[] permittedRoles;

}
