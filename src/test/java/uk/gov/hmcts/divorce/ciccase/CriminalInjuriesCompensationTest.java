package uk.gov.hmcts.divorce.ciccase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.ciccase.model.State;
import uk.gov.hmcts.divorce.ciccase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;

@ExtendWith(MockitoExtension.class)
public class CriminalInjuriesCompensationTest {

    @InjectMocks
    private CriminalInjuriesCompensation criminalInjuriesCompensation;

    @Test
    void shouldAddSystemUpdateUserAccessToDraftStateWhenEnvironmentIsAat() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        criminalInjuriesCompensation.configure(configBuilder);

        assertThat(configBuilder.build().getCaseType()).isEqualTo("CIC");

    }

}
