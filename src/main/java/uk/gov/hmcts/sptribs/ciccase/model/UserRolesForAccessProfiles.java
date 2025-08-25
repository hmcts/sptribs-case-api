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
    IDAM_RAS_CASEWORKER_VALIDATION("idam:caseworker-ras-validation", "CRU"),

    // Below are the Access Profiles for the Idam Roles
    CIC_SUPER_USER("caseworker-sptribs-superuser", "CRU"),
    //Privileged Granted to all users who are allowed to read privileged tabs (in essence this is everyone except the respondent)
    NON_RESPONDENT_PROFILE("non-respondent-user", "R"),
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
    AC_CASEFLAGS_VIEWER("caseflags-viewer", "R"),
    GS_PROFILE("GS_profile", "R"),

    // Read access user for WA post deployment
    ST_CIC_WA_CONFIG_USER("idam:caseworker-wa-task-configuration", "CRU"),
    AC_ST_CIC_WA_CONFIG_USER("caseworker-wa-task-configuration", "CRU"),

    RAS_HMCTS_STAFF("hmcts-staff", "R"),
    RAS_HMCTS_CTSC("hmcts-ctsc", "CRU"),
    RAS_HMCTS_LEGAL_OPERATIONS("hmcts-legal-operations", "CRU"),
    RAS_HMCTS_ADMIN("hmcts-admin", "CRU"),
    RAS_HMCTS_JUDICIARY("hmcts-judiciary", "CRU"),
    RAS_ST_SENIOR_TRIBUNAL_CASEWORKER("senior-tribunal-caseworker", "CRU"),
    RAS_ST_TRIBUNAL_CASEWORKER("tribunal-caseworker", "CRU"),
    RAS_ST_HEARING_CENTRE_TEAM_LEADER("hearing-centre-team-leader", "CRU"),
    RAS_ST_HEARING_CENTRE_ADMIN("hearing-centre-admin", "CRU"),
    RAS_ST_REGIONAL_CENTRE_TEAM_LEADER("regional-centre-team-leader", "CRU"),
    RAS_ST_REGIONAL_CENTRE_ADMIN("regional-centre-admin", "CRU"),
    RAS_ST_CICA("cica", "CRU"),
    RAS_ST_CTSC_TEAM_LEADER("ctsc-team-leader", "CRU"),
    RAS_ST_CTSC("ctsc", "CRU"),
    RAS_ST_CASE_ALLOCATOR("case-allocator", "CRU"),
    RAS_ST_TASK_SUPERVISOR("task-supervisor", "CRU"),

    RAS_ST_SPECIFIC_ACCESS_APPROVER_LEGAL_OPS("specific-access-approver-legal-ops", "CRU"),
    RAS_ST_SPECIFIC_ACCESS_APPROVER_ADMIN("specific-access-approver-admin", "CRU"),
    RAS_ST_SPECIFIC_ACCESS_APPROVER_CTSC("specific-access-approver-ctsc", "CRU"),
    RAS_ST_SPECIFIC_ACCESS_APPROVER_JUDICIARY("specific-access-approver-judiciary", "CRU"),

    RAS_ST_SPECIFIC_ACCESS_LEGAL_OPS("specific-access-legal-ops", "CRU"),
    RAS_ST_SPECIFIC_ACCESS_ADMIN("specific-access-admin", "CRU"),
    RAS_ST_SPECIFIC_ACCESS_CTSC("specific-access-ctsc", "CRU"),
    RAS_ST_SPECIFIC_ACCESS_JUDICIARY("specific-access-judiciary", "CRU"),

    RAS_ST_JUDGE("judge", "CRU"),
    RAS_ST_SENIOR_JUDGE("senior-judge", "CRU"),
    RAS_ST_LEADERSHIP_JUDGE("leadership-judge", "CRU"),
    RAS_ST_FEE_PAID_JUDGE("fee-paid-judge", "CRU"),
    RAS_ST_FEE_PAID_TRIBUNAL_MEMBER("fee-paid-tribunal-member", "CRU"),
    RAS_ST_MEDICAL("medical", "CRU"),
    RAS_ST_FEE_PAID_MEDICAL("fee-paid-medical", "CRU"),
    RAS_ST_FEE_PAID_DISABILITY("fee-paid-disability", "CRU"),
    RAS_ST_FEE_PAID_FINANCIAL("fee-paid-financial", "CRU"),
    RAS_ST_ALLOCATED_JUDGE("allocated-judge", "CRU"),
    RAS_ST_INTERLOC_JUDGE("interloc-judge", "CRU"),
    RAS_ST_TRIBUNAL_MEMBER1("tribunal-member-1", "CRU"),
    RAS_ST_TRIBUNAL_MEMBER2("tribunal-member-2", "CRU"),
    RAS_ST_TRIBUNAL_MEMBER3("tribunal-member-3", "CRU"),
    RAS_ST_APPRAISER1("appraiser-1", "CRU"),
    RAS_ST_APPRAISER2("appraiser-2", "CRU"),

    RAS_CASEWORKER_VALIDATION("caseworker-ras-validation", "R");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;

}

