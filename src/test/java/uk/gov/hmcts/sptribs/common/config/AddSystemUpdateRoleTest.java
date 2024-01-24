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
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;

@ExtendWith(MockitoExtension.class)
public class AddSystemUpdateRoleTest {

    @InjectMocks
    private AddSystemUpdateRole addSystemUpdateRole;

    @Test
    public void shouldAddSystemUpdateRoleWhenEnvironmentIsAat() throws Exception {
        //When
        List<UserRole> actualRoles =
            withEnvironmentVariable("ENVIRONMENT", "aat")
                .execute(() -> addSystemUpdateRole.addIfConfiguredForEnvironment(List.of(CITIZEN))
                );

        //Then
        assertThat(actualRoles).containsExactlyInAnyOrder(CITIZEN, SYSTEM_UPDATE);
    }

    @Test
    public void shouldAddSystemUpdateRoleWhenEnvironmentIsNotAat() throws Exception {
        //When
        List<UserRole> actualRoles =
            withEnvironmentVariable("ENVIRONMENT", "demo")
                .execute(() -> addSystemUpdateRole.addIfConfiguredForEnvironment(List.of(CITIZEN))
                );

        //Then
        assertThat(actualRoles).containsExactly(CITIZEN);
    }

    @Test
    public void shouldAddSystemUpdateRoleWhenEnvironmentIsAatInDifferentCase() throws Exception {
        //When
        List<UserRole> actualRoles =
            withEnvironmentVariable("ENVIRONMENT", "Aat")
                .execute(() -> addSystemUpdateRole.addIfConfiguredForEnvironment(List.of(CREATOR, CASEWORKER))
                );

        //Then
        assertThat(actualRoles).containsExactlyInAnyOrder(CREATOR, CASEWORKER, SYSTEM_UPDATE);
    }

}
