package uk.gov.hmcts.sptribs.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;

@ExtendWith(MockitoExtension.class)
public class SetStateAfterSubmissionTest {

    @InjectMocks
    private SetStateAfterSubmission setStateAfterSubmission;

    @Test
    void shouldNotThrowNPEWhenCaseDetailsIsNull() {
        assertDoesNotThrow(() -> setStateAfterSubmission.apply(null));
    }

    @Test
    void shouldChangeCaseStateToSubmitted() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(1234567890123456L);

        //When
        final CaseDetails<CaseData, State> result = setStateAfterSubmission.apply(caseDetails);

        //Then
        assertThat(result.getState()).isEqualTo(Submitted);
    }

}
