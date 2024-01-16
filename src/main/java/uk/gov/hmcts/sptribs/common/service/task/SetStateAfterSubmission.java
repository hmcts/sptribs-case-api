package uk.gov.hmcts.sptribs.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;

import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;

@Component
@Slf4j
public class SetStateAfterSubmission implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        if (caseDetails != null) {
            caseDetails.setState(Submitted);
            log.debug("State set to {}, CaseID {}", caseDetails.getState(), caseDetails.getId());
        }

        return caseDetails;
    }
}
