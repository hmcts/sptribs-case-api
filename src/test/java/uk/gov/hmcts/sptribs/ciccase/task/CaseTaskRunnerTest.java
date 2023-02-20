package uk.gov.hmcts.sptribs.ciccase.task;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.task.CaseTaskRunner.caseTasks;

class CaseTaskRunnerTest {

    @Test
    void shouldReturnReducedFunctionWrappedInCaseTaskRunnerAndBeAppliedToCaseDetails() {
        //Given
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        //When
        final CaseDetails<CaseData, State> result = caseTasks(
            cd -> {
                cd.getData().setCicCase(CicCase.builder().applicantFullName("full name").build());
                return cd;
            },
            new TestCaseTask()
        ).run(caseDetails);

        //Then
        assertThat(result.getData().getCicCase())
            .extracting(CicCase::getApplicantFullName, CicCase::getApplicantPhoneNumber)
            .contains("full name", "01234567890");
    }

    public static class TestCaseTask implements CaseTask {

        @Override
        public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {
            caseDetails.getData().getCicCase().setApplicantPhoneNumber("01234567890");
            return caseDetails;
        }
    }
}
