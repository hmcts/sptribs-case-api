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
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_RESPONDENT;

class CaseworkerAccessWithCAATest {

    @Test
    void shouldGrantCaseworkerAccess() {
        //When
        final SetMultimap<HasRole, Permission> grants = new CaseworkerWithCAAAccess().getGrants();
        //Then
        assertThat(grants)
            .hasSize(6)
            .contains(
                entry(CIC_JUDGE, C),
                entry(CIC_JUDGE, R),
                entry(CIC_JUDGE, U),
                entry(CIC_RESPONDENT, C),
                entry(CIC_RESPONDENT, R),
                entry(CIC_RESPONDENT, U)
            );
    }
}
