package uk.gov.hmcts.sptribs.ciccase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCICCaseDataConfigBuilder;

@ExtendWith(MockitoExtension.class)
public class CriminalInjuriesCompensationTest {

    @InjectMocks
    private CriminalInjuriesCompensation criminalInjuriesCompensation;

    @Test
    void shouldAddSystemUpdateUserAccessToDraftStateWhenEnvironmentIsAat() {
        //Given
        final ConfigBuilderImpl<CriminalInjuriesCompensationData, State, UserRole> configBuilder = createCICCaseDataConfigBuilder();

        //When
        criminalInjuriesCompensation.configure(configBuilder);

        //Then
        assertThat(configBuilder.build().getCaseType()).isEqualTo(CcdCaseType.CIC.name());

    }

}
