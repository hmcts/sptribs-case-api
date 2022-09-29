package uk.gov.hmcts.sptribs.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.caseworker.service.task.GenerateGeneralLetter;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.buildCaseDataWithGeneralLetter;

@ExtendWith(MockitoExtension.class)
public class GeneralLetterServiceTest {
    @Mock
    private GenerateGeneralLetter generateGeneralLetter;

    @InjectMocks
    private GeneralLetterService service;

    @Test
    public void testProcessGeneralLetter() {
        //Given
        var caseData = buildCaseDataWithGeneralLetter(APPLICANT);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1L);

        when(generateGeneralLetter.apply(caseDetails)).thenReturn(caseDetails);

        //Wehn
        final CaseDetails<CaseData, State> response = service.processGeneralLetter(caseDetails);

        //Then
        var expectedCaseData = buildCaseDataWithGeneralLetter(APPLICANT);

        assertThat(response.getData()).isEqualTo(expectedCaseData);

        verify(generateGeneralLetter).apply(caseDetails);
    }
}
