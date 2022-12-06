package uk.gov.hmcts.sptribs.common.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC;
import uk.gov.hmcts.sptribs.common.AddSystemUpdateRole;

import java.util.List;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.CITIZEN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.SYSTEMUPDATE;

@ExtendWith(MockitoExtension.class)
public class AddSystemUpdateRoleTest {

    @InjectMocks
    private AddSystemUpdateRole addSystemUpdateRole;

    @Test
    public void shouldAddSystemUpdateRoleWhenEnvironmentIsAat() throws Exception {
        //When
        List<UserRoleCIC> actualRoles =
            withEnvironmentVariable("ENVIRONMENT", "aat")
                .execute(() -> addSystemUpdateRole.addIfConfiguredForEnvironment(List.of(CITIZEN_CIC))
                );

        //Then
        assertThat(actualRoles).containsExactlyInAnyOrder(CITIZEN_CIC, SYSTEMUPDATE);
    }

    @Test
    public void shouldReturnTrueWhenEnvironmentIsAat() throws Exception {
        //When
        boolean isEnvironmentAat =
            withEnvironmentVariable("ENVIRONMENT", "aat")
                .execute(() -> addSystemUpdateRole.isEnvironmentAat()
                );

        //Then
        assertThat(isEnvironmentAat).isTrue();
    }
}
