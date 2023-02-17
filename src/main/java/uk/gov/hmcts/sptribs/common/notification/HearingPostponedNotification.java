package uk.gov.hmcts.sptribs.common.notification;

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
public class HearingPostponedNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    @Autowired
    private NotificationHelper notificationHelper;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);
        notificationHelper.addHearingPostponedTemplateVars(cicCase, templateVars);

        NotificationResponse hearingNotifyResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            hearingNotifyResponse = sendEmailNotification(cicCase.getEmail(), templateVars);
            cicCase.setSubjectNotifyList(hearingNotifyResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            hearingNotifyResponse = sendLetterNotification(templateVars);
        }

        cicCase.setSubjectNotifyList(hearingNotifyResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);
        notificationHelper.addHearingPostponedTemplateVars(cicCase, templateVars);

        NotificationResponse hearingNotifyResponse;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            hearingNotifyResponse =
                sendEmailNotification(cicCase.getRepresentativeEmailAddress(), templateVars);
            cicCase.setRepNotificationResponse(hearingNotifyResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            hearingNotifyResponse = sendLetterNotification(templateVars);
        }

        cicCase.setRepNotificationResponse(hearingNotifyResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> respondentTemplateVars = notificationHelper.getRespondentCommonVars(caseNumber, cicCase);
        notificationHelper.addHearingPostponedTemplateVars(cicCase, respondentTemplateVars);

        NotificationResponse hearingNotifyResponse = sendEmailNotification(cicCase.getRespondentEmail(), respondentTemplateVars);
        cicCase.setResNotificationResponse(hearingNotifyResponse);
    }

    private NotificationResponse sendEmailNotification(final String destinationAddress, final Map<String, Object> templateVars) {
        NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            templateVars,
            TemplateName.HEARING_POSTPONED_EMAIL);
        notificationService.setNotificationRequest(request);
        return notificationService.sendEmail();
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter) {
        NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.HEARING_POSTPONED_POST);
        notificationService.setNotificationRequest(letterRequest);
        return notificationService.sendLetter();
    }

}
