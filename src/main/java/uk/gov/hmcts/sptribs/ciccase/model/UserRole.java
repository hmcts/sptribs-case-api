package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

@AllArgsConstructor
@Getter
public enum UserRole implements HasRole {

    // Common User roles
    //TODO: update : SUPER_USER, SOLICITOR before prod deploy
    SUPER_USER("caseworker-sptribs-superuser", "CRU"),
    SYSTEM_UPDATE("caseworker-sptribs-systemupdate", "CRUD"),
    CASEWORKER("caseworker", "CRU"),
    CREATOR("[CREATOR]", "CRU"),

    // CIC User roles
    DISTRICT_JUDGE_CIC("caseworker-sptribs-cic-districtjudge", "CRU"),
    RESPONDENT_CIC("caseworker-sptribs-cic-respondent", "CRU"),

    ST_CIC_CASEWORKER("caseworker-st_cic-caseworker", "CRU"),
    ST_CIC_SENIOR_CASEWORKER("caseworker-st_cic-senior-caseworker", "CRU"),
    ST_CIC_HEARING_CENTRE_ADMIN("caseworker-st_cic-hearing-centre-admin", "CRU"),
    ST_CIC_HEARING_CENTRE_TEAM_LEADER("caseworker-st_cic-hearing-centre-team-leader", "CRU"),
    ST_CIC_SENIOR_JUDGE("caseworker-st_cic-senior-judge", "CRU"),

    ST_CIC_JUDGE("caseworker-st_cic-judge", "CRU"),
    ST_CIC_RESPONDENT("caseworker-st_cic-respondent", "CRU"),
    AC_CASE_FLAGS_ADMIN("caseflags-admin", "CRU"),
    AC_CASE_FLAGS_VIEWER("caseflags-viewer", "R"),

    CITIZEN("citizen", "CRU"),

    GS_PROFILE("GS_profile", "R");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;
}
