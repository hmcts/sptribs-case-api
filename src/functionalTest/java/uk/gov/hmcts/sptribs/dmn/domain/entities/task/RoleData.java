package uk.gov.hmcts.sptribs.dmn.domain.entities.task;

import java.util.List;

public class RoleData {
    private final String key;

    private final Integer numberOfRolesAvailable;

    private final List<Role> roles;

    public RoleData(String key, Integer numberOfRolesAvailable, List<Role> roles) {
        this.key = key;
        this.numberOfRolesAvailable = numberOfRolesAvailable;
        this.roles = roles;
    }
}
