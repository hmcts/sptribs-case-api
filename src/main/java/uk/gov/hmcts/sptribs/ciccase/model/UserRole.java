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
    SYSTEMUPDATE("caseworker-sptribs-systemupdate", "CRU"),
    CREATOR("caseworker-sptribs", "CRU"),
    SOLICITOR("caseworker", "CRU"),

    // CIC User roles
    COURT_ADMIN_CIC("caseworker-sptribs-cic-courtadmin", "CRU"),
    CASE_OFFICER_CIC("caseworker-sptribs-cic-caseofficer", "CRU"),
    DISTRICT_REGISTRAR_CIC("caseworker-sptribs-cic-districtregistrar", "CRU"),
    DISTRICT_JUDGE_CIC("caseworker-sptribs-cic-districtjudge", "CRU"),
    CITIZEN_CIC("citizen-sptribs-cic-dss", "C");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;

    public static UserRole fromString(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.getRole().equals(value)) {
                return role;
            }
        }
        return null;
    }

}
