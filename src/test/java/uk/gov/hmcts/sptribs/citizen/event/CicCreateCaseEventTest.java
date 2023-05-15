package uk.gov.hmcts.sptribs.citizen.event;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.AddSystemUpdateRole;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.constants.CommonConstants;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN_CIC;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource("classpath:application.yaml")
@ActiveProfiles("test")
class CicCreateCaseEventTest {

    @InjectMocks
    private CicCreateCaseEvent cicCreateCaseEvent;

    @Mock
    private AddSystemUpdateRole addSystemUpdateRole;

    @Mock
    private AppsConfig appsConfig;

    @Mock
    private AppsConfig.AppsDetails cicAppDetail;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        cicAppDetail = new AppsConfig.AppsDetails();
        cicAppDetail.setCaseType(ST_CIC_CASE_TYPE);
        cicAppDetail.setJurisdiction(CommonConstants.ST_CIC_JURISDICTION);
        cicAppDetail.setCaseTypeOfApplication(List.of(CASE_DATA_CIC_ID));

        AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setCreateEvent("citizen-cic-create-dss-application");

        cicAppDetail.setEventIds(eventsConfig);

    }

    @Test
    void shouldAddConfigurationToConfigBuilderAndSetPermissionOnlyForCitizenRole() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        when(addSystemUpdateRole.addIfConfiguredForEnvironment(anyList()))
            .thenReturn(List.of(CITIZEN_CIC));

        when(appsConfig.getApps()).thenReturn(Arrays.asList(cicAppDetail));

        cicCreateCaseEvent.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(AppsUtil.getExactAppsDetailsByCaseType(appsConfig, ST_CIC_CASE_TYPE).getEventIds()
                          .getCreateEvent());

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
    void shouldSetPermissionForCitizenAndCaseWorkerRoleWhenEnvironmentIsAat() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        when(addSystemUpdateRole.addIfConfiguredForEnvironment(anyList()))
            .thenReturn(List.of(CITIZEN_CIC));

        when(appsConfig.getApps()).thenReturn(Arrays.asList(cicAppDetail));

        cicCreateCaseEvent.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains("citizen-cic-create-dss-application");

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getDescription)
            .contains("Apply for edge case (cic)");

        SetMultimap<UserRole, Permission> expectedRolesAndPermissions =
            ImmutableSetMultimap.<UserRole, Permission>builder()
                .put(CITIZEN_CIC, C)
                .put(CITIZEN_CIC, R)
                .put(CITIZEN_CIC, U)
                .build();

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getGrants)
            .containsExactlyInAnyOrder(expectedRolesAndPermissions);
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
