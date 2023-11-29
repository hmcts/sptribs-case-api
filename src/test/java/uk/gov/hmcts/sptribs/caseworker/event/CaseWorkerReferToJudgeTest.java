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
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_REFER_TO_JUDGE;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class CaseWorkerReferToJudgeTest {

    @InjectMocks
    private CaseWorkerReferToJudge caseWorkerReferToJudge;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseWorkerReferToJudge.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REFER_TO_JUDGE);
    }

    @Test
    void shouldAlwaysInitiateReferToJudgeWithEmptyObject() {
        //Given
        final CaseDetails<CaseData, State> existingCaseDetails = getCaseDetails();

        //When
        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartResponse =
            caseWorkerReferToJudge.aboutToStart(existingCaseDetails);

        //Then
        assertThat(aboutToStartResponse.getData().getReferToJudge()).isNotNull();
        assertThat(aboutToStartResponse.getData().getReferToJudge().getReferralReason()).isNull();
        assertThat(aboutToStartResponse.getData().getReferToJudge().getReasonForReferral()).isNull();
        assertThat(aboutToStartResponse.getData().getReferToJudge().getAdditionalInformation()).isNull();
        assertThat(aboutToStartResponse.getData().getReferToJudge().getReferralDate()).isNull();
    }

    @Test
    void shouldReferToJudge() {
        //Given
        final CaseDetails<CaseData, State> updatedCaseDetails = getCaseDetails();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response1 =
            caseWorkerReferToJudge.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse response2 =
            caseWorkerReferToJudge.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response1).isNotNull();
        assertThat(response1.getData().getReferToJudge().getReferralDate()).isNotNull();
        assertThat(response2).isNotNull();
        assertThat(response2.getConfirmationHeader()).contains("Referral completed");
    }

    private CaseDetails<CaseData, State> getCaseDetails() {
        CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        return caseDetails;
    }

}
