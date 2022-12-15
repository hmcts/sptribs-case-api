package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
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
import uk.gov.service.notify.NotificationClientException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

@Component
@Slf4j
public class CaseFinalDecisionIssuedNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    private static final String tribunalName = "Criminal Injuries Compensation Tribunal";

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsSubject = getCommonTemplateVars(caseNumber);
        templateVarsSubject.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsSubject.put(CommonConstants.CONTACT_NAME, cicCase.getFullName());

        if (cicCase.getContactPreferenceType().isEmail()) {
            // Send Email
            NotificationResponse notificationResponse = sendEmailNotification(templateVarsSubject,
                cicCase.getEmail(),
                EmailTemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
            cicCase.setSubjectNotifyList(notificationResponse);
        } else {
            templateVarsSubject.put("address_line_1", cicCase.getAddress().getAddressLine1());
            templateVarsSubject.put("address_line_2", cicCase.getAddress().getAddressLine2());
            templateVarsSubject.put("address_line_3", cicCase.getAddress().getAddressLine3());
            templateVarsSubject.put("address_line_4", cicCase.getAddress().getPostCode());
            //SEND POST
            NotificationResponse notificationResponse = sendLetterNotification(templateVarsSubject,
                EmailTemplateName.CASE_FINAL_DECISION_ISSUED_POST);
            cicCase.setSubjectLetterNotifyList(notificationResponse);
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
                cicCase.getRepresentativeEmailAddress(), EmailTemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
            cicCase.setRepNotificationResponse(notificationResponse);
        } else {
            templateVarsRepresentative.put("address_line_1", cicCase.getRepresentativeAddress().getAddressLine1());
            templateVarsRepresentative.put("address_line_2", cicCase.getRepresentativeAddress().getAddressLine2());
            templateVarsRepresentative.put("address_line_3", cicCase.getRepresentativeAddress().getAddressLine3());
            templateVarsRepresentative.put("address_line_4", cicCase.getRepresentativeAddress().getPostCode());
            NotificationResponse notificationResponse = sendLetterNotification(templateVarsRepresentative,
                EmailTemplateName.CASE_FINAL_DECISION_ISSUED_POST);
            cicCase.setRepLetterNotificationResponse(notificationResponse);
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
            caseData.getCicCase().getRespondantEmail(), EmailTemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
        cicCase.setResNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars,
                                                       String toEmail,
                                                       EmailTemplateName emailTemplateName) {

        NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(toEmail)
            .hasEmailAttachment(true)
            .template(emailTemplateName)
            .templateVars(templateVars)
            .build();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("sampleFile.txt").getFile());
        File file1 = new File(classLoader.getResource("sampleFile1.txt").getFile());

        byte [] finalDecisionNoticeFileContent = null;
        byte [] finalDecisionGuidanceFileContent = null;
        try {
            finalDecisionNoticeFileContent = FileUtils.readFileToByteArray(file);
            finalDecisionGuidanceFileContent = FileUtils.readFileToByteArray(file1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        addFileAttachments(templateVars, finalDecisionNoticeFileContent, finalDecisionGuidanceFileContent);
        notificationService.setNotificationRequest(request);
        return notificationService.sendEmail();
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter, EmailTemplateName emailTemplateName) {
        NotificationRequest letterRequest = NotificationRequest.builder()
            .template(emailTemplateName)
            .templateVars(templateVarsLetter)
            .build();

        notificationService.setNotificationRequest(letterRequest);
        return notificationService.sendLetter();
    }

    private void addFileAttachments(Map<String, Object> templateVars, byte[] finalDecisionNoticeFileContent, byte[] finalDecisionGuidanceFileContent) {

            JSONObject jsonObjectFinalDecisionNotice = notificationService.prepareUpload(finalDecisionNoticeFileContent);

            JSONObject jsonObjectFinalDecisionGuidance = notificationService.prepareUpload(finalDecisionGuidanceFileContent);
            if(nonNull(jsonObjectFinalDecisionNotice)) {
                templateVars.put("FinalDecisionNotice", jsonObjectFinalDecisionNotice);
            }
            if(nonNull(jsonObjectFinalDecisionGuidance)) {
                templateVars.put("FinalDecisionGuidance", jsonObjectFinalDecisionGuidance);
            }
    }

    private Map<String, Object> getCommonTemplateVars(final String caseNumber) {
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CommonConstants.TRIBUNAL_NAME, tribunalName);
        templateVars.put(CommonConstants.CIC_CASE_NUMBER, caseNumber);
        return templateVars;
    }
}
