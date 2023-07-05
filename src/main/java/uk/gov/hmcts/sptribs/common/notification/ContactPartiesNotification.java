package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
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
public class ContactPartiesNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    @Autowired
    private NotificationHelper notificationHelper;

    private static final int DOC_ATTACH_LIMIT = 10;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsSubject = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);
        templateVarsSubject.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsSubject.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            // Send Email
            Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);

            NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getEmail(),
                templateVarsSubject,
                uploadedDocuments,
                TemplateName.CONTACT_PARTIES_EMAIL);
            cicCase.setSubjectLetterNotifyList(notificationResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVarsSubject);
            //SEND POST
            NotificationResponse notificationResponse = sendLetterNotification(templateVarsSubject, TemplateName.CONTACT_PARTIES_POST);
            cicCase.setSubjectLetterNotifyList(notificationResponse);
        }
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsApplicant = notificationHelper.getApplicantCommonVars(caseNumber, cicCase);
        templateVarsApplicant.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsApplicant.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        if (caseData.getCicCase().getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            // Send Email
            Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getApplicantEmailAddress(),
                templateVarsApplicant,
                uploadedDocuments,
                TemplateName.CONTACT_PARTIES_EMAIL);
            cicCase.setAppNotificationResponse(notificationResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVarsApplicant);
            NotificationResponse notificationResponse = sendLetterNotification(templateVarsApplicant,
                TemplateName.CONTACT_PARTIES_POST);
            cicCase.setAppNotificationResponse(notificationResponse);
        }
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsRepresentative.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            // Send Email
            Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getRepresentativeEmailAddress(),
                templateVarsRepresentative,
                uploadedDocuments,
                TemplateName.CONTACT_PARTIES_EMAIL);
            cicCase.setRepNotificationResponse(notificationResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVarsRepresentative);
            NotificationResponse notificationResponse = sendLetterNotification(templateVarsRepresentative,
                TemplateName.CONTACT_PARTIES_POST);
            cicCase.setRepNotificationResponse(notificationResponse);
        }
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRespondent = notificationHelper.getRespondentCommonVars(caseNumber, cicCase);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsRespondent.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        // Send Email
        if (!ObjectUtils.isEmpty(caseData.getContactPartiesDocuments().getDocumentList())) {

            Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(cicCase.getRespondentEmail(),
                templateVarsRespondent,
                uploadedDocuments,
                TemplateName.CONTACT_PARTIES_EMAIL);
            cicCase.setResNotificationResponse(notificationResponse);
        } else {
            NotificationResponse notificationResponse = sendEmailNotification(templateVarsRespondent,
                cicCase.getRespondentEmail(), TemplateName.CONTACT_PARTIES_EMAIL);
            cicCase.setResNotificationResponse(notificationResponse);
        }
    }

    @Override
    public void sendToTribunal(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsTribunal = notificationHelper.getTribunalCommonVars(caseNumber, cicCase);
        templateVarsTribunal.put(CommonConstants.CIC_CASE_TRIBUNAL_NAME, caseData.getCicCase().getTribunalName());
        templateVarsTribunal.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        // Send Email
        if (!ObjectUtils.isEmpty(caseData.getContactPartiesDocuments().getDocumentList())) {

            Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(cicCase.getTribunalEmail(),
                templateVarsTribunal,
                uploadedDocuments,
                TemplateName.CONTACT_PARTIES_EMAIL);
            cicCase.setTribunalNotificationResponse(notificationResponse);
        } else {
            NotificationResponse notificationResponse = sendEmailNotification(templateVarsTribunal,
                cicCase.getTribunalEmail(), TemplateName.CONTACT_PARTIES_EMAIL);
            cicCase.setTribunalNotificationResponse(notificationResponse);
        }
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars,
                                                       String toEmail,
                                                       TemplateName emailTemplateName) {
        NotificationRequest request = notificationHelper.buildEmailNotificationRequest(toEmail, templateVars, emailTemplateName);
        return notificationService.sendEmail(request);
    }

    private NotificationResponse sendEmailNotificationWithAttachment(
        String toEmail, final Map<String, Object> templateVars,
        Map<String, String> uploadedDocuments,
        TemplateName emailTemplateName) {
        NotificationRequest request = notificationHelper.buildEmailNotificationRequest(toEmail,
            true,
            uploadedDocuments,
            templateVars,
            emailTemplateName);
        return notificationService.sendEmail(request);
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter, TemplateName emailTemplateName) {
        NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(templateVarsLetter, emailTemplateName);
        return notificationService.sendLetter(letterRequest);
    }

    private Map<String, String> getUploadedDocuments(CaseData caseData) {
        ContactPartiesDocuments contactPartiesDocuments = caseData.getContactPartiesDocuments();
        return notificationHelper.buildDocumentList(contactPartiesDocuments.getDocumentList(), DOC_ATTACH_LIMIT);
    }


}
