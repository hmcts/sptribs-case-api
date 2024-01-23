package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

@RequiredArgsConstructor
@Getter
public enum SecurityClass implements HasLabel {

    @JsonProperty("PUBLIC")
    PUBLIC("PUBLIC", new String[] {
        UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER.getRole(),
        UserRole.ST_CIC_SENIOR_CASEWORKER.getRole(),
        UserRole.ST_CIC_SENIOR_JUDGE.getRole()
    }),

    @JsonProperty("PRIVATE")
    PRIVATE("PRIVATE", new String[] {
        UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER.getRole(),
        UserRole.ST_CIC_SENIOR_CASEWORKER.getRole(),
        UserRole.ST_CIC_SENIOR_JUDGE.getRole()
    }),

    @JsonProperty("Restricted")
    RESTRICTED("RESTRICTED", new String[] {
        UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER.getRole(),
        UserRole.ST_CIC_SENIOR_CASEWORKER.getRole(),
        UserRole.ST_CIC_SENIOR_JUDGE.getRole()
    });

    private final String label;
    private final String[] permittedRoles;

}
