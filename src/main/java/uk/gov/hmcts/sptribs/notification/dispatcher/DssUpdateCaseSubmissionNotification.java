package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_EMAIL_VALUE;
import static uk.gov.hmcts.sptribs.notification.TemplateName.UPDATE_RECEIVED;
import static uk.gov.hmcts.sptribs.notification.TemplateName.UPDATE_RECEIVED_CIC;

@Component
@Slf4j
public class DssUpdateCaseSubmissionNotification implements PartiesNotification {

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    @Autowired
    public DssUpdateCaseSubmissionNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, caseData);
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            cicCase.getEmail(),
            templateVars,
                UPDATE_RECEIVED);

        notificationService.sendEmail(request, caseNumber);
    }

    @Override
    public void sendToTribunal(final CaseData caseData, final String caseNumber) {
        final Map<String, Object> templateVars = notificationHelper.getTribunalCommonVars(caseNumber, caseData);
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            TRIBUNAL_EMAIL_VALUE,
            templateVars,
                UPDATE_RECEIVED_CIC);

        notificationService.sendEmail(request, caseNumber);
    }
}
