package uk.gov.hmcts.sptribs.ciccase.model.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.AC_CASE_FLAGS_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.DISTRICT_JUDGE_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.GS_PROFILE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.RESPONDENT_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.DELETE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.READ;

public class GlobalSearchAccess implements HasAccessControl {

    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(GS_PROFILE, READ);
        grants.putAll(SUPER_USER, DELETE);
        grants.putAll(SYSTEM_UPDATE, DELETE);
        grants.putAll(CREATOR, DELETE);
        grants.putAll(DISTRICT_JUDGE_CIC, DELETE);
        grants.putAll(RESPONDENT_CIC, DELETE);
        grants.putAll(ST_CIC_CASEWORKER, DELETE);
        grants.putAll(ST_CIC_SENIOR_CASEWORKER, DELETE);
        grants.putAll(ST_CIC_HEARING_CENTRE_ADMIN, DELETE);
        grants.putAll(ST_CIC_SENIOR_JUDGE, DELETE);
        grants.putAll(ST_CIC_JUDGE, DELETE);
        grants.putAll(ST_CIC_RESPONDENT, DELETE);
        grants.putAll(AC_CASE_FLAGS_ADMIN, DELETE);
        grants.putAll(CITIZEN, DELETE);
        grants.putAll(ST_CIC_WA_CONFIG_USER, DELETE);


        return grants;
    }
}
