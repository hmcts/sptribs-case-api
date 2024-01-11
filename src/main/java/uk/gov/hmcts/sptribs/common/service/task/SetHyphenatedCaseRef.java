package uk.gov.hmcts.sptribs.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;

@Component
@Slf4j
public class SetHyphenatedCaseRef implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        if (caseDetails != null
            && caseDetails.getData() != null
            && caseDetails.getId() != null) {
            caseDetails.getData().setHyphenatedCaseRef(caseDetails.getData().formatCaseRef(caseDetails.getId()));
        }

        return caseDetails;
    }
}
