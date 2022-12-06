package uk.gov.hmcts.sptribs.ciccase.model.access;

import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static org.assertj.core.data.MapEntry.entry;
import static org.assertj.guava.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.SYSTEMUPDATE;

class SolicitorAndSystemUpdateAccessTest {

    @Test
    void shouldGrantSolicitorAndSystemUpdateAccess() {
        //When
        final SetMultimap<HasRole, Permission> grants = new SolicitorAndSystemUpdateAccess().getGrants();
        //Then
        assertThat(grants)
            .hasSize(6)
            .contains(
                entry(SOLICITOR, C),
                entry(SOLICITOR, R),
                entry(SOLICITOR, U),
                entry(SYSTEMUPDATE, C),
                entry(SYSTEMUPDATE, R),
                entry(SYSTEMUPDATE, U)
            );
    }
}
