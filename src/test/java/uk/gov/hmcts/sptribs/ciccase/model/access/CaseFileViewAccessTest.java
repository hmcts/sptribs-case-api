package uk.gov.hmcts.sptribs.ciccase.model.access;

import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static org.assertj.core.data.MapEntry.entry;
import static org.assertj.guava.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;

class CaseFileViewAccessTest {
    @Test
    void shouldGrantCaseFileViewAccess() {
        //When
        final SetMultimap<HasRole, Permission> grants = new CaseFileViewAccess().getGrants();
        //Then
        assertThat(grants)
            .hasSize(11)
            .contains(
                entry(CREATOR, R),
                entry(ST_CIC_CASEWORKER, R),
                entry(ST_CIC_SENIOR_CASEWORKER, R),
                entry(ST_CIC_HEARING_CENTRE_ADMIN, R),
                entry(ST_CIC_HEARING_CENTRE_TEAM_LEADER, R),
                entry(ST_CIC_SENIOR_JUDGE, R),
                entry(ST_CIC_WA_CONFIG_USER, R),
                entry(ST_CIC_JUDGE, R),
                entry(ST_CIC_RESPONDENT, R),
                entry(SYSTEM_UPDATE, R),
                entry(SUPER_USER, R)
            );
    }
}
