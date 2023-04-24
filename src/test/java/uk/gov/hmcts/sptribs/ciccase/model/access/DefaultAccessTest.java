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
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;

class DefaultAccessTest {

    @Test
    void shouldGrantDefaultAccess() {
        //When
        final SetMultimap<HasRole, Permission> grants = new DefaultAccess().getGrants();
        //Then
        assertThat(grants)
            .hasSize(18)
            .contains(
                entry(CREATOR, C),
                entry(CREATOR, R),
                entry(CREATOR, U),
                entry(ST_CIC_CASEWORKER, C),
                entry(ST_CIC_CASEWORKER, R),
                entry(ST_CIC_CASEWORKER, U),
                entry(ST_CIC_SENIOR_CASEWORKER, C),
                entry(ST_CIC_SENIOR_CASEWORKER, R),
                entry(ST_CIC_SENIOR_CASEWORKER, U),
                entry(ST_CIC_HEARING_CENTRE_ADMIN, C),
                entry(ST_CIC_HEARING_CENTRE_ADMIN, R),
                entry(ST_CIC_HEARING_CENTRE_ADMIN, U),
                entry(ST_CIC_HEARING_CENTRE_TEAM_LEADER, C),
                entry(ST_CIC_HEARING_CENTRE_TEAM_LEADER, R),
                entry(ST_CIC_HEARING_CENTRE_TEAM_LEADER, U),
                entry(ST_CIC_SENIOR_JUDGE, C),
                entry(ST_CIC_SENIOR_JUDGE, R),
                entry(ST_CIC_SENIOR_JUDGE, U)
            );
    }
}
