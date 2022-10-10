package uk.gov.hmcts.sptribs.ciccase.task;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.Applicant;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
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
                cd.getData().setApplicant1(Applicant.builder().firstName("first name").build());
                return cd;
            },
            new TestCaseTask()
        ).run(caseDetails);

        //Then
        assertThat(result.getData().getApplicant1())
            .extracting(Applicant::getFirstName, Applicant::getLastName)
            .contains("first name", "last name");
    }

    public static class TestCaseTask implements CaseTask {

        @Override
        public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {
            caseDetails.getData().getApplicant1().setLastName("last name");
            return caseDetails;
        }
    }
}
