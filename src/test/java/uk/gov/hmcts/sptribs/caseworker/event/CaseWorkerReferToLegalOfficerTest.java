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
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_REFER_TO_LEGAL_OFFICER;

@ExtendWith(MockitoExtension.class)
class CaseWorkerReferToLegalOfficerTest {

    @InjectMocks
    private CaseWorkerReferToLegalOfficer caseWorkerReferToLegalOfficer;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseWorkerReferToLegalOfficer.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REFER_TO_LEGAL_OFFICER);
    }

    @Test
    void shouldAlwaysInitiateReferToLegalOfficerWithEmptyObject() {
        //Given
        final CaseDetails<CaseData, State> existingCaseDetails = getCaseDetails();

        //When
        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartResponse =
            caseWorkerReferToLegalOfficer.aboutToStart(existingCaseDetails);

        //Then
        assertThat(aboutToStartResponse.getData().getReferToLegalOfficer()).isNotNull();
        assertThat(aboutToStartResponse.getData().getReferToLegalOfficer().getReferralReason()).isNull();
        assertThat(aboutToStartResponse.getData().getReferToLegalOfficer().getReasonForReferral()).isNull();
        assertThat(aboutToStartResponse.getData().getReferToLegalOfficer().getAdditionalInformation()).isNull();
        assertThat(aboutToStartResponse.getData().getReferToLegalOfficer().getReferralDate()).isNull();
    }

    @Test
    void shouldReferToLegalOfficer() {
        //Given
        final CaseDetails<CaseData, State> updatedCaseDetails = getCaseDetails();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response1 =
            caseWorkerReferToLegalOfficer.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse response2 =
            caseWorkerReferToLegalOfficer.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response1).isNotNull();
        assertThat(response1.getData().getReferToLegalOfficer().getReferralDate()).isNotNull();
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
