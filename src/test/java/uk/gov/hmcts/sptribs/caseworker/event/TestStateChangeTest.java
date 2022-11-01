package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseReinstate;
import uk.gov.hmcts.sptribs.caseworker.model.ReinstateReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.event.TestStateChange.TEST_CHANGE_STATE;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class TestStateChangeTest {

    @InjectMocks
    private TestStateChange testStateChange;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        testStateChange.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(TEST_CHANGE_STATE);
    }

    @Test
    void shouldSuccessfullySetState() {
        //Given
        final CaseData caseData = caseData();
        CaseReinstate caseReinstate = new CaseReinstate();
        caseReinstate.setReinstateReason(ReinstateReason.CASE_HAD_BEEN_CLOSED_IN_ERROR);
        caseReinstate.setAdditionalDetail("some detail");
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseData.setCaseReinstate(caseReinstate);
        caseData.getCicCase().setTestState(State.CaseClosed);
        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            testStateChange.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse changedResponse = testStateChange.changed(updatedCaseDetails, beforeDetails);
        //Then
        assertThat(response.getData()).isNotNull();
        assertThat(changedResponse).isNotNull();
    }

    @Test
    void shouldSuccessfullySetStateWithDays() {
        //Given
        final CaseData caseData = caseData();
        CaseReinstate caseReinstate = new CaseReinstate();
        caseReinstate.setReinstateReason(ReinstateReason.CASE_HAD_BEEN_CLOSED_IN_ERROR);
        caseReinstate.setAdditionalDetail("some detail");
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseData.setCaseReinstate(caseReinstate);
        caseData.getCicCase().setTestState(State.CaseClosed);
        caseData.getCicCase().setDays("3");

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            testStateChange.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse changedResponse = testStateChange.changed(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData()).isNotNull();
        assertThat(changedResponse).isNotNull();
        assertThat(response.getData().getClosureDate()).isBefore(LocalDate.now().minusDays(2));
    }

}
