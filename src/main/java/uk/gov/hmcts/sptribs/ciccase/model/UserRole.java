package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

@AllArgsConstructor
@Getter
public enum UserRole implements HasRole {

    SUPER_USER("caseworker-sptribs-superuser", "CRU"),

    //TODO: Check with Jeff on adding UserRoleCIC
    CASE_WORKER("caseworker-divorce-courtadmin_beta", "CRU"),

    //TODO Create jiira for SYSTEM_UPDATE and SOLICITOR as they are added for AAT env
    SYSTEMUPDATE("caseworker-divorce-systemupdate", "CRU"),
    SOLICITOR("caseworker-divorce-solicitor", "CRU"),

    CITIZEN("citizen", "CRU"),
    CREATOR("[CREATOR]", "CRU");

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
