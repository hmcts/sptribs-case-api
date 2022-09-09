package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

@AllArgsConstructor
@Getter
public enum UserRole implements HasRole {

    // Common User roles
    SUPER_USER("caseworker-sptribs-superuser", "CRU"),

    //TODO Create jiira for SYSTEM_UPDATE and SOLICITOR as they are added for AAT env
    SYSTEMUPDATE("caseworker-divorce-systemupdate", "CRU"),
    SOLICITOR("caseworker-divorce-solicitor", "CRU"),
    CITIZEN("citizen", "CRU"),
    CREATOR("[CREATOR]", "CRU"),

    // CIC User roles
    COURT_ADMIN_CIC("caseworker-sptribs-cic-courtadmin", "CRU"),
    CASE_OFFICER_CIC("caseworker-sptribs-cic-caseofficer", "CRU"),
    DISTRICT_REGISTRAR_CIC("caseworker-sptribs-cic-districtregistrar", "CRU"),
    DISTRICT_JUDGE_CIC("caseworker-sptribs-cic-districtjudge", "CRU"),
    RESPONDENT_CIC("caseworker-sptribs-cic-respondent", "CRU"),
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
