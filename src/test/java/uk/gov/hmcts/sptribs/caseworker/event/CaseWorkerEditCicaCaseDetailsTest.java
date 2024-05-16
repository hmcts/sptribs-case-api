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
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

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
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

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
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();


        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseWorkerEditCicaCaseDetails.aboutToSubmit(updatedCaseDetails, beforeDetails);
        assertThat(response).isNotNull();


        SubmittedCallbackResponse draftCreatedResponse = caseWorkerEditCicaCaseDetails.submitted(updatedCaseDetails, beforeDetails);
        //Then
        assertThat(draftCreatedResponse).isNotNull();

    }


}

