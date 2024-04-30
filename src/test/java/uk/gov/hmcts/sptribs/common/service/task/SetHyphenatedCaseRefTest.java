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
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SetHyphenatedCaseRefTest {

    @InjectMocks
    private SetHyphenatedCaseRef setHyphenatedCaseRef;

    @Test
    void shouldNotThrowNPEWhenCaseDetailsIsNull() {
        assertDoesNotThrow(() -> setHyphenatedCaseRef.apply(null));
    }

    @Test
    void shouldNotThrowNPEWhenCaseDataIsNull() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(null);

        assertDoesNotThrow(() -> setHyphenatedCaseRef.apply(caseDetails));
    }

    @Test
    void shouldNotThrowNPEWhenCaseIdIsNull() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = caseData();
        caseDetails.setData(caseData);
        caseDetails.setId(null);

        assertDoesNotThrow(() -> setHyphenatedCaseRef.apply(caseDetails));
    }

    @Test
    void shouldFormatCaseIdToHyphenated16DigitNumber() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = caseData();
        caseDetails.setData(caseData);
        caseDetails.setId(1234567890123456L);

        //When
        final CaseDetails<CaseData, State> result = setHyphenatedCaseRef.apply(caseDetails);

        //Then
        assertThat(result.getData().getHyphenatedCaseRef()).isEqualTo("1234-5678-9012-3456");
    }

}
