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
public class HearingPostponedNotification implements PartiesNotification {

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    @Autowired
    public HearingPostponedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, caseData);
        notificationHelper.addHearingPostponedTemplateVars(cicCase, templateVars);

        final NotificationResponse hearingNotifyResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            hearingNotifyResponse = sendEmailNotification(cicCase.getEmail(), templateVars, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            hearingNotifyResponse = sendLetterNotification(templateVars, caseNumber);
        }

        cicCase.setSubjectNotifyList(hearingNotifyResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);
        notificationHelper.addHearingPostponedTemplateVars(cicCase, templateVars);

        final NotificationResponse hearingNotifyResponse;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            hearingNotifyResponse =
                sendEmailNotification(cicCase.getRepresentativeEmailAddress(), templateVars, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            hearingNotifyResponse = sendLetterNotification(templateVars, caseNumber);
        }

        cicCase.setRepNotificationResponse(hearingNotifyResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();

        final Map<String, Object> respondentTemplateVars = notificationHelper.getRespondentCommonVars(caseNumber, caseData);
        notificationHelper.addHearingPostponedTemplateVars(cicCase, respondentTemplateVars);

        final NotificationResponse hearingNotifyResponse = sendEmailNotification(cicCase.getRespondentEmail(),
            respondentTemplateVars, caseNumber);
        cicCase.setResNotificationResponse(hearingNotifyResponse);
    }

    private NotificationResponse sendEmailNotification(final String destinationAddress,
                                                       final Map<String, Object> templateVars, String caseReferenceNumber) {
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            templateVars,
            TemplateName.HEARING_POSTPONED_EMAIL);
        return notificationService.sendEmail(request, caseReferenceNumber);
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter, String caseReferenceNumber) {
        final NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.HEARING_POSTPONED_POST);
        return notificationService.sendLetter(letterRequest, caseReferenceNumber);
    }

}
