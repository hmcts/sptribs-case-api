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
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ORGANISATION_CASE_ACCESS_ADMINISTRATOR;

class DefaultStateAccessTest {

    @Test
    void shouldGrantCaseAccessAdministratorsSolicitorsAndCitizens() {
        //When
        final SetMultimap<HasRole, Permission> grants = new DefaultStateAccess().getGrants();
        //Then
        assertThat(grants)
            .hasSize(5)
            .contains(
                entry(ORGANISATION_CASE_ACCESS_ADMINISTRATOR, C),
                entry(ORGANISATION_CASE_ACCESS_ADMINISTRATOR, R),
                entry(ORGANISATION_CASE_ACCESS_ADMINISTRATOR, U),
                entry(CREATOR, R),
                entry(APPLICANT_1_SOLICITOR, R)
            );
    }
}
