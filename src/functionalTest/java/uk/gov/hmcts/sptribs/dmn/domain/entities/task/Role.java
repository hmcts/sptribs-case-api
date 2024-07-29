package uk.gov.hmcts.sptribs.dmn.domain.entities.task;

import java.util.ArrayList;

public class Role {
    private final String roleCategory;

    private final String roleName;

    private final ArrayList<String> permissions;

    private final ArrayList<String> authorisations;

    public Role(String roleCategory, String roleName, ArrayList<String> permissions, ArrayList<String> authorisations) {
        this.roleCategory = roleCategory;
        this.roleName = roleName;
        this.permissions = permissions;
        this.authorisations = authorisations;
    }
}
