package uk.gov.hmcts.sptribs.common.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;
import uk.gov.hmcts.sptribs.common.notification.SoleApplicationDisputedNotification;
import uk.gov.hmcts.sptribs.common.notification.SoleApplicationNotDisputedNotification;
import uk.gov.hmcts.sptribs.notification.NotificationDispatcher;

@Component
public class SendAosNotifications implements CaseTask {

    @Autowired
    private SoleApplicationNotDisputedNotification soleApplicationNotDisputedNotification;

    @Autowired
    private SoleApplicationDisputedNotification soleApplicationDisputedNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> details) {
        final var data = details.getData();

        if (data.getAcknowledgementOfService().isDisputed()) {
            notificationDispatcher.send(soleApplicationDisputedNotification, data, details.getId());
        } else {
            notificationDispatcher.send(soleApplicationNotDisputedNotification, data, details.getId());
        }

        return details;
    }
}

