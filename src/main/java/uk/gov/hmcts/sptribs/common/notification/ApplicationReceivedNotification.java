package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.notification.EmailTemplateName;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class ApplicationReceivedNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    @Override
    public void sendToSubject(final CaseData caseData, final Long caseId) {
        // Send Email
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

        //Send Letter
        Map<String, String> templateVarsLetter = templateVars(caseData, caseId);
        templateVarsLetter.put("address_line_1", "Addrees 1");
        templateVarsLetter.put("address_line_2", "Address 2");
        templateVarsLetter.put("address_line_3", "Address 3");
        templateVarsLetter.put("address_line_4", "Address 4");
        templateVarsLetter.put("TribunalName", "testtribunalName");
        templateVarsLetter.put("CicCaseNumber", "123");
        templateVarsLetter.put("CicCaseSubjectFullName", "testFullName");
        templateVarsLetter.put("ContactName", "testContactName");
        templateVarsLetter.put("StayExpirationDate", LocalDate.now().format(DATE_TIME_FORMATTER));
        templateVarsLetter.put("stayStayReason", "testStayReason");
        templateVarsLetter.put("stayAdditionalDetail", "testAddtionalDetails");

        NotificationRequest letterRequest = NotificationRequest.builder()
            .template(EmailTemplateName.CASE_STAYED)
            .templateVars(templateVarsLetter)
            .build();

        notificationService.setNotificationRequest(letterRequest);
        notificationService.sendLetter();
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
        return templateVars;
    }
}
