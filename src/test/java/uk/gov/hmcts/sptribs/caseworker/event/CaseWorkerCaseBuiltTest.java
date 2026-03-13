package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;
import uk.gov.hmcts.sptribs.taskmanagement.TaskManagementService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.issueCaseToRespondent;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.vetNewCaseDocuments;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CASE_BUILT;

@ExtendWith(MockitoExtension.class)
class CaseWorkerCaseBuiltTest {

    @InjectMocks
    private CaseworkerCaseBuilt caseworkerCaseBuilt;

    @Mock
    private TaskManagementService taskManagementService;

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerCaseBuilt.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CASE_BUILT);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.containsKey(ST_CIC_WA_CONFIG_USER))
                .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.get(ST_CIC_WA_CONFIG_USER))
                .contains(Permissions.CREATE_READ_UPDATE);
    }

    @Test
    void shouldSuccessfullyBuiltCase() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();

        final SubmittedCallbackResponse stayedResponse = caseworkerCaseBuilt.submitted(details, details);

        assertThat(stayedResponse.getConfirmationHeader()).contains("# Case built successful");
    }

    @Test
    void shouldEnqueueTasksInAboutToSubmit() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(CaseData.builder().build());
        details.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerCaseBuilt.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(CaseManagement);
        verify(taskManagementService).enqueueCompletionTasks(List.of(vetNewCaseDocuments), TEST_CASE_ID);
        verify(taskManagementService).enqueueInitiationTasks(List.of(issueCaseToRespondent), details.getData(), TEST_CASE_ID);
    }
}
