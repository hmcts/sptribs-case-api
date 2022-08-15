package uk.gov.hmcts.sptribs.solicitor.service.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;

@Component
public class SetApplicantGender implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> details) {
        var data = details.getData();
        data.deriveAndPopulateApplicantGenderDetails();
        return details;
    }
}
