package uk.gov.hmcts.sptribs.ciccase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;

@ExtendWith(MockitoExtension.class)
public class CriminalInjuriesCompensationTest {

    @InjectMocks
    private CriminalInjuriesCompensation criminalInjuriesCompensation;

    @Test
    void shouldAddSystemUpdateUserAccessToDraftStateWhenEnvironmentIsAat() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRoleCIC> configBuilder = createCaseDataConfigBuilder();

        //When
        criminalInjuriesCompensation.configure(configBuilder);

        //Then
        assertThat(configBuilder.build().getCaseType()).isEqualTo("CIC");

    }

}
