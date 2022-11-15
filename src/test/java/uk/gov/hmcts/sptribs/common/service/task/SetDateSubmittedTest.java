package uk.gov.hmcts.sptribs.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.sptribs.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SetDateSubmittedTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private SetDateSubmitted setDateSubmitted;

    @Test
    void shouldSetDateSubmitted() {
        //Given
        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1L);
        caseDetails.setState(State.Submitted);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        setMockClock(clock);

        //When
        final CaseDetails<CaseData, State> response = setDateSubmitted.apply(caseDetails);

        //Then
        assertThat(response.getData().getApplication().getDateSubmitted()).isEqualTo(getExpectedLocalDateTime());
    }

    @Test
    void shouldNotSetDateSubmitted() {
        // Given
        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1L);
        caseDetails.setState(State.AwaitingHearing);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        final CaseDetails<CaseData, State> response = setDateSubmitted.apply(caseDetails);

        // Then
        assertThat(response.getData().getApplication().getDateSubmitted()).isNotEqualTo(getExpectedLocalDateTime());
    }
}
