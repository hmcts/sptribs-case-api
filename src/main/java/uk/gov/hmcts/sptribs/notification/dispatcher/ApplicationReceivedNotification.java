package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

@Component
@Slf4j
public class ApplicationReceivedNotification implements PartiesNotification {

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    @Autowired
    public ApplicationReceivedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        final Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, caseData);

        if (caseData.getCicCase().getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            NotificationResponse notificationResponse = sendEmailNotification(caseData.getCicCase().getEmail(), templateVars, caseNumber);
            caseData.getCicCase().setSubjectNotifyList(notificationResponse);
        }
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVars = notificationHelper.getApplicantCommonVars(caseNumber, caseData);

        if (cicCase.getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            NotificationResponse notificationResponse = sendEmailNotification(cicCase.getApplicantEmailAddress(), templateVars, caseNumber);
            cicCase.setAppNotificationResponse(notificationResponse);
        }
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            NotificationResponse notificationResponse = sendEmailNotification(cicCase.getRepresentativeEmailAddress(),
                templateVars, caseNumber);
            cicCase.setRepNotificationResponse(notificationResponse);
        }
    }

    private NotificationResponse sendEmailNotification(final String destinationAddress,
                                                       final Map<String, Object> templateVars,
                                                       String caseReferenceNumber) {
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            templateVars,
            TemplateName.APPLICATION_RECEIVED);
        return notificationService.sendEmail(request, caseReferenceNumber);
    }
}
