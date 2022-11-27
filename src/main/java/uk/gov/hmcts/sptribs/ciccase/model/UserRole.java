package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

@AllArgsConstructor
@Getter
public enum UserRole implements HasRole {

    // Common User roles
    SUPER_USER("IDAM:caseworker-divorce-superuser", "CRU"),
    SYSTEMUPDATE("IDAM:caseworker-divorce-systemupdate", "CRU"),
    SOLICITOR("IDAM:caseworker-divorce-solicitor", "CRU"),
    CREATOR("[CREATOR]", "CRU"),

    // CIC User roles
    SUPER_USER_CIC("IDAM:caseworker-sptribs-superuser", "CRU"),
    COURT_ADMIN_CIC("IDAM:caseworker-sptribs-cic-courtadmin", "CRU"),
    CASE_OFFICER_CIC("IDAM:caseworker-sptribs-cic-caseofficer", "CRU"),
    DISTRICT_REGISTRAR_CIC("IDAM:caseworker-sptribs-cic-districtregistrar", "CRU"),
    DISTRICT_JUDGE_CIC("IDAM:caseworker-sptribs-cic-districtjudge", "CRU"),
    RESPONDENT_CIC("IDAM:caseworker-sptribs-cic-respondent", "CRU"),

    HMCTS_STAFF("hmcts-staff", "CRU"),
    HMCTS_JUDICIARY("hmcts-judiciary", "CRU"),

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
    public static String getAccessProfileName(UserRole userRole) {
        for (UserRole role : UserRole.values()) {
            if (role.equals(userRole)) {
                if (role.getRole().startsWith("IDAM:")) {
                    String tempString = role.getRole();
                    return tempString.substring("IDAM:".length());
                }
                else {
                    return role.getRole();
                }
            }
        }
        return null;
    }

}
