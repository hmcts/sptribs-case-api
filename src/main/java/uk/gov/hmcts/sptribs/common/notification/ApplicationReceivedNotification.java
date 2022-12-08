package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.EmailTemplateName;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;

@Component
@Slf4j
public class ApplicationReceivedNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        if (cicCase.getContactPreferenceType().isEmail()) {
            Map<String, Object> templateVars = templateVars(cicCase, caseNumber);
            templateVars.put(CONTACT_NAME, cicCase.getFullName());

            // Send Email
            NotificationResponse notificationResponse = sendEmailNotification(cicCase.getEmail(), templateVars);
            cicCase.setSubjectNotifyList(notificationResponse);
        }
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        if (cicCase.getApplicantContactDetailsPreference().isEmail()) {
            Map<String, Object> templateVars = templateVars(cicCase, caseNumber);
            templateVars.put(CONTACT_NAME, cicCase.getApplicantFullName());

            // Send Email
            NotificationResponse notificationResponse = sendEmailNotification(cicCase.getApplicantEmailAddress(), templateVars);
            cicCase.setAppNotificationResponse(notificationResponse);
        }
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        if (cicCase.getRepresentativeContactDetailsPreference().isEmail()) {
            Map<String, Object> templateVars = templateVars(cicCase, caseNumber);
            templateVars.put(CONTACT_NAME, cicCase.getRepresentativeFullName());

            // Send Email
            NotificationResponse notificationResponse = sendEmailNotification(cicCase.getRepresentativeEmailAddress(), templateVars);
            cicCase.setRepNotificationResponse(notificationResponse);
        }
    }

    private NotificationResponse sendEmailNotification(final String destinationAddress, final Map<String, Object> templateVars) {
        NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(destinationAddress)
            .template(EmailTemplateName.APPLICATION_RECEIVED)
            .templateVars(templateVars)
            .build();

        notificationService.setNotificationRequest(request);
        return notificationService.sendEmail();
    }

    private Map<String, Object> templateVars(final CicCase cicCase, final String caseNumber) {
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(TRIBUNAL_NAME, "Criminal Injuries Compensation Tribunal");
        templateVars.put(CIC_CASE_NUMBER, caseNumber);
        templateVars.put(CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        return templateVars;
    }


}
