package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.notification.EmailTemplateName;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ApplicationReceivedNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    @Override
    public void sendToSubject(final CaseData caseData, final Long caseId) {
        Map<String, String> templateVars = templateVars(caseData, caseId);
        templateVars.put("TribunalName", "testtribunalName");
        templateVars.put("CicCaseNumber", "123");
        templateVars.put("CicCaseSubjectFullName", "testFullName");
        templateVars.put("ContactName", "testContactName");

        NotificationRequest request = NotificationRequest.builder()
            .destinationAddress("santoshini.jami@hmcts.net")
            .template(EmailTemplateName.APPLICATION_RECEIVED)
            .templateVars(templateVars)
            .build();

        notificationService.setNotificationRequest(request);
        notificationService.sendEmail();
    }

    public void sendToApplicant(final CaseData caseData, final Long caseId) {
        //No operation
    }

    public void sendToRepresentative(final CaseData caseData, final Long caseId) {
        //No operation
    }

    public void sendToRespondent(final CaseData caseData, final Long caseId) {
        //No operation
    }

    private Map<String, String> templateVars(final CaseData caseData, final Long caseId) {
        final Map<String, String> templateVars = new HashMap<>();
        //templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(caseId));
        /*templateVars.put(GENERAL_OTHER_RECIPIENT_NAME, caseData.getGeneralEmail().getGeneralEmailOtherRecipientName());
        templateVars.put(GENERAL_EMAIL_DETAILS, caseData.getGeneralEmail().getGeneralEmailDetails());*/
        return templateVars;
    }
}
