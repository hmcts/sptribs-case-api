package uk.gov.hmcts.sptribs.taskmanagement.model;

import org.apache.commons.lang.StringUtils;

public enum RoleCategory {
    JUDICIAL,
    ADMIN,
    CTSC,
    LEGAL_OPERATIONS,
    NONE;

    public String getName() {
        return this != NONE ? this.name() : StringUtils.EMPTY;
    }
}




