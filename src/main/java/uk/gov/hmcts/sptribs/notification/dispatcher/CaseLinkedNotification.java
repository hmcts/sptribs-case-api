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
public class CaseLinkedNotification implements PartiesNotification {

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;


    @Autowired
    public CaseLinkedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);

        final NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            notificationResponse = sendEmailNotification(cicCase.getEmail(), templateVars);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars);
        }

        cicCase.setSubjectNotifyList(notificationResponse);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> applicantCommonVars = notificationHelper.getApplicantCommonVars(caseNumber, cicCase);

        final NotificationResponse notificationResponse;
        if (cicCase.getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            notificationResponse = sendEmailNotification(cicCase.getApplicantEmailAddress(), applicantCommonVars);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), applicantCommonVars);
            notificationResponse = sendLetterNotification(applicantCommonVars);
        }

        cicCase.setAppNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> representativeCommonVars = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);

        final NotificationResponse notificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            notificationResponse = sendEmailNotification(cicCase.getRepresentativeEmailAddress(), representativeCommonVars);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), representativeCommonVars);
            notificationResponse = sendLetterNotification(representativeCommonVars);
        }

        cicCase.setRepNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotification(final String destinationAddress, final Map<String, Object> templateVars) {
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            templateVars,
            TemplateName.CASE_LINKED_EMAIL);
        return notificationService.sendEmail(request);
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter) {
        final NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(templateVarsLetter,
            TemplateName.CASE_LINKED_POST);
        return notificationService.sendLetter(letterRequest);
    }

}