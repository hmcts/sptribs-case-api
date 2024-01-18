package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

@AllArgsConstructor
@Getter
public enum UserRolesForAccessProfiles implements HasRole {

    CREATOR("[CREATOR]", "CRU"),
    SUPER_USER("idam:caseworker-sptribs-superuser", "CRU"),
    SYSTEMUPDATE("idam:caseworker-sptribs-systemupdate", "CRU"),
    CASEWORKER("idam:caseworker", "CRU"),

    ST_CIC_CASEWORKER("idam:caseworker-st_cic-caseworker", "CRU"),
    ST_CIC_SENIOR_CASEWORKER("idam:caseworker-st_cic-senior-caseworker", "CRU"),
    ST_CIC_HEARING_CENTRE_ADMIN("idam:caseworker-st_cic-hearing-centre-admin", "CRU"),
    ST_CIC_HEARING_CENTRE_TEAM_LEADER("idam:caseworker-st_cic-hearing-centre-team-leader", "CRU"),
    ST_CIC_SENIOR_JUDGE("idam:caseworker-st_cic-senior-judge", "CRU"),
    ST_CIC_JUDGE("idam:caseworker-st_cic-judge", "CRU"),
    ST_CIC_RESPONDENT("idam:caseworker-st_cic-respondent", "CRU"),
    CITIZEN_CIC("idam:citizen", "CRU"),

    // Below are the Access Profiles for the Idam Roles
    CIC_SUPER_USER("caseworker-sptribs-superuser", "CRU"),
    AC_SYSTEMUPDATE("caseworker-sptribs-systemupdate", "CRU"),
    CIC_CASEWORKER("caseworker-st_cic-caseworker", "CRU"),
    CIC_SENIOR_CASEWORKER("caseworker-st_cic-senior-caseworker", "CRU"),
    CIC_CENTRE_ADMIN("caseworker-st_cic-hearing-centre-admin", "CRU"),
    CIC_CENTRE_TEAM_LEADER("caseworker-st_cic-hearing-centre-team-leader", "CRU"),
    CIC_SENIOR_JUDGE("caseworker-st_cic-senior-judge", "CRU"),
    CIC_JUDGE("caseworker-st_cic-judge", "CRU"),
    CIC_RESPONDENT("caseworker-st_cic-respondent", "CRU"),
    AC_CASEWORKER("caseworker", "CRU"),
    AC_CITIZEN("citizen", "CRU"),
    AC_CASEFLAGS_ADMIN("caseflags-admin", "CRU"),
    AC_CASEFLAGS_VIEWER("caseflags-viewer", "R");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;

}

