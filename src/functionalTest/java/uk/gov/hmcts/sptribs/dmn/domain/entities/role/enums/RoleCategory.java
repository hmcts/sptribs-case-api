package uk.gov.hmcts.sptribs.dmn.domain.entities.role.enums;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum RoleCategory {
    JUDICIAL, LEGAL_OPERATIONS, ADMIN, CTSC, @JsonEnumDefaultValue UNKNOWN
}
