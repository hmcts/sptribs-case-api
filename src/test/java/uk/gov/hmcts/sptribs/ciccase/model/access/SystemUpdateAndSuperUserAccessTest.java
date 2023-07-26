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
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEMUPDATE;

class SystemUpdateAndSuperUserAccessTest {

    @Test
    void shouldGrantSystemUpdateAndSuperUserAccess() {
        //When
        final SetMultimap<HasRole, Permission> grants = new SystemUpdateAndSuperUserAccess().getGrants();
        //Then
        assertThat(grants)
            .hasSize(7)
            .contains(
                entry(CIC_SUPER_USER, C),
                entry(CIC_SUPER_USER, R),
                entry(CIC_SUPER_USER, U),
                entry(CIC_SUPER_USER, D),
                entry(SYSTEMUPDATE, C),
                entry(SYSTEMUPDATE, R),
                entry(SYSTEMUPDATE, U)
            );
    }
}
