package uk.gov.hmcts.sptribs.ciccase.model.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

public class CaseworkerWithCAAAccess implements HasAccessControl {

    /*
    * Reusing this class for roll access, but need to see relevance of this in future
    */

    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(ST_CIC_JUDGE, CREATE_READ_UPDATE);
        grants.putAll(ST_CIC_RESPONDENT, CREATE_READ_UPDATE);

        return grants;
    }
}
