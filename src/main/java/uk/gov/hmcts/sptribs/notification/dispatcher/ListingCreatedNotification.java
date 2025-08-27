package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
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
public class ListingCreatedNotification implements PartiesNotification {

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    @Autowired
    public ListingCreatedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void sendToSubject(final CaseData caseDataSubject, final String caseNumber) {
        final CicCase cicCaseListing = caseDataSubject.getCicCase();
        final Map<String, Object> templateVarsSubject = notificationHelper.getSubjectCommonVars(caseNumber, caseDataSubject);
        final Listing listingSubject = caseDataSubject.getListing();
        notificationHelper.setRecordingTemplateVars(templateVarsSubject, listingSubject);

        final NotificationResponse notificationResponse;
        if (cicCaseListing.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            // Send Email
            notificationResponse = sendEmailNotification(templateVarsSubject,
                cicCaseListing.getEmail());
        } else {
            notificationHelper.addAddressTemplateVars(cicCaseListing.getAddress(), templateVarsSubject);
            //SEND POST
            notificationResponse = sendLetterNotification(templateVarsSubject);
        }

        cicCaseListing.setSubHearingNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Listing listing = caseData.getListing();
        final Map<String, Object> templateVarsRepresentative  = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);

        templateVarsRepresentative.put(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, cicCase.getRepresentativeFullName());
        notificationHelper.setRecordingTemplateVars(templateVarsRepresentative, listing);

        final NotificationResponse notificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            // Send Email
            notificationResponse = sendEmailNotification(templateVarsRepresentative,
                cicCase.getRepresentativeEmailAddress());
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVarsRepresentative);
            notificationResponse = sendLetterNotification(templateVarsRepresentative);
        }

        cicCase.setRepHearingNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRespondent = notificationHelper.getRespondentCommonVars(caseNumber, caseData);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_RESPONDENT_NAME, caseData.getCicCase().getRespondentName());
        final Listing listing = caseData.getListing();
        notificationHelper.setRecordingTemplateVars(templateVarsRespondent, listing);
        // Send Email
        final NotificationResponse notificationResponse = sendEmailNotification(templateVarsRespondent,
            cicCase.getRespondentEmail());
        cicCase.setResHearingNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVars = notificationHelper.getApplicantCommonVars(caseNumber, caseData);
        final Listing listing = caseData.getListing();
        notificationHelper.setRecordingTemplateVars(templateVars, listing);

        final NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            // Send Email
            notificationResponse = sendEmailNotification(templateVars,
                cicCase.getApplicantEmailAddress());
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVars);
            //SEND POST
            notificationResponse = sendLetterNotification(templateVars);
        }

        cicCase.setSubHearingNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars,
                                                       String toEmail) {

        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            toEmail,
            templateVars,
            TemplateName.HEARING_CREATED_EMAIL);
        return notificationService.sendEmail(request);
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter) {

        final NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.LISTING_CREATED_CITIZEN_POST);
        return notificationService.sendLetter(letterRequest);
    }
}
