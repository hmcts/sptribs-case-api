package uk.gov.hmcts.sptribs.common.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.AddSystemUpdateRole;

import java.util.List;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEMUPDATE;

@ExtendWith(MockitoExtension.class)
public class AddSystemUpdateRoleTest {

    @InjectMocks
    private AddSystemUpdateRole addSystemUpdateRole;

    @Test
    public void shouldAddSystemUpdateRoleWhenEnvironmentIsAat() throws Exception {
        //When
        List<UserRole> actualRoles =
            withEnvironmentVariable("ENVIRONMENT", "aat")
                .execute(() -> addSystemUpdateRole.addIfConfiguredForEnvironment(List.of(CITIZEN_CIC))
                );

        //Then
        assertThat(actualRoles).containsExactlyInAnyOrder(CITIZEN_CIC, SYSTEMUPDATE);
    }

    @Test
    public void shouldAddSystemUpdateRoleWhenEnvironmentIsNotAat() throws Exception {
        //When
        List<UserRole> actualRoles =
            withEnvironmentVariable("ENVIRONMENT", "demo")
                .execute(() -> addSystemUpdateRole.addIfConfiguredForEnvironment(List.of(CITIZEN_CIC))
                );

        //Then
        assertThat(actualRoles).containsExactly(CITIZEN_CIC);
    }

    @Test
    public void shouldAddSystemUpdateRoleWhenEnvironmentIsAatInDifferentCase() throws Exception {
        //When
        List<UserRole> actualRoles =
            withEnvironmentVariable("ENVIRONMENT", "Aat")
                .execute(() -> addSystemUpdateRole.addIfConfiguredForEnvironment(List.of(CREATOR, SOLICITOR))
                );

        //Then
        assertThat(actualRoles).containsExactlyInAnyOrder(CREATOR, SOLICITOR, SYSTEMUPDATE);
    }

}
