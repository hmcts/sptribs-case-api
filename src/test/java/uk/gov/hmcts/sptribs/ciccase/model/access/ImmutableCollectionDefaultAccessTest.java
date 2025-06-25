package uk.gov.hmcts.sptribs.ciccase.model.access;

import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static org.assertj.core.data.MapEntry.entry;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.D;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;

class ImmutableCollectionDefaultAccessTest {

    @Test
    void shouldGrantReadOnlyAccess() {
        final SetMultimap<HasRole, Permission> grants = new ImmutableCollectionDefaultAccess().getGrants();
        assertThat(grants)
            .hasSize(7)
            .contains(
                entry(CREATOR, R),
                entry(ST_CIC_CASEWORKER, R),
                entry(ST_CIC_SENIOR_CASEWORKER, R),
                entry(ST_CIC_HEARING_CENTRE_ADMIN, R),
                entry(ST_CIC_HEARING_CENTRE_TEAM_LEADER, R),
                entry(ST_CIC_SENIOR_JUDGE, R),
                entry(SYSTEM_UPDATE, R)
            );
    }
}