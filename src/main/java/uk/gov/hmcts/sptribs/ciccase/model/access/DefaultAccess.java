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


public class DefaultAccess implements HasAccessControl {
    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(CREATOR, Permissions.CREATE_READ_UPDATE);
        grants.putAll(ST_CIC_CASEWORKER, Permissions.CREATE_READ_UPDATE);
        grants.putAll(ST_CIC_SENIOR_CASEWORKER, Permissions.CREATE_READ_UPDATE);
        grants.putAll(ST_CIC_HEARING_CENTRE_ADMIN, Permissions.CREATE_READ_UPDATE);
        grants.putAll(ST_CIC_HEARING_CENTRE_TEAM_LEADER, Permissions.CREATE_READ_UPDATE);
        grants.putAll(ST_CIC_SENIOR_JUDGE, Permissions.CREATE_READ_UPDATE);
        return grants;
    }
}
