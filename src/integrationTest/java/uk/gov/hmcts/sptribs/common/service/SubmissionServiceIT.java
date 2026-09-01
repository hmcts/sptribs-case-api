package uk.gov.hmcts.sptribs.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_HYPHENATED;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class SubmissionServiceIT {

    @Autowired
    SubmissionService submissionService;

    @Test
    void shouldSubmitApplicationAndRunCaseTask() {
        final CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder().build();

        final CaseDetails<CriminalInjuriesCompensationData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final CaseDetails<CriminalInjuriesCompensationData, State> caseDetailsOutput = submissionService.submitApplication(caseDetails);

        assertThat(caseDetailsOutput.getData().getHyphenatedCaseRef()).isEqualTo(TEST_CASE_ID_HYPHENATED);
        assertThat(caseDetailsOutput.getState()).isEqualTo(Submitted);
    }
}
