package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.ReferToJudge;
import uk.gov.hmcts.sptribs.caseworker.model.ReferralReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_REFER_TO_JUDGE;

@ExtendWith(MockitoExtension.class)
public class CaseWorkerReferToJudgeTest {

    @InjectMocks
    private CaseWorkerReferToJudge caseWorkerReferToJudge;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseWorkerReferToJudge.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REFER_TO_JUDGE);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::isPublishToCamunda)
                .contains(false);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.containsKey(ST_CIC_WA_CONFIG_USER))
                .contains(false);
    }

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {
        ReflectionTestUtils.setField(caseWorkerReferToJudge, "isWorkAllocationEnabled", true);

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseWorkerReferToJudge.configure(configBuilder);

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

    @ParameterizedTest
    @EnumSource(ReferralReason.class)
    void shouldSetReferralTypeForWA(ReferralReason referralReason) {
        ReflectionTestUtils.setField(caseWorkerReferToJudge, "isWorkAllocationEnabled", true);
        final CaseDetails<CaseData, State> updatedCaseDetails = getCaseDetails();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final ReferToJudge referToJudge = ReferToJudge.builder()
                .referralReason(referralReason).build();
        updatedCaseDetails.getData().setReferToJudge(referToJudge);

        final AboutToStartOrSubmitResponse<CaseData, State> response1 =
                caseWorkerReferToJudge.aboutToSubmit(updatedCaseDetails, beforeDetails);

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
                caseWorkerReferToJudge.aboutToSubmit(updatedCaseDetails, beforeDetails);

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
