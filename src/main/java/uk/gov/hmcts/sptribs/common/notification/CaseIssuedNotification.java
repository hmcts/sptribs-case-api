package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.notification.EmailTemplateName;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class CaseIssuedNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    final String tribunalName = "riminal Injuries Compensation Tribunal";

    @Override
    public void sendToSubject(final CaseData caseData, final Long caseId) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsSubject = getCommonTemplateVars(caseId);
        templateVarsSubject.put("CicCaseSubjectFullName", cicCase.getFullName());
        templateVarsSubject.put("ContactName", cicCase.getFullName());

        if(cicCase.getContactPreferenceType().isEmail()) {
            // Send Email
            sendEmailNotification(templateVarsSubject, cicCase.getEmail(), EmailTemplateName.CASE_ISSUED_CITIZEN_EMAIL);
        }
        else{
            templateVarsSubject.put("address_line_1", cicCase.getAddress().getAddressLine1());
            templateVarsSubject.put("address_line_2", cicCase.getAddress().getAddressLine2());
            templateVarsSubject.put("address_line_3", cicCase.getAddress().getAddressLine3());
            templateVarsSubject.put("address_line_4", cicCase.getAddress().getPostCode());
            //SEND POST
            sendLetterNotification(templateVarsSubject, EmailTemplateName.CASE_ISSUED_CITIZEN_POST);
        }
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final Long caseId) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsApplicant  = getCommonTemplateVars(caseId);
        templateVarsApplicant.put("CicCaseSubjectFullName", cicCase.getApplicantFullName());
        templateVarsApplicant.put("ContactName", cicCase.getApplicantFullName());

        if(caseData.getCicCase().getApplicantContactDetailsPreference().isEmail()){
            // Send Email
            sendEmailNotification(templateVarsApplicant, cicCase.getApplicantEmailAddress(), EmailTemplateName.CASE_ISSUED_CITIZEN_EMAIL);
        }
        else{
            templateVarsApplicant.put("address_line_1", cicCase.getAddress().getAddressLine1());
            templateVarsApplicant.put("address_line_2", cicCase.getAddress().getAddressLine2());
            templateVarsApplicant.put("address_line_3", cicCase.getAddress().getAddressLine3());
            templateVarsApplicant.put("address_line_4", cicCase.getAddress().getPostCode());
            sendLetterNotification(templateVarsApplicant, EmailTemplateName.CASE_ISSUED_CITIZEN_POST);
        }
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final Long caseId) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative  = getCommonTemplateVars(caseId);
        templateVarsRepresentative.put("CicCaseSubjectFullName", cicCase.getRepresentativeFullName());
        templateVarsRepresentative.put("ContactName", cicCase.getRepresentativeFullName());

        if(cicCase.getRepresentativeContactDetailsPreference().isEmail()) {
            // Send Email
            sendEmailNotification(templateVarsRepresentative, cicCase.getApplicantEmailAddress(), EmailTemplateName.CASE_ISSUED_CITIZEN_EMAIL);
        }
        else{
            templateVarsRepresentative.put("address_line_1", cicCase.getAddress().getAddressLine1());
            templateVarsRepresentative.put("address_line_2", cicCase.getAddress().getAddressLine2());
            templateVarsRepresentative.put("address_line_3", cicCase.getAddress().getAddressLine3());
            templateVarsRepresentative.put("address_line_4", cicCase.getAddress().getPostCode());
            sendLetterNotification(templateVarsRepresentative, EmailTemplateName.CASE_ISSUED_CITIZEN_POST);
        }
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final Long caseId) {
        final Map<String, Object> templateVarsRespondent = getCommonTemplateVars(caseId);
        templateVarsRespondent.put("CicCaseSubjectFullName", caseData.getCicCase().getRespondantName());
        templateVarsRespondent.put("ContactName", caseData.getCicCase().getRespondantName());

        // Send Email
        sendEmailNotification(templateVarsRespondent, caseData.getCicCase().getRespondantEmail(), EmailTemplateName.CASE_ISSUED_RESPONDENT_EMAIL);
    }

    private void sendEmailNotification(final Map<String, Object> templateVars, String toEmail, EmailTemplateName emailTemplateName) {

        NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(toEmail)
            .hasEmailAttachment(false)
            .template(emailTemplateName)
            .templateVars(templateVars)
            .build();

        notificationService.setNotificationRequest(request);
        try {
            notificationService.sendEmail();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendLetterNotification(Map<String, Object> templateVarsLetter, EmailTemplateName emailTemplateName) {
        NotificationRequest letterRequest = NotificationRequest.builder()
            .template(emailTemplateName)
            .templateVars(templateVarsLetter)
            .build();

        notificationService.setNotificationRequest(letterRequest);
        notificationService.sendLetter();
    }

    private Map<String, Object> getCommonTemplateVars(final Long caseId){
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put("TribunalName", tribunalName);
        templateVars.put("CicCaseNumber", caseId);
        return templateVars;
    }
}
