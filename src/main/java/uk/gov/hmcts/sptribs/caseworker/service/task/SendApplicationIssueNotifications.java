package uk.gov.hmcts.sptribs.caseworker.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;
import uk.gov.hmcts.sptribs.common.notification.ApplicationIssuedNotification;
import uk.gov.hmcts.sptribs.common.notification.ApplicationIssuedOverseasNotification;
import uk.gov.hmcts.sptribs.notification.NotificationDispatcher;

import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingService;

@Component
public class SendApplicationIssueNotifications implements CaseTask {

    @Autowired
    private ApplicationIssuedNotification applicationIssuedNotification;

    @Autowired
    private ApplicationIssuedOverseasNotification applicationIssuedOverseasNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();
        final Long caseId = caseDetails.getId();

        notificationDispatcher.send(applicationIssuedNotification, caseData, caseId);

        if (caseDetails.getState() == AwaitingService
            && caseData.getApplicationType().isSole()
            && (caseData.getApplicant2().isBasedOverseas()
            || caseData.getApplication().isPersonalServiceMethod())) {
            notificationDispatcher.send(applicationIssuedOverseasNotification, caseData, caseId);
        }

        return caseDetails;
    }
}
