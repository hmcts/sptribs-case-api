package uk.gov.hmcts.sptribs.ciccase.model.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

//TODO: Remove this class as LEGAL-ADVISOR is reformatted
public class LegalAdvisorAccess implements HasAccessControl {
    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        //SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        return HashMultimap.create();
    }
}
