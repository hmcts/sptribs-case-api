package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

@AllArgsConstructor
@Getter
public enum UserRoleCIC implements HasRole {

    SUPER_USER("caseworker-divorce-superuser", "CRU"),
    SYSTEMUPDATE("caseworker-sptribs-systemupdate", "CRU"),
    SOLICITOR("solicitor", "CRU"),
    CREATOR("solicitor-creator", "CRU"),

    // CIC User roles
    SUPER_USER_CIC("caseworker-sptribs-superuser", "CRU"),
    COURT_ADMIN_CIC("caseworker-sptribs-cic-courtadmin", "CRU"),
    CASE_OFFICER_CIC("caseworker-sptribs-cic-caseofficer", "CRU"),
    DISTRICT_REGISTRAR_CIC("caseworker-sptribs-cic-districtregistrar", "CRU"),
    DISTRICT_JUDGE_CIC("caseworker-sptribs-cic-districtjudge", "CRU"),
    RESPONDENT_CIC("caseworker-sptribs-cic-respondent", "CRU"),
    CITIZEN_CIC("citizen-sptribs-cic-dss", "C");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;

    public static UserRoleCIC fromString(String value) {
        for (UserRoleCIC role : UserRoleCIC.values()) {
            if (role.getRole().equals(value)) {
                return role;
            }
        }
        return null;
    }

}
