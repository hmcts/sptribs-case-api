package uk.gov.hmcts.sptribs.ciccase.model.access;

import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static org.assertj.core.data.MapEntry.entry;
import static org.assertj.guava.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.api.Permission.D;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.AC_CASE_FLAGS_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.DISTRICT_JUDGE_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.GS_PROFILE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.RESPONDENT_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;

class GlobalSearchAccessTest {

    @Test
    void shouldGrantGlobalSearchAccess() {
        final SetMultimap<HasRole, Permission> grants = new GlobalSearchAccess().getGrants();

        assertThat(grants)
            .hasSize(15)
            .contains(
                entry(GS_PROFILE, R),
                entry(SUPER_USER, D),
                entry(SYSTEM_UPDATE, D),
                entry(CREATOR, D),
                entry(DISTRICT_JUDGE_CIC, D),
                entry(RESPONDENT_CIC, D),
                entry(ST_CIC_CASEWORKER, D),
                entry(ST_CIC_SENIOR_CASEWORKER, D),
                entry(ST_CIC_HEARING_CENTRE_ADMIN, D),
                entry(ST_CIC_SENIOR_JUDGE, D),
                entry(ST_CIC_JUDGE, D),
                entry(ST_CIC_RESPONDENT, D),
                entry(AC_CASE_FLAGS_ADMIN, D),
                entry(CITIZEN, D),
                entry(ST_CIC_WA_CONFIG_USER, D)
            );
    }
}
