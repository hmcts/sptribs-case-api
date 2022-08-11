package uk.gov.hmcts.divorce.ciccase.model.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.SYSTEMUPDATE;

public class Applicant2Access implements HasAccessControl {
    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(CASE_WORKER, Permissions.READ);
        grants.putAll(LEGAL_ADVISOR, Permissions.READ);
        grants.putAll(CITIZEN, Permissions.READ);
        grants.putAll(CREATOR, Permissions.READ);
        grants.putAll(SYSTEMUPDATE, Permissions.CREATE_READ_UPDATE);
        return grants;
    }
}
