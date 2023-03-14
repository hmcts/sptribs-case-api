package uk.gov.hmcts.sptribs.citizen.event;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN_CIC;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class CicCreateCaseEventTest {

    @InjectMocks
    private CicCreateCaseEvent cicCreateCaseEvent;

    @Test
    void shouldAddConfigurationToConfigBuilderAndSetPermissionOnlyForCitizenRole() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        cicCreateCaseEvent.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains("citizen-cic-create-dss-application");

        SetMultimap<UserRole, Permission> expectedRolesAndPermissions =
            ImmutableSetMultimap.<UserRole, Permission>builder()
                .put(CITIZEN_CIC, C)
                .put(CITIZEN_CIC, R)
                .put(CITIZEN_CIC, U)
                .build();

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getGrants)
            .containsExactly(expectedRolesAndPermissions);
    }

    @Test
    void shouldChangeCaseStateWhenAboutToSubmit() {
        // Given
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        // When
        AboutToStartOrSubmitResponse<CaseData, State> response = cicCreateCaseEvent.aboutToSubmit(
            details,
            beforeDetails
        );

        // Then
        assertThat(response.getState()).isEqualTo(State.Submitted);
    }
}
