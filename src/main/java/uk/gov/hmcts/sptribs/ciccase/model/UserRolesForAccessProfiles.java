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
    ST_CIC_WA_CONFIG_USER("idam:caseworker-wa-task-configuration", "CRU"),
    CASEWORKER_ADMIN("idam:caseworker-admin", "CRU"),
    CASE_OFFICER_CIC("idam:caseworker-sptribs-cic-caseofficer", "CRU"),
    COURT_ADMIN_CIC("idam:caseworker-sptribs-cic-courtadmin", "CRU"),
    DISTRICT_JUDGE_CIC("idam:caseworker-sptribs-cic-districtjudge", "CRU"),

    // Below are the Access Profiles for the Idam Roles
    CIC_SUPER_USER("caseworker-sptribs-superuser", "CRU"),
    AC_SYSTEMUPDATE("caseworker-sptribs-systemupdate", "CRU"),
    CIC_CASEWORKER_PROFILE("caseworker-st_cic-caseworker", "CRU"),
    CIC_SENIOR_CASEWORKER_PROFILE("caseworker-st_cic-senior-caseworker", "CRU"),
    CIC_CENTRE_ADMIN_PROFILE("caseworker-st_cic-hearing-centre-admin", "CRU"),
    CIC_CENTRE_TEAM_LEADER_PROFILE("caseworker-st_cic-hearing-centre-team-leader", "CRU"),

    CIC_SENIOR_JUDGE("caseworker-st_cic-senior-judge", "CRU"),
    CIC_JUDGE("caseworker-st_cic-judge", "CRU"),
    CIC_RESPONDENT("caseworker-st_cic-respondent", "CRU"),
    AC_CASEWORKER("caseworker", "CRU"),
    AC_CITIZEN("citizen", "CRU"),
    AC_ST_CIC_WA_CONFIG_USER("caseworker-wa-task-configuration", "CRU"),
    AP_CASEWORKER_ADMIN("caseworker-admin", "CRU"),
    AC_CASEFLAGS_ADMIN("caseflags-admin", "CRU"),
    AC_CASEFLAGS_VIEWER("caseflags-viewer", "R"),
    AP_HMCTS_ADMIN("hmcts-admin", "CRU"),
    AP_CASE_OFFICER_CIC("caseworker-sptribs-cic-caseofficer", "CRU"),
    AP_COURT_ADMIN_CIC("caseworker-sptribs-cic-courtadmin", "CRU"),
    AP_DISTRICT_JUDGE_CIC("caseworker-sptribs-cic-districtjudge", "CRU"),

    // Staff Roles of role assignment
    SENIOR_TRIBUNAL_CASE_WORKER("senior-tribunal-caseworker", "CRU"),
    TRIBUNAL_CASE_WORKER("tribunal-caseworker", "CRU"),
    HEARING_CENTRE_TEAM_LEADER("hearing-centre-team-leader", "CRU"),
    HEARING_CENTRE_ADMIN("hearing-centre-admin", "CRU"),
    REGIONAL_CENTRE_TEAM_LEADER("regional-centre-team-leader", "CRU"),
    REGIONAL_CENTRE_ADMIN("regional-centre-admin", "CRU"),
    CTSC_TEAM_LEADER("ctsc-team-leader", "CRU"),
    CTSC_ADMIN("ctsc", "CRU"),
    HMCTS_CTSC("hmcts-ctsc", "CRU"),
    HMCTS_ADMIN("hmcts-admin", "CRU"),
    HMCTS_LEGAL_OPERATIONS("hmcts-legal-operations", "CRU"),
    CICA("cica", "CRU"),
    // Staff Roles of role assignment
    SENIOR_JUDGE("senior-judge", "CRU"),
    JUDGE("judge", "CRU");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;

}

