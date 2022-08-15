package uk.gov.hmcts.divorce.ciccase.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

@AllArgsConstructor
@Getter
public enum UserRoleMH implements HasRole {

    COURT_ADMIN("caseworker-sptribs-mh-courtadmin", "CRU"),
    CASE_OFFICER("caseworker-sptribs-mh-caseofficer", "CRU"),
    DISTRICT_REGISTRAR("caseworker-sptribs-mh-districtregistrar", "CRU"),
    DISTRICT_JUDGE("caseworker-sptribs-mh-districtjudge", "CRU"),
    CITIZEN("citizen-sptribs-mh-dss", "C");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;

    public static UserRoleMH fromString(String value) {
        for (UserRoleMH role : UserRoleMH.values()) {
            if (role.getRole().equalsIgnoreCase(value)) {
                return role;
            }
        }
        return null;
    }

}
