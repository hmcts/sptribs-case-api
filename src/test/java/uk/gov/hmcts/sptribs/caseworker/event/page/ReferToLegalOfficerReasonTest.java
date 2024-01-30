package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.ReferralReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.NEW_CASE;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.INCOMPATIBLE_REFERRAL_REASON;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;

@ExtendWith(MockitoExtension.class)
class ReferToLegalOfficerReasonTest {

    @InjectMocks
    private ReferToLegalOfficerReason referToLegalOfficerReason;

    @Test
    void shouldReturnNoErrorIfReasonIsInRightState() {

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseData.getReferToLegalOfficer().setReferralReason(ReferralReason.POSTPONEMENT_REQUEST);
        caseDetails.setState(AwaitingHearing);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = referToLegalOfficerReason.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(0);

    }

    @Test
    void shouldReturnErrorIfReasonIsInWrongState() {

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseData.getReferToLegalOfficer().setReferralReason(ReferralReason.POSTPONEMENT_REQUEST);
        caseDetails.setState(CaseClosed);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = referToLegalOfficerReason.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(INCOMPATIBLE_REFERRAL_REASON);

    }

    @Test
    void shouldReturnNoErrorForValidStateReasonNewCaseCaseManagementState() {
        // Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseData.getReferToLegalOfficer().setReferralReason(NEW_CASE);
        caseDetails.setState(CaseManagement);
        caseDetails.setData(caseData);

        // When
        final AboutToStartOrSubmitResponse<CaseData, State> response = referToLegalOfficerReason.midEvent(caseDetails, caseDetails);

        // Then
        assertThat(response.getErrors()).hasSize(0);
    }

    @Test
    void shouldReturnNoErrorForValidStateReasonNewCaseSubmittedState() {
        // Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseData.getReferToLegalOfficer().setReferralReason(NEW_CASE);
        caseDetails.setState(Submitted);
        caseDetails.setData(caseData);

        // When
        final AboutToStartOrSubmitResponse<CaseData, State> response = referToLegalOfficerReason.midEvent(caseDetails, caseDetails);

        // Then
        assertThat(response.getErrors()).hasSize(0);
    }

    @Test
    void shouldReturnErrorForInvalidStateReasonNewCase() {
        // Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseData.getReferToLegalOfficer().setReferralReason(NEW_CASE);
        caseDetails.setState(AwaitingHearing);
        caseDetails.setData(caseData);

        // When
        final AboutToStartOrSubmitResponse<CaseData, State> response = referToLegalOfficerReason.midEvent(caseDetails, caseDetails);

        // Then
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).isEqualTo(INCOMPATIBLE_REFERRAL_REASON);
    }
}
