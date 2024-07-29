package uk.gov.hmcts.sptribs.dmn.domain.entities.task;

import java.util.List;

public class Role {
    private final String roleCategory;

    private final String roleName;

    private final List<String> permissions;

    private final List<String> authorisations;

    public Role(String roleCategory, String roleName, List<String> permissions, List<String> authorisations) {
        this.roleCategory = roleCategory;
        this.roleName = roleName;
        this.permissions = permissions;
        this.authorisations = authorisations;
    }
}
