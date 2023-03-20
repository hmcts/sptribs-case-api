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

@ExtendWith(MockitoExtension.class)
public class ReferToJudgeReasonTest {

    @InjectMocks
    private ReferToJudgeReason referToJudgeReason;

    @Test
    void shouldReturnNoErrorIfValidStateForReason() {
        // Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseData.getReferToJudge().setReferralReason(ReferralReason.CORRECTIONS);
        caseDetails.setState(State.CaseClosed);
        caseDetails.setData(caseData);

        // When
        AboutToStartOrSubmitResponse<CaseData, State> response = referToJudgeReason.midEvent(caseDetails, caseDetails);

        // Then
        assertThat(response.getErrors()).hasSize(0);
    }

    @Test
    void shouldReturnErrorIfInvalidStateForReason() {
        // Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseData.getReferToJudge().setReferralReason(ReferralReason.CORRECTIONS);
        caseDetails.setState(State.CaseManagement);
        caseDetails.setData(caseData);

        // When
        AboutToStartOrSubmitResponse<CaseData, State> response = referToJudgeReason.midEvent(caseDetails, caseDetails);

        // Then
        assertThat(response.getErrors()).hasSize(1);
    }
}
