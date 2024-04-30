package uk.gov.hmcts.sptribs.ciccase.model.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;

public class CaseworkerAccess implements HasAccessControl {

    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(CITIZEN, Permissions.READ);
        grants.putAll(CASEWORKER, Permissions.READ);
        grants.putAll(SUPER_USER, Permissions.READ);

        grants.putAll(SYSTEM_UPDATE, Permissions.CREATE_READ_UPDATE_DELETE);
        return grants;
    }
}
