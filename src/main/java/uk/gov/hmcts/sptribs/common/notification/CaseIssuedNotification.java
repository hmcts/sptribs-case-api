package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.EmailTemplateName;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class CaseIssuedNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    static final String tribunalName = "Criminal Injuries Compensation Tribunal";

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsSubject = getCommonTemplateVars(caseNumber);
        templateVarsSubject.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsSubject.put(CommonConstants.CONTACT_NAME, cicCase.getFullName());

        if (cicCase.getContactPreferenceType().isEmail()) {
            // Send Email
            NotificationResponse notificationResponse =  sendEmailNotification(templateVarsSubject,
                cicCase.getEmail(),
                EmailTemplateName.CASE_ISSUED_CITIZEN_EMAIL);
            cicCase.setAppNotificationResponse(notificationResponse);
        } else {
            templateVarsSubject.put("address_line_1", cicCase.getAddress().getAddressLine1());
            templateVarsSubject.put("address_line_2", cicCase.getAddress().getAddressLine2());
            templateVarsSubject.put("address_line_3", cicCase.getAddress().getAddressLine3());
            templateVarsSubject.put("address_line_4", cicCase.getAddress().getPostCode());
            //SEND POST
            sendLetterNotification(templateVarsSubject, EmailTemplateName.CASE_ISSUED_CITIZEN_POST);
        }
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsApplicant  = getCommonTemplateVars(caseNumber);
        templateVarsApplicant.put(CommonConstants.CIC_CASE_APPLICANT_NAME, cicCase.getApplicantFullName());
        templateVarsApplicant.put(CommonConstants.CONTACT_NAME, cicCase.getApplicantFullName());

        if (caseData.getCicCase().getApplicantContactDetailsPreference().isEmail()) {
            // Send Email
            NotificationResponse notificationResponse = sendEmailNotification(templateVarsApplicant,
                cicCase.getApplicantEmailAddress(), EmailTemplateName.CASE_ISSUED_CITIZEN_EMAIL);
            cicCase.setAppNotificationResponse(notificationResponse);
        } else {
            templateVarsApplicant.put("address_line_1", cicCase.getAddress().getAddressLine1());
            templateVarsApplicant.put("address_line_2", cicCase.getAddress().getAddressLine2());
            templateVarsApplicant.put("address_line_3", cicCase.getAddress().getAddressLine3());
            templateVarsApplicant.put("address_line_4", cicCase.getAddress().getPostCode());
            sendLetterNotification(templateVarsApplicant, EmailTemplateName.CASE_ISSUED_CITIZEN_POST);
        }
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative  = getCommonTemplateVars(caseNumber);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, cicCase.getRepresentativeFullName());
        templateVarsRepresentative.put(CommonConstants.CONTACT_NAME, cicCase.getRepresentativeFullName());

        if (cicCase.getRepresentativeContactDetailsPreference().isEmail()) {
            // Send Email
            NotificationResponse notificationResponse = sendEmailNotification(templateVarsRepresentative,
                cicCase.getApplicantEmailAddress(), EmailTemplateName.CASE_ISSUED_CITIZEN_EMAIL);
            cicCase.setAppNotificationResponse(notificationResponse);
        } else {
            templateVarsRepresentative.put("address_line_1", cicCase.getAddress().getAddressLine1());
            templateVarsRepresentative.put("address_line_2", cicCase.getAddress().getAddressLine2());
            templateVarsRepresentative.put("address_line_3", cicCase.getAddress().getAddressLine3());
            templateVarsRepresentative.put("address_line_4", cicCase.getAddress().getPostCode());
            sendLetterNotification(templateVarsRepresentative, EmailTemplateName.CASE_ISSUED_CITIZEN_POST);
        }
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        final Map<String, Object> templateVarsRespondent = getCommonTemplateVars(caseNumber);
        CicCase cicCase = caseData.getCicCase();
        templateVarsRespondent.put(CommonConstants.CIC_CASE_RESPONDENT_NAME, caseData.getCicCase().getRespondantName());
        templateVarsRespondent.put(CommonConstants.CONTACT_NAME, cicCase.getRespondantName());

        // Send Email
        NotificationResponse notificationResponse = sendEmailNotification(templateVarsRespondent,
            caseData.getCicCase().getRespondantEmail(), EmailTemplateName.CASE_ISSUED_RESPONDENT_EMAIL);
        cicCase.setAppNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars,
                                                       String toEmail,
                                                       EmailTemplateName emailTemplateName) {

        NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(toEmail)
            .hasEmailAttachment(false)
            .template(emailTemplateName)
            .templateVars(templateVars)
            .build();

        notificationService.setNotificationRequest(request);
        return notificationService.sendEmail();
    }

    private void sendLetterNotification(Map<String, Object> templateVarsLetter, EmailTemplateName emailTemplateName) {
        NotificationRequest letterRequest = NotificationRequest.builder()
            .template(emailTemplateName)
            .templateVars(templateVarsLetter)
            .build();

        notificationService.setNotificationRequest(letterRequest);
        notificationService.sendLetter();
    }

    private Map<String, Object> getCommonTemplateVars(final String caseNumber) {
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CommonConstants.TRIBUNAL_NAME, tribunalName);
        templateVars.put(CommonConstants.CIC_CASE_NUMBER, caseNumber);
        return templateVars;
    }
}
