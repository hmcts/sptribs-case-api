package uk.gov.hmcts.sptribs.ciccase.model.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;


public class ImmutableCollectionDefaultAccess implements HasAccessControl {
    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(CREATOR, Permissions.READ);
        grants.putAll(ST_CIC_CASEWORKER, Permissions.READ);
        grants.putAll(ST_CIC_SENIOR_CASEWORKER, Permissions.READ);
        grants.putAll(ST_CIC_HEARING_CENTRE_ADMIN, Permissions.READ);
        grants.putAll(ST_CIC_HEARING_CENTRE_TEAM_LEADER, Permissions.READ);
        grants.putAll(ST_CIC_SENIOR_JUDGE, Permissions.READ);
        grants.putAll(SYSTEM_UPDATE, Permissions.READ);
        return grants;
    }
}
