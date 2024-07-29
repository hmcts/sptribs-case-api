package uk.gov.hmcts.sptribs.dmn.domain.entities.task;

import java.util.ArrayList;

public class RoleData {
    private final String key;

    private final Integer numberOfRolesAvailable;

    private final ArrayList<Role> roles;

    public RoleData(String key, Integer numberOfRolesAvailable, ArrayList<Role> roles) {
        this.key = key;
        this.numberOfRolesAvailable = numberOfRolesAvailable;
        this.roles = roles;
    }
}
