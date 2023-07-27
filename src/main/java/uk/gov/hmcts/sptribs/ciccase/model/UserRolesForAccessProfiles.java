package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

@AllArgsConstructor
@Getter
public enum UserRolesForAccessProfiles implements HasRole {

    SUPER_USER("idam:caseworker-sptribs-superuser", "CRU"),
    SYSTEMUPDATE("caseworker-sptribs-systemupdate", "CRU"),
    SOLICITOR("idam:caseworker", "CRU"),
    CREATOR("[CREATOR]", "CRU"),

    ST_CIC_CASEWORKER("idam:caseworker-st_cic-caseworker", "CRU"),
    ST_CIC_SENIOR_CASEWORKER("idam:caseworker-st_cic-senior-caseworker", "CRU"),
    ST_CIC_HEARING_CENTRE_ADMIN("idam:caseworker-st_cic-hearing-centre-admin", "CRU"),
    ST_CIC_HEARING_CENTRE_TEAM_LEADER("idam:caseworker-st_cic-hearing-centre-team-leader", "CRU"),
    ST_CIC_SENIOR_JUDGE("idam:caseworker-st_cic-senior-judge", "CRU"),

    ST_CIC_JUDGE("idam:caseworker-st_cic-judge", "CRU"),
    ST_CIC_RESPONDENT("idam:caseworker-st_cic-respondent", "CRU"),

    CITIZEN_CIC("citizen", "CRU"),

    CIC_SUPER_USER("cic-superuser", "CRU"),
    CIC_CASEWORKER("cic-caseworker", "CRU"),
    CIC_SENIOR_CASEWORKER("cic-senior-caseworker", "CRU"),
    CIC_CENTRE_ADMIN("cic-centre-admin", "CRU"),
    CIC_CENTRE_TEAM_LEADER("cic-centre-team-leader", "CRU"),
    CIC_SENIOR_JUDGE("cic-senior-judge", "CRU"),
    CIC_JUDGE("cic-judge", "CRU"),
    CIC_RESPONDENT("cic-respondent", "CRU");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;

}

