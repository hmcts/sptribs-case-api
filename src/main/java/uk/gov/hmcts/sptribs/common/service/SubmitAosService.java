package uk.gov.hmcts.sptribs.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.caseworker.service.task.SendAosResponseLetterPackToApplicant;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.service.task.AddRespondentAnswersLink;
import uk.gov.hmcts.sptribs.common.service.task.GenerateAosResponseLetterDocument;
import uk.gov.hmcts.sptribs.common.service.task.GenerateRespondentAnswersDoc;
import uk.gov.hmcts.sptribs.common.service.task.SendAosNotifications;
import uk.gov.hmcts.sptribs.common.service.task.SetSubmissionAndDueDate;
import uk.gov.hmcts.sptribs.common.service.task.SetSubmitAosState;

import static uk.gov.hmcts.sptribs.ciccase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class SubmitAosService {

    @Autowired
    private SetSubmitAosState setSubmitAosState;

    @Autowired
    private SetSubmissionAndDueDate setSubmissionAndDueDate;

    @Autowired
    private GenerateRespondentAnswersDoc generateRespondentAnswersDoc;

    @Autowired
    private AddRespondentAnswersLink addRespondentAnswersLink;

    @Autowired
    private SendAosNotifications sendAosNotifications;

    @Autowired
    private GenerateAosResponseLetterDocument generateAosResponseLetterDocument;

    @Autowired
    private SendAosResponseLetterPackToApplicant sendAosResponseLetterPackToApplicant;

    public CaseDetails<CaseData, State> submitAos(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            setSubmitAosState,
            setSubmissionAndDueDate,
            generateRespondentAnswersDoc,
            addRespondentAnswersLink,
            sendAosNotifications,
            generateAosResponseLetterDocument,
            sendAosResponseLetterPackToApplicant
        ).run(caseDetails);
    }

    public CaseDetails<CaseData, State> submitOfflineAos(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            setSubmitAosState,
            setSubmissionAndDueDate,
            addRespondentAnswersLink,
            sendAosNotifications,
            generateAosResponseLetterDocument,
            sendAosResponseLetterPackToApplicant
        ).run(caseDetails);
    }
}
