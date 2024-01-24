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
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;

class CaseworkerAndSuperUserAccessTest {

    @Test
    void shouldGrantCaseworkerAndSuperUserAccess() {
        //When
        final SetMultimap<HasRole, Permission> grants = new CaseworkerAndSuperUserAccess().getGrants();
        //Then
        assertThat(grants)
            .hasSize(8)
            .contains(
                entry(SUPER_USER, C),
                entry(SUPER_USER, R),
                entry(SUPER_USER, U),
                entry(SUPER_USER, D),
                entry(SYSTEM_UPDATE, C),
                entry(SYSTEM_UPDATE, R),
                entry(SYSTEM_UPDATE, U),
                entry(SYSTEM_UPDATE, D)
            );
    }
}
