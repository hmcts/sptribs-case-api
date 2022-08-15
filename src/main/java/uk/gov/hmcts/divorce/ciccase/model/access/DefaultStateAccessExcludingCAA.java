package uk.gov.hmcts.divorce.ciccase.model.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static uk.gov.hmcts.divorce.ciccase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.ciccase.model.access.Permissions.READ;

public class DefaultStateAccessExcludingCAA implements HasAccessControl {
    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(CREATOR, READ);
        grants.putAll(APPLICANT_1_SOLICITOR, READ);
        return grants;
    }
}
