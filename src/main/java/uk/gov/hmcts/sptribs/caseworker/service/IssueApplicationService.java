package uk.gov.hmcts.sptribs.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.caseworker.service.task.GenerateApplicant1NoticeOfProceeding;
import uk.gov.hmcts.sptribs.caseworker.service.task.GenerateApplicant2NoticeOfProceedings;
import uk.gov.hmcts.sptribs.caseworker.service.task.GenerateD10Form;
import uk.gov.hmcts.sptribs.caseworker.service.task.GenerateDivorceApplication;
import uk.gov.hmcts.sptribs.caseworker.service.task.SendApplicationIssueNotifications;
import uk.gov.hmcts.sptribs.caseworker.service.task.SetDueDateAfterIssue;
import uk.gov.hmcts.sptribs.caseworker.service.task.SetIssueDate;
import uk.gov.hmcts.sptribs.caseworker.service.task.SetPostIssueState;
import uk.gov.hmcts.sptribs.caseworker.service.task.SetServiceType;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.task.DivorceApplicationRemover;

import static uk.gov.hmcts.sptribs.ciccase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class IssueApplicationService {

    @Autowired
    private SetPostIssueState setPostIssueState;

    @Autowired
    private DivorceApplicationRemover divorceApplicationRemover;

    @Autowired
    private GenerateDivorceApplication generateDivorceApplication;

    @Autowired
    private GenerateApplicant2NoticeOfProceedings generateApplicant2NoticeOfProceedings;

    @Autowired
    private GenerateApplicant1NoticeOfProceeding generateApplicant1NoticeOfProceeding;

    @Autowired
    private SendApplicationIssueNotifications sendApplicationIssueNotifications;

    @Autowired
    private SetDueDateAfterIssue setDueDateAfterIssue;

    @Autowired
    private GenerateD10Form generateD10Form;

    @Autowired
    private SetServiceType setServiceType;

    @Autowired
    private SetIssueDate setIssueDate;

    public CaseDetails<CaseData, State> issueApplication(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            setServiceType,
            setIssueDate,
            setPostIssueState,
            setDueDateAfterIssue,
            generateApplicant1NoticeOfProceeding,
            generateApplicant2NoticeOfProceedings,
            divorceApplicationRemover,
            generateDivorceApplication,
            generateD10Form,
            sendApplicationIssueNotifications
        ).run(caseDetails);
    }
}
