package uk.gov.hmcts.sptribs.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.service.HoldingPeriodService;

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.sptribs.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.sptribs.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.sptribs.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SetSubmissionAndDueDateTest {

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @Mock
    private Clock clock;

    @InjectMocks
    private SetSubmissionAndDueDate setSubmissionAndDueDate;

    @Test
    void shouldNotSetDueDateAndDateAosSubmittedIfStateHasNotChanged() {
        //Given
        setMockClock(clock);

        final LocalDate dueDate = getExpectedLocalDate();

        final CaseData caseData = caseData();
        caseData.setDueDate(dueDate);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(AwaitingConditionalOrder);
        caseDetails.setData(caseData);

        //When
        final CaseDetails<CaseData, State> result = setSubmissionAndDueDate.apply(caseDetails);

        //Then
        assertThat(result.getData().getAcknowledgementOfService().getDateAosSubmitted()).isEqualTo(getExpectedLocalDateTime());
        assertThat(result.getData().getDueDate()).isEqualTo(dueDate);
    }
}
