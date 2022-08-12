package uk.gov.hmcts.divorce.ciccase.model.access;

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
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.ORGANISATION_CASE_ACCESS_ADMINISTRATOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.SYSTEMUPDATE;

class OrganisationPolicyAccessTest {

    @Test
    void shouldGrantOrganisationPolicyAccess() {

        final SetMultimap<HasRole, Permission> grants = new OrganisationPolicyAccess().getGrants();

        assertThat(grants)
            .hasSize(16)
            .contains(
                entry(ORGANISATION_CASE_ACCESS_ADMINISTRATOR, C),
                entry(ORGANISATION_CASE_ACCESS_ADMINISTRATOR, R),
                entry(ORGANISATION_CASE_ACCESS_ADMINISTRATOR, U),
                entry(CITIZEN, C),
                entry(CITIZEN, R),
                entry(CITIZEN, U),
                entry(SOLICITOR, C),
                entry(SOLICITOR, R),
                entry(SOLICITOR, U),
                entry(SOLICITOR, D),
                entry(SUPER_USER, R),
                entry(CASE_WORKER, R),
                entry(LEGAL_ADVISOR, R),
                entry(SYSTEMUPDATE, C),
                entry(SYSTEMUPDATE, R),
                entry(SYSTEMUPDATE, U)
            );
    }
}
