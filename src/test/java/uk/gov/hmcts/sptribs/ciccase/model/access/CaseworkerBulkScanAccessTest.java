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
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEMUPDATE;

public class CaseworkerBulkScanAccessTest {
    @Test
    void shouldGrantCaseworkerBulkScanAccess() {
        //When
        final SetMultimap<HasRole, Permission> grants = new CaseworkerBulkScanAccess().getGrants();
        //Then
        assertThat(grants)
            .hasSize(8)
            .contains(
                entry(SUPER_USER, R),
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
