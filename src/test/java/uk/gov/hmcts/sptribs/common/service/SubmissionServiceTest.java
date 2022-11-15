package uk.gov.hmcts.sptribs.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.service.task.SendSubmissionNotifications;
import uk.gov.hmcts.sptribs.common.service.task.SetApplicantOfflineStatus;
import uk.gov.hmcts.sptribs.common.service.task.SetDateSubmitted;
import uk.gov.hmcts.sptribs.common.service.task.SetHyphenatedCaseRef;
import uk.gov.hmcts.sptribs.common.service.task.SetStateAfterSubmission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SetHyphenatedCaseRef setHyphenatedCaseRef;

    @Mock
    private SetStateAfterSubmission setStateAfterSubmission;

    @Mock
    private SetDateSubmitted setDateSubmitted;

    @Mock
    private SetApplicantOfflineStatus setApplicantOfflineStatus;

    @Mock
    private SendSubmissionNotifications sendSubmissionNotifications;

    @InjectMocks
    private SubmissionService submissionService;

    @Test
    void shouldProcessSubmissionCaseTasks() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();

        when(setHyphenatedCaseRef.apply(caseDetails)).thenReturn(caseDetails);
        when(setStateAfterSubmission.apply(caseDetails)).thenReturn(caseDetails);
        when(setDateSubmitted.apply(caseDetails)).thenReturn(caseDetails);
        when(setApplicantOfflineStatus.apply(caseDetails)).thenReturn(caseDetails);
        when(sendSubmissionNotifications.apply(caseDetails)).thenReturn(expectedCaseDetails);

        //When
        final CaseDetails<CaseData, State> result = submissionService.submitApplication(caseDetails);

        //Then
        assertThat(result).isSameAs(expectedCaseDetails);

        verify(setHyphenatedCaseRef).apply(caseDetails);
        verify(setStateAfterSubmission).apply(caseDetails);
        verify(setDateSubmitted).apply(caseDetails);
        verify(setApplicantOfflineStatus).apply(caseDetails);
        verify(sendSubmissionNotifications).apply(caseDetails);
    }
}
