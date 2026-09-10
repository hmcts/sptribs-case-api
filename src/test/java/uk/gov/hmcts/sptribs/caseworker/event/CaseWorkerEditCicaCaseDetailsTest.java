package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_EDIT_CICA_CASE_DETAILS;

@ExtendWith(MockitoExtension.class)
class CaseWorkerEditCicaCaseDetailsTest {

    @InjectMocks
    private CaseWorkerEditCicaCaseDetails caseWorkerEditCicaCaseDetails;


    @Test
    void shouldAddConfigurationToConfigBuilder() {

        //Given
        final ConfigBuilderImpl<CriminalInjuriesCompensationData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseWorkerEditCicaCaseDetails.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_EDIT_CICA_CASE_DETAILS);
    }

    @Test
    void shouldSuccessfullySave() {

        //Given
        final CaseDetails<CriminalInjuriesCompensationData, State> updatedCaseDetails = CaseDetails.<CriminalInjuriesCompensationData,
            State>builder()
            .id(12345L)
            .data(CriminalInjuriesCompensationData.builder().build())
            .state(State.CaseManagement)
            .build();
        final CaseDetails<CriminalInjuriesCompensationData, State> beforeDetails = CaseDetails.<CriminalInjuriesCompensationData,
            State>builder()
            .id(12345L)
            .data(CriminalInjuriesCompensationData.builder().build())
            .state(State.CaseManagement)
            .build();


        //When
        SubmittedCallbackResponse draftCreatedResponse = caseWorkerEditCicaCaseDetails.submitted(updatedCaseDetails, beforeDetails);
        //Then
        assertThat(draftCreatedResponse).isNotNull();

    }


}

