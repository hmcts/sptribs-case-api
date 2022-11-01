package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseReinstate;
import uk.gov.hmcts.sptribs.caseworker.model.ReinstateReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.event.ReinstateCase.CASEWORKER_REINSTATE_CASE;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class ReinstateCaseTest {

    @InjectMocks
    private ReinstateCase reinstateCase;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        reinstateCase.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REINSTATE_CASE);
    }

    @Test
    public void shouldSuccessfullyReinstateTheCase() {
        //Given
        final CaseData caseData = caseData();
        final CaseReinstate caseReinstate = new CaseReinstate();
        caseReinstate.setReinstateReason(ReinstateReason.CASE_HAD_BEEN_CLOSED_IN_ERROR);
        caseReinstate.setAdditionalDetail("some detail");
        caseData.setCaseReinstate(caseReinstate);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            reinstateCase.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData().getCaseReinstate()).isNotNull();
        CaseReinstate responseCaseReinstate = response.getData().getCaseReinstate();
        Assertions.assertEquals(responseCaseReinstate.getReinstateReason(), ReinstateReason.CASE_HAD_BEEN_CLOSED_IN_ERROR);
        assertThat(responseCaseReinstate.getAdditionalDetail()).isNotNull();

    }

}
