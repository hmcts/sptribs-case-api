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
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.LEGAL_ADVISOR;

class LegalAdvisorAccessTest {

    @Test
    void shouldGrantAccessToLegalAdvisor() {
        //When
        final SetMultimap<HasRole, Permission> grants = new LegalAdvisorAccess().getGrants();
        //Then
        assertThat(grants)
            .hasSize(3)
            .contains(
                entry(LEGAL_ADVISOR, C),
                entry(LEGAL_ADVISOR, R),
                entry(LEGAL_ADVISOR, U)
            );
    }
}
