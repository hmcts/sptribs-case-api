package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
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

    @Autowired
    private NotificationServiceCIC notificationService;

    @Autowired
    private NotificationHelper notificationHelper;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsSubject = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);
        RecordListing recordListing = caseData.getRecordListing();
        setRecordingTemplateVars(templateVarsSubject, recordListing);
        if (cicCase.getContactPreferenceType().isEmail()) {
            // Send Email
            NotificationResponse notificationResponse =  sendEmailNotification(templateVarsSubject,
                cicCase.getEmail(),
                TemplateName.LISTING_CREATED_CITIZEN_EMAIL);
            cicCase.setSubHearingNotificationResponse(notificationResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVarsSubject);
            //SEND POST
            NotificationResponse notificationResponse = sendLetterNotification(templateVarsSubject,
                TemplateName.LISTING_CREATED_CITIZEN_POST);
            cicCase.setSubHearingNotificationResponse(notificationResponse);
        }
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative  = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, cicCase.getRepresentativeFullName());
        RecordListing recordListing = caseData.getRecordListing();
        setRecordingTemplateVars(templateVarsRepresentative, recordListing);

        if (cicCase.getRepresentativeContactDetailsPreference().isEmail()) {
            // Send Email
            NotificationResponse notificationResponse = sendEmailNotification(templateVarsRepresentative,
                cicCase.getRepresentativeEmailAddress(), TemplateName.LISTING_CREATED_CITIZEN_EMAIL);
            cicCase.setRepHearingNotificationResponse(notificationResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVarsRepresentative);
            NotificationResponse notificationResponse = sendLetterNotification(templateVarsRepresentative,
                TemplateName.LISTING_CREATED_CITIZEN_POST);
            cicCase.setRepHearingNotificationResponse(notificationResponse);
        }
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRespondent = notificationHelper.getRespondentCommonVars(caseNumber, cicCase);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_RESPONDENT_NAME, caseData.getCicCase().getRespondantName());
        RecordListing recordListing = caseData.getRecordListing();
        setRecordingTemplateVars(templateVarsRespondent, recordListing);
        // Send Email
        NotificationResponse notificationResponse = sendEmailNotification(templateVarsRespondent,
            cicCase.getRespondantEmail(), TemplateName.LISTING_CREATED_CITIZEN_EMAIL);
        cicCase.setResHearingNotificationResponse(notificationResponse);
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

    private void setRecordingTemplateVars(Map<String, Object> templateVars, RecordListing recordListing) {
        templateVars.put(CommonConstants.CIC_CASE_HEARING_TYPE, recordListing.getHearingType());
        templateVars.put(CommonConstants.CIC_CASE_HEARING_DATE, recordListing.getHearingDate());
        templateVars.put(CommonConstants.CIC_CASE_HEARING_TIME, recordListing.getHearingTime());

        if (null != recordListing.getHearingVenueName()) {
            templateVars.put(CommonConstants.CIC_CASE_HEARING_VENUE, recordListing.getHearingVenueName());
        } else {
            templateVars.put(CommonConstants.CIC_CASE_HEARING_VENUE, CommonConstants.CIC_CASE_RECORD_REMOTE_HEARING);
        }
        templateVars.put(CommonConstants.CIC_CASE_HEARING_INFO, recordListing.getImportantInfoDetails());

        if (null != recordListing.getVideoCallLink()) {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_HEARING_FORMAT_VIDEO, true);
            templateVars.put(CommonConstants.CIC_CASE_RECORD_VIDEO_CALL_LINK, recordListing.getVideoCallLink());
        } else {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_HEARING_FORMAT_VIDEO, false);
            templateVars.put(CommonConstants.CIC_CASE_RECORD_VIDEO_CALL_LINK, " ");
        }
        if (null != recordListing.getConferenceCallNumber()) {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_FORMAT_TEL, true);
            templateVars.put(CommonConstants.CIC_CASE_RECORD_CONF_CALL_NUM, recordListing.getConferenceCallNumber());
        } else {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_FORMAT_TEL, false);
            templateVars.put(CommonConstants.CIC_CASE_RECORD_CONF_CALL_NUM, " ");
        }
        if (null != recordListing.getHearingFormat() && recordListing.getHearingFormat().equals(HearingFormat.FACE_TO_FACE)) {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_HEARING_1FACE_TO_FACE, true);
        } else {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_HEARING_1FACE_TO_FACE, false);
        }
    }
}
