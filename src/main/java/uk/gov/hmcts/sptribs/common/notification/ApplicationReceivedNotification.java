package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.EmailTemplateName;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;

@Component
@Slf4j
public class ApplicationReceivedNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    @Autowired
    private NotificationHelper notificationHelper;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        if (cicCase.getContactPreferenceType().isEmail()) {
            Map<String, Object> templateVars = notificationHelper.commonTemplateVars(cicCase, caseNumber);
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
            Map<String, Object> templateVars = notificationHelper.commonTemplateVars(cicCase, caseNumber);
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
            Map<String, Object> templateVars = notificationHelper.commonTemplateVars(cicCase, caseNumber);
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




}
