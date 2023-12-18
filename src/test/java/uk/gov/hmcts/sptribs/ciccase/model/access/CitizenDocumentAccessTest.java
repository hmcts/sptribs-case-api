package uk.gov.hmcts.sptribs.ciccase.model.access;

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
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN_CIC;

public class CitizenDocumentAccessTest {

    @Test
    void shouldGrantCitizenDocumentAccess() {
        //When
        final SetMultimap<HasRole, Permission> grants = new CitizenDocumentAccess().getGrants();
        //Then
        assertThat(grants)
            .hasSize(4)
            .contains(
                entry(CITIZEN_CIC, C),
                entry(CITIZEN_CIC, R),
                entry(CITIZEN_CIC, U),
                entry(CITIZEN_CIC, D)
            );
    }
}