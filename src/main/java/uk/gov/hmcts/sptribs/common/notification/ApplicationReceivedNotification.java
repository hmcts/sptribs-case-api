package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationType;
import uk.gov.hmcts.sptribs.notification.EmailTemplateName;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.service.notify.SendEmailResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Component
@Slf4j
public class ApplicationReceivedNotification implements PartiesNotification {

    private static final String CONTACT_NAME = "ContactName";
    private static final String TRIBUNAL_NAME = "TribunalName";
    private static final String CIC_CASE_NUMBER = "CicCaseNumber";
    private static final String CIC_CASE_SUBJECT_NAME = "CicCaseSubjectFullName";

    @Autowired
    private NotificationServiceCIC notificationService;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        if (cicCase.getContactPreferenceType().isEmail()) {
            Map<String, Object> templateVars = templateVars(cicCase, caseNumber);
            templateVars.put(CONTACT_NAME, cicCase.getFullName());

            // Send Email
            SendEmailResponse sendEmailResponse = sendEmailNotification(cicCase.getEmail(), templateVars);
            cicCase.setSubjectNotificationResponse(getNotificationResponse(sendEmailResponse));
        }
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        if (cicCase.getApplicantContactDetailsPreference().isEmail()) {
            Map<String, Object> templateVars = templateVars(cicCase, caseNumber);
            templateVars.put(CONTACT_NAME, cicCase.getApplicantFullName());

            // Send Email
            SendEmailResponse sendEmailResponse = sendEmailNotification(cicCase.getApplicantEmailAddress(), templateVars);
            cicCase.setApplicantNotificationResponse(getNotificationResponse(sendEmailResponse));
        }
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        if (cicCase.getRepresentativeContactDetailsPreference().isEmail()) {
            Map<String, Object> templateVars = templateVars(cicCase, caseNumber);
            templateVars.put(CONTACT_NAME, cicCase.getRepresentativeFullName());

            // Send Email
            SendEmailResponse sendEmailResponse = sendEmailNotification(cicCase.getRepresentativeEmailAddress(), templateVars);
            cicCase.setRepNotificationResponse(getNotificationResponse(sendEmailResponse));
        }
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        //No operation
    }

    private SendEmailResponse sendEmailNotification(final String destinationAddress, final Map<String, Object> templateVars) {
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

    private NotificationResponse getNotificationResponse(final SendEmailResponse sendEmailResponse) {
        String clientReference = nonNull(sendEmailResponse) ? sendEmailResponse.getReference().orElse(null) : null;
        UUID id = nonNull(sendEmailResponse) ? sendEmailResponse.getNotificationId() : null;

        return NotificationResponse.builder()
            .id(id.toString())
            .client_reference(clientReference)
            .notificationType(NotificationType.EMAIL)
            .updatedAtTime(LocalDateTime.now())
            .createdAtTime(LocalDateTime.now())
            .status("Received")
            .build();
    }
}
