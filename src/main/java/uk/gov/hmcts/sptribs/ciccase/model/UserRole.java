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
    SUPER_USER("caseworker-sptribs-superuser", "CRUD"),
    SYSTEMUPDATE("caseworker-sptribs-systemupdate", "CRUD"),
    SOLICITOR("caseworker", "CRUD"),
    CREATOR("[CREATOR]", "CRUD"),

    // CIC User roles
    COURT_ADMIN_CIC("caseworker-sptribs-cic-courtadmin", "CRUD"),
    CASE_OFFICER_CIC("caseworker-sptribs-cic-caseofficer", "CRUD"),
    DISTRICT_REGISTRAR_CIC("caseworker-sptribs-cic-districtregistrar", "CRUD"),
    DISTRICT_JUDGE_CIC("caseworker-sptribs-cic-districtjudge", "CRUD"),
    RESPONDENT_CIC("caseworker-sptribs-cic-respondent", "CRUD"),

    ST_CIC_CASEWORKER("caseworker-st_cic-caseworker", "CRUD"),
    ST_CIC_SENIOR_CASEWORKER("caseworker-st_cic-senior-caseworker", "CRUD"),
    ST_CIC_HEARING_CENTRE_ADMIN("caseworker-st_cic-hearing-centre-admin", "CRUD"),
    ST_CIC_HEARING_CENTRE_TEAM_LEADER("caseworker-st_cic-hearing-centre-team-leader", "CRUD"),
    ST_CIC_SENIOR_JUDGE("caseworker-st_cic-senior-judge", "CRUD"),

    ST_CIC_JUDGE("caseworker-st_cic-judge", "CRUD"),
    ST_CIC_RESPONDENT("caseworker-st_cic-respondent", "CRUD"),
    AC_CASEFLAGS_ADMIN("caseflags-admin", "CRUD"),
    AC_CASEFLAGS_VIEWER("caseflags-viewer", "R"),
    CITIZEN_CIC("citizen", "CRUD");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;
}
