package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
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
public class CaseIssuedNotification implements PartiesNotification {

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    private static final int DOC_ATTACH_LIMIT = 5;

    @Autowired
    public CaseIssuedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsSubject = notificationHelper.getSubjectCommonVars(caseNumber, caseData);
        templateVarsSubject.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());

        final NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            // Send Email
            notificationResponse = sendEmailNotification(templateVarsSubject,
                cicCase.getEmail(),
                TemplateName.CASE_ISSUED_CITIZEN_EMAIL, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVarsSubject);
            //SEND POST
            notificationResponse = sendLetterNotification(templateVarsSubject, caseNumber);
        }

        cicCase.setSubjectLetterNotifyList(notificationResponse);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsApplicant = notificationHelper.getApplicantCommonVars(caseNumber, caseData);
        templateVarsApplicant.put(CommonConstants.CIC_CASE_APPLICANT_NAME, cicCase.getApplicantFullName());

        final NotificationResponse notificationResponse;
        if (caseData.getCicCase().getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            // Send Email
            notificationResponse = sendEmailNotification(templateVarsApplicant,
                cicCase.getApplicantEmailAddress(), TemplateName.CASE_ISSUED_CITIZEN_EMAIL, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVarsApplicant);
            notificationResponse = sendLetterNotification(templateVarsApplicant, caseNumber);
        }

        cicCase.setAppNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, cicCase.getRepresentativeFullName());

        final NotificationResponse notificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            // Send Email
            notificationResponse = sendEmailNotification(templateVarsRepresentative,
                cicCase.getRepresentativeEmailAddress(), TemplateName.CASE_ISSUED_CITIZEN_EMAIL, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVarsRepresentative);
            notificationResponse = sendLetterNotification(templateVarsRepresentative, caseNumber);
        }

        cicCase.setRepNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRespondent = notificationHelper.getRespondentCommonVars(caseNumber, caseData);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_RESPONDENT_NAME, caseData.getCicCase().getRespondentName());

        final NotificationResponse notificationResponse;
        if (ObjectUtils.isNotEmpty(caseData.getCaseIssue().getDocumentList())) {
            final Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);

            // Send Email
            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getRespondentEmail(),
                templateVarsRespondent,
                uploadedDocuments, caseNumber);
            cicCase.setSubjectLetterNotifyList(notificationResponse);
        } else {
            notificationResponse = sendEmailNotification(templateVarsRespondent,
                cicCase.getRespondentEmail(), TemplateName.CASE_ISSUED_RESPONDENT_EMAIL, caseNumber);
            cicCase.setResNotificationResponse(notificationResponse);
        }
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars, String toEmail,
                                                       TemplateName emailTemplateName, String caseReferenceNumber) {
        return notificationService.sendEmail(
            notificationHelper.buildEmailNotificationRequest(toEmail,
                templateVars,
                emailTemplateName),
            caseReferenceNumber);
    }

    private NotificationResponse sendEmailNotificationWithAttachment(String toEmail, final Map<String, Object> templateVars,
                                                                     Map<String, String> uploadedDocuments,
                                                                     String caseReferenceNumber) {
        return notificationService.sendEmail(
            notificationHelper.buildEmailNotificationRequest(toEmail,
                true,
                uploadedDocuments,
                templateVars,
                TemplateName.CASE_ISSUED_RESPONDENT_EMAIL),
            caseReferenceNumber);
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter, String caseReferenceNumber) {
        final NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(templateVarsLetter,
            TemplateName.CASE_ISSUED_CITIZEN_POST);
        return notificationService.sendLetter(letterRequest, caseReferenceNumber);
    }

    private Map<String, String> getUploadedDocuments(CaseData caseData) {
        final CaseIssue caseIssue = caseData.getCaseIssue();
        return notificationHelper.buildDocumentList(caseIssue.getDocumentList(), DOC_ATTACH_LIMIT);
    }

}
