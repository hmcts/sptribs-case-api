package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

@AllArgsConstructor
@Getter
public enum UserRoleCIC implements HasRole {

    COURT_ADMIN("caseworker-sptribs-cic-courtadmin", "CRU"),
    CASE_OFFICER("caseworker-sptribs-cic-caseofficer", "CRU"),
    DISTRICT_REGISTRAR("caseworker-sptribs-cic-districtregistrar", "CRU"),
    DISTRICT_JUDGE("caseworker-sptribs-cic-districtjudge", "CRU"),
    RESPONDENT("caseworker-sptribs-cic-respondent", "CRU"),
    CITIZEN("citizen-sptribs-cic-dss", "C");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;

    public static UserRoleCIC fromString(String value) {
        for (UserRoleCIC role : UserRoleCIC.values()) {
            if (role.getRole().equalsIgnoreCase(value)) {
                return role;
            }
        }
        return null;
    }

}
