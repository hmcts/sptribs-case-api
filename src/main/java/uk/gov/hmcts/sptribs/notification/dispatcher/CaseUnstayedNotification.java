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
public class CaseUnstayedNotification implements PartiesNotification {

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    @Autowired
    public CaseUnstayedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        final Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, caseData);

        final NotificationResponse notificationResponse;
        final CicCase cicCase = caseData.getCicCase();
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            notificationResponse = sendEmailNotification(cicCase.getEmail(), templateVars, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars, caseNumber);
        }

        cicCase.setSubjectNotifyList(notificationResponse);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final Map<String, Object> templateVars = notificationHelper.getApplicantCommonVars(caseNumber, caseData);

        final NotificationResponse notificationResponse;
        final CicCase cicCase = caseData.getCicCase();
        if (cicCase.getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            notificationResponse = sendEmailNotification(cicCase.getApplicantEmailAddress(), templateVars, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars, caseNumber);
        }

        cicCase.setAppNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final Map<String, Object> templateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);

        final NotificationResponse notificationResponse;
        final CicCase cicCase = caseData.getCicCase();
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            notificationResponse = sendEmailNotification(cicCase.getRepresentativeEmailAddress(), templateVars, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars, caseNumber);
        }

        cicCase.setRepNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotification(final String destinationAddress,
                                                       final Map<String, Object> templateVars,
                                                       String caseReferenceNumber) {
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            templateVars,
            TemplateName.CASE_UNSTAYED_EMAIL);
        return notificationService.sendEmail(request, caseReferenceNumber);
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter, String caseReferenceNumber) {
        final NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(templateVarsLetter,
            TemplateName.CASE_UNSTAYED_POST);
        return notificationService.sendLetter(letterRequest, caseReferenceNumber);
    }
}
