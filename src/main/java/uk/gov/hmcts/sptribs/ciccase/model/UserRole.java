package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.sptribs.common.UserRoleConstant;

@AllArgsConstructor
@Getter
public enum UserRole implements HasRole {

    // Common User roles
    //TODO: update : SUPER_USER, SOLICITOR before prod deploy
    CITIZEN(UserRoleConstant.CITIZEN, UserRoleConstant.CASE_TYPE_PERMISSIONS_CRU),
    ST_CIC_GENERIC(UserRoleConstant.ST_CIC_GENERIC, UserRoleConstant.CASE_TYPE_PERMISSIONS_CRU),
    SUPER_USER("caseworker-sptribs-superuser", "CRU"),
    SYSTEMUPDATE("caseworker-sptribs-systemupdate", "CRU"),
    SOLICITOR("caseworker", "CRU"),
    CREATOR("[CREATOR]", "CRU"),

    // CIC User roles
    COURT_ADMIN_CIC("caseworker-sptribs-cic-courtadmin", "CRU"),
    CASE_OFFICER_CIC("caseworker-sptribs-cic-caseofficer", "CRU"),
    DISTRICT_REGISTRAR_CIC("caseworker-sptribs-cic-districtregistrar", "CRU"),
    DISTRICT_JUDGE_CIC("caseworker-sptribs-cic-districtjudge", "CRU"),
    RESPONDENT_CIC("caseworker-sptribs-cic-respondent", "CRU"),

    ST_CIC_CASEWORKER("caseworker-st_cic-caseworker", "CRU"),
    ST_CIC_SENIOR_CASEWORKER("caseworker-st_cic-senior-caseworker", "CRU"),
    ST_CIC_HEARING_CENTRE_ADMIN("caseworker-st_cic-hearing-centre-admin", "CRU"),
    ST_CIC_HEARING_CENTRE_TEAM_LEADER("caseworker-st_cic-hearing-centre-team-leader", "CRU"),
    ST_CIC_SENIOR_JUDGE("caseworker-st_cic-senior-judge", "CRU"),

    ST_CIC_JUDGE("caseworker-st_cic-judge", "CRU"),
    ST_CIC_RESPONDENT("caseworker-st_cic-respondent", "CRU"),

    CITIZEN_CIC("citizen-sptribs-cic-dss", "C");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;
}
