package uk.gov.hmcts.sptribs.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AosDrafted;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AosOverdue;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Holding;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SetSubmitAosStateTest {

    @InjectMocks
    private SetSubmitAosState setSubmitAosState;

    @Test
    void shouldSetStateToHoldingIfPreviousStateIsAwaitingAos() {
        //Given
        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(AosDrafted);
        caseDetails.setData(caseData);

        //When
        final CaseDetails<CaseData, State> result = setSubmitAosState.apply(caseDetails);

        //Then
        assertThat(result.getState()).isEqualTo(Holding);
    }

    @Test
    void shouldSetStateToHoldingIfPreviousStateIsAosOverdue() {
        //Given
        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(AosOverdue);
        caseDetails.setData(caseData);

        //When
        final CaseDetails<CaseData, State> result = setSubmitAosState.apply(caseDetails);

        //Then
        assertThat(result.getState()).isEqualTo(Holding);
    }

    @Test
    void shouldNotSetStateToHoldingIfPreviousStateIsNotAwaitingAosOrAosOverdue() {
        //Given
        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(AwaitingConditionalOrder);
        caseDetails.setData(caseData);

        //When
        final CaseDetails<CaseData, State> result = setSubmitAosState.apply(caseDetails);

        //Then
        assertThat(result.getState()).isEqualTo(AwaitingConditionalOrder);
    }
}
