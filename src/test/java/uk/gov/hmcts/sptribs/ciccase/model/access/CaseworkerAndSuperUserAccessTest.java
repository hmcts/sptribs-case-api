package uk.gov.hmcts.sptribs.ciccase.model.access;

import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static org.assertj.core.data.MapEntry.entry;
import static org.assertj.guava.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.D;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.SUPER_USER_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.SYSTEMUPDATE;

class CaseworkerAndSuperUserAccessTest {

    @Test
    void shouldGrantCaseworkerAndSuperUserAccess() {
        //When
        final SetMultimap<HasRole, Permission> grants = new CaseworkerAndSuperUserAccess().getGrants();
        //Then
        assertThat(grants)
            .hasSize(12)
            .contains(
                entry(SOLICITOR, R),
                entry(SUPER_USER_CIC, C),
                entry(SUPER_USER_CIC, R),
                entry(SUPER_USER_CIC, U),
                entry(SUPER_USER_CIC, D),
                entry(COURT_ADMIN_CIC, C),
                entry(COURT_ADMIN_CIC, R),
                entry(COURT_ADMIN_CIC, U),
                entry(SYSTEMUPDATE, C),
                entry(SYSTEMUPDATE, R),
                entry(SYSTEMUPDATE, U),
                entry(SYSTEMUPDATE, D)
            );
    }
}
