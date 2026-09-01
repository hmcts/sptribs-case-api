package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.ReferralReason;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.NEW_CASE;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.INCOMPATIBLE_REFERRAL_REASON;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;

@ExtendWith(MockitoExtension.class)
public class ReferToJudgeReasonTest {

    @InjectMocks
    private ReferToJudgeReason referToJudgeReason;

    @Test
    void shouldReturnNoErrorIfValidStateForReason() {
        final CaseDetails<CriminalInjuriesCompensationData, State> caseDetails = new CaseDetails<>();
        final CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder().build();
        caseData.getReferToJudge().setReferralReason(ReferralReason.CORRECTIONS);
        caseDetails.setState(CaseClosed);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData, State> response = referToJudgeReason.midEvent(caseDetails,
            caseDetails);

        assertThat(response.getErrors()).hasSize(0);
    }

    @Test
    void shouldReturnErrorIfInvalidStateForReason() {
        final CaseDetails<CriminalInjuriesCompensationData, State> caseDetails = new CaseDetails<>();
        final CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder().build();
        caseData.getReferToJudge().setReferralReason(ReferralReason.CORRECTIONS);
        caseDetails.setState(CaseManagement);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData, State> response = referToJudgeReason.midEvent(caseDetails,
            caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(INCOMPATIBLE_REFERRAL_REASON);
    }

    @Test
    void shouldReturnNoErrorForValidStateReasonNewCaseCaseManagementState() {
        // Given
        final CaseDetails<CriminalInjuriesCompensationData, State> caseDetails = new CaseDetails<>();
        final CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder().build();
        caseData.getReferToJudge().setReferralReason(NEW_CASE);
        caseDetails.setState(CaseManagement);
        caseDetails.setData(caseData);

        // When
        final AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData, State> response = referToJudgeReason.midEvent(caseDetails,
            caseDetails);

        // Then
        assertThat(response.getErrors()).hasSize(0);
    }

    @Test
    void shouldReturnNoErrorForValidStateReasonNewCaseSubmittedState() {
        // Given
        final CaseDetails<CriminalInjuriesCompensationData, State> caseDetails = new CaseDetails<>();
        final CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder().build();
        caseData.getReferToJudge().setReferralReason(NEW_CASE);
        caseDetails.setState(Submitted);
        caseDetails.setData(caseData);

        // When
        final AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData, State> response = referToJudgeReason.midEvent(caseDetails,
            caseDetails);

        // Then
        assertThat(response.getErrors()).hasSize(0);
    }

    @Test
    void shouldReturnErrorForInvalidStateReasonNewCase() {
        // Given
        final CaseDetails<CriminalInjuriesCompensationData, State> caseDetails = new CaseDetails<>();
        final CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder().build();
        caseData.getReferToJudge().setReferralReason(NEW_CASE);
        caseDetails.setState(AwaitingHearing);
        caseDetails.setData(caseData);

        // When
        final AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData, State> response = referToJudgeReason.midEvent(caseDetails,
            caseDetails);

        // Then
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).isEqualTo(INCOMPATIBLE_REFERRAL_REASON);
    }
}

