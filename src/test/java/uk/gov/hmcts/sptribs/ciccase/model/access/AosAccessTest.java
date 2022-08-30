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
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEMUPDATE;

class AosAccessTest {

    @Test
    void shouldGrantAosAccess() {
        //When
        final SetMultimap<HasRole, Permission> grants = new AosAccess().getGrants();
        //Then
        assertThat(grants)
            .hasSize(6)
            .contains(
                entry(SYSTEMUPDATE, C),
                entry(SYSTEMUPDATE, R),
                entry(SYSTEMUPDATE, U),
                entry(CASE_WORKER, R),
                entry(LEGAL_ADVISOR, R),
                entry(CITIZEN, R)
            );
    }
}
