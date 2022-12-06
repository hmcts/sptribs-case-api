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
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.CITIZEN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.SYSTEMUPDATE;

class Applicant2AccessTest {

    @Test
    void shouldGrantApplicant2Access() {
        //When
        final SetMultimap<HasRole, Permission> grants = new Applicant2Access().getGrants();
        //Then
        assertThat(grants)
            .hasSize(6)
            .contains(
                entry(SYSTEMUPDATE, R),
                entry(COURT_ADMIN_CIC, R),
                entry(CITIZEN_CIC, R),
                entry(CREATOR, R),
                entry(SYSTEMUPDATE, C),
                entry(SYSTEMUPDATE, R),
                entry(SYSTEMUPDATE, U)
            );
    }
}
