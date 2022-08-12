package uk.gov.hmcts.divorce.ciccase.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

@AllArgsConstructor
@Getter
public enum UserRolePHL implements HasRole {

    COURT_ADMIN("caseworker-sptribs-phl-courtadmin", "CRU"),
    CASE_OFFICER("caseworker-sptribs-phl-caseofficer", "CRU"),
    DISTRICT_REGISTRAR("caseworker-sptribs-phl-districtregistrar", "CRU"),
    DISTRICT_JUDGE("caseworker-sptribs-phl-districtjudge", "CRU"),
    CITIZEN("citizen-sptribs-phl-dss", "C");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;

    public static UserRolePHL fromString(String value) {
        for (UserRolePHL role : UserRolePHL.values()) {
            if (role.getRole().equalsIgnoreCase(value)) {
                return role;
            }
        }
        return null;
    }

}
