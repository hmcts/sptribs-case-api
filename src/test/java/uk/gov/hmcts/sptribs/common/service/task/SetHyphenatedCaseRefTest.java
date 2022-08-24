package uk.gov.hmcts.sptribs.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SetHyphenatedCaseRefTest {

    @InjectMocks
    private SetHyphenatedCaseRef setHyphenatedCaseRef;

    @Test
    void shouldFormatCaseIdToHyphenated16DigitNumber() {

        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1L);

        final CaseDetails<CaseData, State> result = setHyphenatedCaseRef.apply(caseDetails);

        assertThat(result.getData().getHyphenatedCaseRef()).isEqualTo("0000-0000-0000-0001");
    }


}
