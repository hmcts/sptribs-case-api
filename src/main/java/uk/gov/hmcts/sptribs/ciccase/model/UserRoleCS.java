package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

@AllArgsConstructor
@Getter
public enum UserRoleCS implements HasRole {

    COURT_ADMIN("caseworker-sptribs-cs-courtadmin", "CRU"),
    CASE_OFFICER("caseworker-sptribs-cs-caseofficer", "CRU"),
    DISTRICT_REGISTRAR("caseworker-sptribs-cs-districtregistrar", "CRU"),
    DISTRICT_JUDGE("caseworker-sptribs-cs-districtjudge", "CRU"),
    CITIZEN("citizen-sptribs-cs-dss", "C");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;

    public static UserRoleCS fromString(String value) {
        for (UserRoleCS role : UserRoleCS.values()) {
            if (role.getRole().equalsIgnoreCase(value)) {
                return role;
            }
        }
        return null;
    }

}
