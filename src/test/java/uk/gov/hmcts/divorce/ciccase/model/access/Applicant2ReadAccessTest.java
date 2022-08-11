package uk.gov.hmcts.divorce.ciccase.model.access;

import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static org.assertj.core.data.MapEntry.entry;
import static org.assertj.guava.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.SYSTEMUPDATE;

class Applicant2ReadAccessTest {

    @Test
    void shouldGrantApplicant2Access() {

        final SetMultimap<HasRole, Permission> grants = new Applicant2ReadAccess().getGrants();

        assertThat(grants)
            .hasSize(7)
            .contains(
                entry(SYSTEMUPDATE, R),
                entry(CASE_WORKER, R),
                entry(LEGAL_ADVISOR, R),
                entry(CITIZEN, R),
                entry(CREATOR, R),
                entry(SYSTEMUPDATE, C),
                entry(SYSTEMUPDATE, R),
                entry(SYSTEMUPDATE, U)
            );
    }
}
