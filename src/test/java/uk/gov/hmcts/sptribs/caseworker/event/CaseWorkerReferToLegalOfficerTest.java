package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.ReferToJudge;
import uk.gov.hmcts.sptribs.caseworker.model.ReferToLegalOfficer;
import uk.gov.hmcts.sptribs.caseworker.model.ReferralReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
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
    void shouldAddPublishToCamundaWhenWAIsEnabled() {

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseWorkerReferToLegalOfficer.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REFER_TO_LEGAL_OFFICER);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::isPublishToCamunda)
                .contains(true);

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
        assertThat(response1.getData().getReferToLegalOfficer().getReferralDate()).isEqualTo(LocalDate.now());
        assertThat(response2).isNotNull();
        assertThat(response2.getConfirmationHeader()).contains("Referral completed");
    }

    @ParameterizedTest
    @EnumSource(ReferralReason.class)
    void shouldSetReferralTypeForWA(ReferralReason referralReason) {
        final CaseDetails<CaseData, State> updatedCaseDetails = getCaseDetails();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final ReferToLegalOfficer referToLegalOfficer = ReferToLegalOfficer.builder()
                .referralReason(referralReason).build();
        updatedCaseDetails.getData().setReferToLegalOfficer(referToLegalOfficer);

        final AboutToStartOrSubmitResponse<CaseData, State> response1 =
                caseWorkerReferToLegalOfficer.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response1).isNotNull();
        assertThat(response1.getData().getCicCase().getReferralTypeForWA()).isEqualTo(referralReason.getLabel());
    }

    @ParameterizedTest
    @EnumSource(ReferralReason.class)
    void shouldNotSetReferralTypeForWAWhenWAIsNotEnabled(ReferralReason referralReason) {
        final CaseDetails<CaseData, State> updatedCaseDetails = getCaseDetails();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final ReferToJudge referToJudge = ReferToJudge.builder()
                .referralReason(referralReason).build();
        updatedCaseDetails.getData().setReferToJudge(referToJudge);

        final AboutToStartOrSubmitResponse<CaseData, State> response1 =
                caseWorkerReferToLegalOfficer.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response1).isNotNull();
        assertThat(response1.getData().getCicCase().getReferralTypeForWA()).isNull();
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
