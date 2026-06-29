package uk.gov.hmcts.sptribs.taskmanagement.model;

public enum RoleCategory {
    JUDICIAL,
    ADMIN,
    CTSC,
    LEGAL_OPERATIONS,
    NONE;

    public String getName() {
        return this != NONE ? this.name() : null;
    }
}




