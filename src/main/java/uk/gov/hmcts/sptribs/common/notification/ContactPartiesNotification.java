package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

import java.util.Map;

import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_POST;


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
            Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);

            NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getEmail(),
                templateVarsSubject,
                uploadedDocuments,
                CONTACT_PARTIES_EMAIL);
            cicCase.setSubjectNotifyList(notificationResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVarsSubject);
            //SEND POST
            NotificationResponse notificationResponse = sendLetterNotification(templateVarsSubject, CONTACT_PARTIES_POST);
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
            Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);

            NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getApplicantEmailAddress(),
                templateVarsApplicant,
                uploadedDocuments,
                CONTACT_PARTIES_EMAIL);
            cicCase.setAppNotificationResponse(notificationResponse);
        } else {
            //SEND POST
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVarsApplicant);
            NotificationResponse notificationResponse = sendLetterNotification(templateVarsApplicant,
                CONTACT_PARTIES_POST);
            cicCase.setAppLetterNotificationResponse(notificationResponse);
        }
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsRepresentative.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);

            NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getRepresentativeEmailAddress(),
                templateVarsRepresentative,
                uploadedDocuments,
                CONTACT_PARTIES_EMAIL);
            cicCase.setRepNotificationResponse(notificationResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVarsRepresentative);
            NotificationResponse notificationResponse = sendLetterNotification(templateVarsRepresentative,
                CONTACT_PARTIES_POST);
            cicCase.setRepLetterNotificationResponse(notificationResponse);
        }
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRespondent = notificationHelper.getRespondentCommonVars(caseNumber, cicCase);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsRespondent.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
        NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(
            cicCase.getRespondentEmail(),
            templateVarsRespondent,
            uploadedDocuments,
            CONTACT_PARTIES_EMAIL
        );
        cicCase.setResNotificationResponse(notificationResponse);
    }


    @Override
    public void sendToTribunal(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsTribunal = notificationHelper.getTribunalCommonVars(caseNumber, cicCase);
        templateVarsTribunal.put(CommonConstants.CIC_CASE_TRIBUNAL_NAME, caseData.getCicCase().getTribunalName());
        templateVarsTribunal.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);

        NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(cicCase.getTribunalEmail(),
            templateVarsTribunal,
            uploadedDocuments,
            TemplateName.CONTACT_PARTIES_EMAIL);
        cicCase.setTribunalNotificationResponse(notificationResponse);
    }


    private NotificationResponse sendEmailNotificationWithAttachment(String toEmail,
                                                                     final Map<String, Object> templateVars,
                                                                     Map<String, String> uploadedDocuments,
                                                                     TemplateName emailTemplateName) {

        return notificationService.sendEmail(
            notificationHelper.buildEmailNotificationRequest(
                toEmail,
                true,
                uploadedDocuments,
                templateVars,
                emailTemplateName
            )
        );
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter, TemplateName emailTemplateName) {
        return notificationService.sendLetter(
            notificationHelper.buildLetterNotificationRequest(templateVarsLetter, emailTemplateName)
        );
    }

    private Map<String, String> getUploadedDocuments(CaseData caseData) {
        ContactPartiesDocuments contactPartiesDocuments = caseData.getContactPartiesDocuments();
        return notificationHelper.buildDocumentList(contactPartiesDocuments.getDocumentList(), DOC_ATTACH_LIMIT);
    }
}
