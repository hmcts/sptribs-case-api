package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
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

import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_EMAIL_VALUE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME_VALUE;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_EMAIL_NEW_CD;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_POST;

@Component
@RequiredArgsConstructor
public class ContactPartiesNotification implements PartiesNotification {

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    @Value("${sptribs-frontend.dashboard-url}")
    private String citizenDashboardUrl;

    @Value("${feature.citizen-dashboard.enabled}")
    private boolean citizenDashboardEnabled;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber, final Map<String, String> uploadedDocuments) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsSubject = notificationHelper.getSubjectCommonVars(caseNumber, caseData);
        templateVarsSubject.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsSubject.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());
        addDashboardLink(templateVarsSubject);
        final NotificationResponse notificationResponse;
        final TemplateName templateName;

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            // Send Email
            notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getEmail(),
                templateVarsSubject,
                uploadedDocuments,
                getTemplateName(),
                caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVarsSubject);
            //SEND POST
            notificationResponse = sendLetterNotification(templateVarsSubject, CONTACT_PARTIES_POST, caseNumber);
        }
        cicCase.setSubjectLetterNotifyList(notificationResponse);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber, final Map<String, String> uploadedDocuments) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsApplicant = notificationHelper.getApplicantCommonVars(caseNumber, caseData);
        templateVarsApplicant.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsApplicant.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());
        addDashboardLink(templateVarsApplicant);

        final NotificationResponse notificationResponse;
        if (caseData.getCicCase().getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            // Send Email
            notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getApplicantEmailAddress(),
                templateVarsApplicant,
                uploadedDocuments,
                getTemplateName(),
                caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVarsApplicant);
            notificationResponse = sendLetterNotification(templateVarsApplicant,
                CONTACT_PARTIES_POST, caseNumber);
        }

        cicCase.setAppNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber, final Map<String, String> uploadedDocuments) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsRepresentative.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());
        addDashboardLink(templateVarsRepresentative);

        final NotificationResponse notificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            // Send Email
            notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getRepresentativeEmailAddress(),
                templateVarsRepresentative,
                uploadedDocuments,
                getTemplateName(),
                caseNumber);

        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVarsRepresentative);
            notificationResponse = sendLetterNotification(templateVarsRepresentative,
                CONTACT_PARTIES_POST, caseNumber);
        }

        cicCase.setRepNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber, final Map<String, String> uploadedDocuments) {
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
                CONTACT_PARTIES_EMAIL,
                caseNumber);
        } else {
            notificationResponse = sendEmailNotification(templateVarsRespondent,
                cicCase.getRespondentEmail(), CONTACT_PARTIES_EMAIL, caseNumber);
        }

        cicCase.setResNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToTribunal(final CaseData caseData, final String caseNumber, final Map<String, String> uploadedDocuments) {
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
                CONTACT_PARTIES_EMAIL, caseNumber);
        } else {
            notificationResponse = sendEmailNotification(templateVarsTribunal,
                TRIBUNAL_EMAIL_VALUE, CONTACT_PARTIES_EMAIL, caseNumber);
        }

        cicCase.setTribunalNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars,
                                                       String toEmail,
                                                       TemplateName emailTemplateName,
                                                       String caseReferenceNumber) {
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(toEmail, templateVars, emailTemplateName);
        return notificationService.sendEmail(request, caseReferenceNumber);
    }

    private NotificationResponse sendEmailNotificationWithAttachment(
        String toEmail,
        final Map<String, Object> templateVars,
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

    private TemplateName getTemplateName() {
        return citizenDashboardEnabled ? CONTACT_PARTIES_EMAIL_NEW_CD : CONTACT_PARTIES_EMAIL;
    }

    private void addDashboardLink(Map<String, Object> templateVars) {
        if (citizenDashboardEnabled) {
            templateVars.put(DASHBOARD_KEY, citizenDashboardUrl);
        }
    }
}
