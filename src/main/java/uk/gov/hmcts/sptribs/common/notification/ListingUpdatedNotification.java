package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

@Component
@Slf4j
public class ListingUpdatedNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    @Autowired
    private NotificationHelper notificationHelper;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsSubject = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);
        RecordListing recordListing = caseData.getRecordListing();
        notificationHelper.setRecordingTemplateVars(templateVarsSubject, recordListing);
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            // Send Email
            NotificationResponse notificationResponse = sendEmailNotification(templateVarsSubject,
                cicCase.getEmail(),
                TemplateName.LISTING_UPDATED_CITIZEN_EMAIL);
            cicCase.setSubjectLetterNotifyList(notificationResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVarsSubject);
            //SEND POST
            sendLetterNotification(templateVarsSubject, TemplateName.LISTING_UPDATED_CITIZEN_POST);
        }
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative  = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, cicCase.getRepresentativeFullName());
        RecordListing recordListing = caseData.getRecordListing();
        notificationHelper.setRecordingTemplateVars(templateVarsRepresentative, recordListing);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            // Send Email
            NotificationResponse notificationResponse = sendEmailNotification(templateVarsRepresentative,
                cicCase.getRepresentativeEmailAddress(), TemplateName.LISTING_UPDATED_CITIZEN_EMAIL);
            cicCase.setRepNotificationResponse(notificationResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVarsRepresentative);
            NotificationResponse notificationResponse = sendLetterNotification(templateVarsRepresentative,
                TemplateName.LISTING_UPDATED_CITIZEN_POST);
            cicCase.setRepNotificationResponse(notificationResponse);
        }
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRespondent = notificationHelper.getRespondentCommonVars(caseNumber, cicCase);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_RESPONDENT_NAME, caseData.getCicCase().getRespondentName());
        RecordListing recordListing = caseData.getRecordListing();
        notificationHelper.setRecordingTemplateVars(templateVarsRespondent, recordListing);
        // Send Email
        NotificationResponse notificationResponse = sendEmailNotification(templateVarsRespondent,
            cicCase.getRespondentEmail(), TemplateName.LISTING_UPDATED_CITIZEN_EMAIL);
        cicCase.setResNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars,
                                                       String toEmail,
                                                       TemplateName emailTemplateName) {

        NotificationRequest request = notificationHelper.buildEmailNotificationRequest(toEmail, templateVars, emailTemplateName);
        notificationService.setNotificationRequest(request);
        return notificationService.sendEmail();
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter, TemplateName emailTemplateName) {
        NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(templateVarsLetter, emailTemplateName);
        notificationService.setNotificationRequest(letterRequest);
        return notificationService.sendLetter();
    }
}
