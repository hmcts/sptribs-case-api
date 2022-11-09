package uk.gov.hmcts.sptribs.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTaskRunner;
import uk.gov.hmcts.sptribs.common.service.task.SendSubmissionNotifications;
import uk.gov.hmcts.sptribs.common.service.task.SetApplicantOfflineStatus;
import uk.gov.hmcts.sptribs.common.service.task.SetDateSubmitted;
import uk.gov.hmcts.sptribs.common.service.task.SetHyphenatedCaseRef;
import uk.gov.hmcts.sptribs.common.service.task.SetStateAfterSubmission;

@Service
public class SubmissionService {

    @Autowired
    private SetHyphenatedCaseRef setHyphenatedCaseRef;

    @Autowired
    private SetStateAfterSubmission setStateAfterSubmission;

    @Autowired
    private SetDateSubmitted setDateSubmitted;

    @Autowired
    private SetApplicantOfflineStatus setApplicantOfflineStatus;

    @Autowired
    private SendSubmissionNotifications sendSubmissionNotifications;

    public CaseDetails<CaseData, State> submitApplication(final CaseDetails<CaseData, State> caseDetails) {

        return CaseTaskRunner.caseTasks(
            setHyphenatedCaseRef,
            setStateAfterSubmission,
            setDateSubmitted,
            setApplicantOfflineStatus,
            sendSubmissionNotifications
        ).run(caseDetails);
    }
}
