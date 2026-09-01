package uk.gov.hmcts.sptribs.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTaskRunner;
import uk.gov.hmcts.sptribs.common.service.task.SetHyphenatedCaseRef;
import uk.gov.hmcts.sptribs.common.service.task.SetStateAfterSubmission;

@Service
public class SubmissionService {

    @Autowired
    private SetHyphenatedCaseRef setHyphenatedCaseRef;

    @Autowired
    private SetStateAfterSubmission setStateAfterSubmission;

    /**
     * Run the post-submission tasks, returning the caller's own case-data type.
     *
     * <p>Generic because callers are now typed on their case type's own data class rather
     * than on {@link CaseData}, and a method fixed to the base class would force each of
     * them to cast the result back.
     *
     * <p>The two casts are safe and confined to here. {@link CaseTaskRunner} and the tasks
     * it runs are declared on {@link CaseData}: they only set the hyphenated case reference
     * and the state, both base-class fields, and neither replaces the data object — the
     * same instance goes in and comes out. So the runtime type of the returned details is
     * still {@code T}, and expressing that through {@code CaseTaskRunner}'s own generics
     * would mean making it, {@code CaseTask} and every task generic for no behavioural
     * gain.
     */
    @SuppressWarnings("unchecked")
    public <T extends CaseData> CaseDetails<T, State> submitApplication(final CaseDetails<T, State> caseDetails) {

        final CaseDetails<CaseData, State> details = (CaseDetails<CaseData, State>) caseDetails;

        return (CaseDetails<T, State>) CaseTaskRunner.caseTasks(
            setHyphenatedCaseRef,
            setStateAfterSubmission
        ).run(details);
    }
}
