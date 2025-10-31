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
public class CancelHearingNotification implements PartiesNotification {

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    @Autowired
    public CancelHearingNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();

        final Map<String, Object> subjectTemplateVars = notificationHelper.getSubjectCommonVars(caseNumber, caseData);
        notificationHelper.addHearingPostponedTemplateVars(cicCase, subjectTemplateVars);

        final NotificationResponse subjectNotificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            subjectNotificationResponse = sendEmailNotification(cicCase.getEmail(), subjectTemplateVars, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), subjectTemplateVars);
            subjectNotificationResponse = sendLetterNotification(subjectTemplateVars, caseNumber);
        }

        cicCase.setSubjectNotifyList(subjectNotificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();

        final Map<String, Object> reprTemplateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);
        notificationHelper.addHearingPostponedTemplateVars(cicCase, reprTemplateVars);

        final NotificationResponse representativeNotificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            representativeNotificationResponse = sendEmailNotification(cicCase.getRepresentativeEmailAddress(),
                reprTemplateVars, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), reprTemplateVars);
            representativeNotificationResponse = sendLetterNotification(reprTemplateVars, caseNumber);
        }

        cicCase.setRepNotificationResponse(representativeNotificationResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();

        final Map<String, Object> respondentTemplateVars =
            notificationHelper.getRespondentCommonVars(caseNumber, caseData);
        notificationHelper.addHearingPostponedTemplateVars(cicCase, respondentTemplateVars);

        final NotificationResponse respondentNotificationResponse =
            sendEmailNotification(cicCase.getRespondentEmail(), respondentTemplateVars, caseNumber);

        cicCase.setResNotificationResponse(respondentNotificationResponse);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();

        final Map<String, Object> applicantCommonVars = notificationHelper.getApplicantCommonVars(caseNumber, caseData);
        notificationHelper.addHearingPostponedTemplateVars(cicCase, applicantCommonVars);

        final NotificationResponse applicantNotificationResponse;
        if (cicCase.getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            applicantNotificationResponse = sendEmailNotification(cicCase.getApplicantEmailAddress(), applicantCommonVars, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), applicantCommonVars);
            applicantNotificationResponse = sendLetterNotification(applicantCommonVars, caseNumber);
        }

        cicCase.setAppNotificationResponse(applicantNotificationResponse);
    }

    private NotificationResponse sendEmailNotification(final String destinationAddress,
                                                       final Map<String, Object> templateVars,
                                                       String caseReferenceNumber) {
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            templateVars,
            TemplateName.HEARING_CANCELLED_EMAIL);
        return notificationService.sendEmail(request, caseReferenceNumber);
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter, String caseReferenceNumber) {
        final NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.HEARING_CANCELLED_POST);
        return notificationService.sendLetter(letterRequest, caseReferenceNumber);
    }
}
