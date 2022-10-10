package uk.gov.hmcts.sptribs.ciccase.model.access;

import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static org.assertj.core.data.MapEntry.entry;
import static org.assertj.guava.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;

class DefaultStateAccessExcludingCAATest {

    @Test
    void shouldGrantSolicitorsAndCitizens() {
        //When
        final SetMultimap<HasRole, Permission> grants = new DefaultStateAccessExcludingCAA().getGrants();
        //Then
        assertThat(grants)
            .hasSize(1)
            .contains(
                entry(CREATOR, R)
            );
    }
}
