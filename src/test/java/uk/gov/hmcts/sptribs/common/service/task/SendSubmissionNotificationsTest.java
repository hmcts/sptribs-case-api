package uk.gov.hmcts.sptribs.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith({MockitoExtension.class})
class SendSubmissionNotificationsTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private SendSubmissionNotifications sendSubmissionNotifications;

    @Test
    void shouldLogOnceWhenSendingNotificationForSubmittedState() {
        //Given
        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingHearing);
        caseDetails.setId(1L);

        //When
        final CaseDetails<CaseData, State> result = sendSubmissionNotifications.apply(caseDetails);

        //Then
        assertThat(result.getState()).isEqualTo(AwaitingHearing);
        verify(logger).info("Sending outstanding action notification if awaiting documents for case : {}", 1L);
        verifyNoMoreInteractions(logger);
    }

    @Test
    void shouldLogTwiceWhenSendingNotificationForSubmittedState() {
        //Given
        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(1L);

        //When
        final CaseDetails<CaseData, State> result = sendSubmissionNotifications.apply(caseDetails);

        //Then
        assertThat(result.getState()).isEqualTo(Submitted);
        verify(logger).info("Sending application submitted notifications for case : {}", 1L);
        verify(logger).info("Sending outstanding action notification if awaiting documents for case : {}", 1L);
        verifyNoMoreInteractions(logger);
    }
}
