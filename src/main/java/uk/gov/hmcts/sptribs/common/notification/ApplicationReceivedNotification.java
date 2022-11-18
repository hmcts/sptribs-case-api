package uk.gov.hmcts.sptribs.common.notification;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.notification.EmailTemplateName;
import uk.gov.hmcts.sptribs.notification.NotificationService;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Collections;

public class ApplicationReceivedNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    @Override
    public void sendToSubject(final CaseData caseData, final Long caseId) {
        NotificationRequest request = NotificationRequest.builder()
            .destinationAddress("address")
            .template(EmailTemplateName.APPLICATION_SUBMITTED)
            .templateVars(Collections.emptyMap())
            .build();
        notificationService.sendEmail(request);


    }

    public void sendToApplicant(final CaseData caseData, final Long caseId) {
        //No operation
    }

    public void sendToRepresentative(final CaseData caseData, final Long caseId) {
        //No operation
    }

    public void sendToRespondent(final CaseData caseData, final Long caseId) {
        //No operation
    }
}
