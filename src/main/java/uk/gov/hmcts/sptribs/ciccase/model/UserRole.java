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
    SUPER_USER("caseworker-sptribs-superuser", "D"),
    SYSTEMUPDATE("caseworker-sptribs-systemupdate", "D"),
    SOLICITOR("caseworker", "D"),
    CREATOR("[CREATOR]", "D"),

    // CIC User roles
    COURT_ADMIN_CIC("caseworker-sptribs-cic-courtadmin", "D"),
    CASE_OFFICER_CIC("caseworker-sptribs-cic-caseofficer", "D"),
    DISTRICT_REGISTRAR_CIC("caseworker-sptribs-cic-districtregistrar", "D"),
    DISTRICT_JUDGE_CIC("caseworker-sptribs-cic-districtjudge", "D"),
    RESPONDENT_CIC("caseworker-sptribs-cic-respondent", "D"),

    ST_CIC_CASEWORKER("caseworker-st_cic-caseworker", "D"),
    ST_CIC_SENIOR_CASEWORKER("caseworker-st_cic-senior-caseworker", "D"),
    ST_CIC_HEARING_CENTRE_ADMIN("caseworker-st_cic-hearing-centre-admin", "D"),
    ST_CIC_HEARING_CENTRE_TEAM_LEADER("caseworker-st_cic-hearing-centre-team-leader", "D"),
    ST_CIC_SENIOR_JUDGE("caseworker-st_cic-senior-judge", "D"),

    ST_CIC_JUDGE("caseworker-st_cic-judge", "D"),
    ST_CIC_RESPONDENT("caseworker-st_cic-respondent", "D"),

    CITIZEN_CIC("citizen-sptribs-cic-dss", "C");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;
}
