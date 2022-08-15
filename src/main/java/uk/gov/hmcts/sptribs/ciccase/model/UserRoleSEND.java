package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

@AllArgsConstructor
@Getter
public enum UserRoleSEND implements HasRole {

    COURT_ADMIN("caseworker-sptribs-send-courtadmin", "CRU"),
    CASE_OFFICER("caseworker-sptribs-send-caseofficer", "CRU"),
    DISTRICT_REGISTRAR("caseworker-sptribs-send-districtregistrar", "CRU"),
    DISTRICT_JUDGE("caseworker-sptribs-send-districtjudge", "CRU"),
    CITIZEN("citizen-sptribs-send-dss", "C"),
    COURT_ADMIN_ASST("caseworker-sptribs-send-courtadminasst", "C");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;

    public static UserRoleSEND fromString(String value) {
        for (UserRoleSEND role : UserRoleSEND.values()) {
            if (role.getRole().equalsIgnoreCase(value)) {
                return role;
            }
        }
        return null;
    }

}
