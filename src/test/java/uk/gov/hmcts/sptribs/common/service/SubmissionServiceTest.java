package uk.gov.hmcts.sptribs.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.service.task.SetHyphenatedCaseRef;
import uk.gov.hmcts.sptribs.common.service.task.SetStateAfterSubmission;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SetHyphenatedCaseRef setHyphenatedCaseRef;

    @Mock
    private SetStateAfterSubmission setStateAfterSubmission;

    @InjectMocks
    private SubmissionService submissionService;

    @Test
    void shouldProcessSubmissionCaseTasks() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        when(setHyphenatedCaseRef.apply(caseDetails)).thenReturn(caseDetails);
        when(setStateAfterSubmission.apply(caseDetails)).thenReturn(caseDetails);

        //When
        submissionService.submitApplication(caseDetails);

        //Then
        verify(setHyphenatedCaseRef).apply(caseDetails);
        verify(setStateAfterSubmission).apply(caseDetails);
    }
}
