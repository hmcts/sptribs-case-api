package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
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

import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_EMAIL_VALUE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME_VALUE;

@Component
@RequiredArgsConstructor
public class ContactPartiesNotification implements PartiesNotification {

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    @Override
    public String sendToSubject(final CaseData caseData, final String caseNumber, final Map<String, String> uploadedDocuments) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsSubject = notificationHelper.getSubjectCommonVars(caseNumber, caseData);
        templateVarsSubject.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsSubject.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        final NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            // Send Email
            notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getEmail(),
                templateVarsSubject,
                uploadedDocuments,
                TemplateName.CONTACT_PARTIES_EMAIL,
                caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVarsSubject);
            //SEND POST
            notificationResponse = sendLetterNotification(templateVarsSubject, TemplateName.CONTACT_PARTIES_POST, caseNumber);
        }
        cicCase.setSubjectLetterNotifyList(notificationResponse);
        //we have the id in the notification response ......
        return notificationResponse.getId();
    }

    @Override
    public String sendToApplicant(final CaseData caseData, final String caseNumber, final Map<String, String> uploadedDocuments) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsApplicant = notificationHelper.getApplicantCommonVars(caseNumber, caseData);
        templateVarsApplicant.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsApplicant.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        final NotificationResponse notificationResponse;
        if (caseData.getCicCase().getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            // Send Email
            notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getApplicantEmailAddress(),
                templateVarsApplicant,
                uploadedDocuments,
                TemplateName.CONTACT_PARTIES_EMAIL,
                caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVarsApplicant);
            notificationResponse = sendLetterNotification(templateVarsApplicant,
                TemplateName.CONTACT_PARTIES_POST, caseNumber);
        }

        cicCase.setAppNotificationResponse(notificationResponse);
        return notificationResponse.getId();
    }

    @Override
    public String sendToRepresentative(final CaseData caseData, final String caseNumber, final Map<String, String> uploadedDocuments) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsRepresentative.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        final NotificationResponse notificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            // Send Email
            notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getRepresentativeEmailAddress(),
                templateVarsRepresentative,
                uploadedDocuments,
                TemplateName.CONTACT_PARTIES_EMAIL,
                caseNumber);

        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVarsRepresentative);
            notificationResponse = sendLetterNotification(templateVarsRepresentative,
                TemplateName.CONTACT_PARTIES_POST, caseNumber);
        }

        cicCase.setRepNotificationResponse(notificationResponse);
        return notificationResponse.getId();
    }

    @Override
    public String sendToRespondent(final CaseData caseData, final String caseNumber, final Map<String, String> uploadedDocuments) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRespondent = notificationHelper.getRespondentCommonVars(caseNumber, caseData);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsRespondent.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        // Send Email
        final NotificationResponse notificationResponse;
        if (ObjectUtils.isNotEmpty(caseData.getContactPartiesDocuments().getDocumentList())) {

            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getRespondentEmail(),
                templateVarsRespondent,
                uploadedDocuments,
                TemplateName.CONTACT_PARTIES_EMAIL,
                caseNumber);
        } else {
            notificationResponse = sendEmailNotification(templateVarsRespondent,
                cicCase.getRespondentEmail(), TemplateName.CONTACT_PARTIES_EMAIL, caseNumber);
        }

        cicCase.setResNotificationResponse(notificationResponse);
        return notificationResponse.getId();
    }

    @Override
    public String sendToTribunal(final CaseData caseData, final String caseNumber, final Map<String, String> uploadedDocuments) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsTribunal = notificationHelper.getTribunalCommonVars(caseNumber, caseData);
        templateVarsTribunal.put(CommonConstants.CIC_CASE_TRIBUNAL_NAME, TRIBUNAL_NAME_VALUE);
        templateVarsTribunal.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        // Send Email
        final NotificationResponse notificationResponse;
        if (ObjectUtils.isNotEmpty(caseData.getContactPartiesDocuments().getDocumentList())) {

            notificationResponse = sendEmailNotificationWithAttachment(TRIBUNAL_EMAIL_VALUE,
                templateVarsTribunal,
                uploadedDocuments,
                TemplateName.CONTACT_PARTIES_EMAIL, caseNumber);
        } else {
            notificationResponse = sendEmailNotification(templateVarsTribunal,
                TRIBUNAL_EMAIL_VALUE, TemplateName.CONTACT_PARTIES_EMAIL, caseNumber);
        }

        cicCase.setTribunalNotificationResponse(notificationResponse);
        return notificationResponse.getId();
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars,
                                                       String toEmail,
                                                       TemplateName emailTemplateName,
                                                       String caseReferenceNumber) {
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(toEmail, templateVars, emailTemplateName);
        return notificationService.sendEmail(request, caseReferenceNumber);
    }

    private NotificationResponse sendEmailNotificationWithAttachment(
        String toEmail, final Map<String, Object> templateVars,
        Map<String, String> uploadedDocuments,
        TemplateName emailTemplateName,
        String caseReferenceNumber) {

        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(toEmail,
            true,
            uploadedDocuments,
            templateVars,
            emailTemplateName);

        return notificationService.sendEmail(request, caseReferenceNumber);
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter,
                                                        TemplateName emailTemplateName,
                                                        String caseReferenceNumber) {
        final NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(templateVarsLetter, emailTemplateName);
        return notificationService.sendLetter(letterRequest, caseReferenceNumber);
    }
}
