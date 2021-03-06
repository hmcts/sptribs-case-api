package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.ciccase.model.State;
import uk.gov.hmcts.divorce.ciccase.task.CaseTask;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.ciccase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.ciccase.model.State.Submitted;

@Component
@Slf4j
public class SendSubmissionNotifications implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();
        final Long caseId = caseDetails.getId();
        final State state = caseDetails.getState();

        if (state == Submitted || state == AwaitingHWFDecision && isEmpty(caseData.getApplication().getMissingDocumentTypes())) {
            log.info("Sending application submitted notifications for case : {}", caseId);
            //notificationDispatcher.send(applicationSubmittedNotification, caseData, caseId);
        }

        log.info("Sending outstanding action notification if awaiting documents for case : {}", caseId);
        //notificationDispatcher.send(applicationOutstandingActionNotification, caseData, caseId);

        return caseDetails;
    }
}
