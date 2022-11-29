package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.notification.EmailTemplateName;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.service.notify.NotificationClientException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ApplicationNewOrderIssuedNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    @Override
    public void sendToSubject(final CaseData caseData, final Long caseId) {
        // Send Email
        sendEmailNotification(caseData, caseId);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final Long caseId) {
        //No operation
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final Long caseId) {
        //No operation
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final Long caseId) {
        //No operation
    }

    private void sendEmailNotification(final CaseData caseData, final Long caseId) {
        Map<String, Object> templateVars = templateVars(caseData, caseId);
        templateVars.put("TribunalName", "testtribunalName");
        templateVars.put("CicCaseNumber", "123");
        templateVars.put("CicCaseSubjectFullName", "testFullName");
        templateVars.put("ContactName", "testContactName");

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("sampleFile.txt").getFile());

        byte [] fileContents = null;
        try {
            fileContents = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        NotificationRequest request = NotificationRequest.builder()
            .destinationAddress("santoshini.jami@hmcts.net")
            .template(EmailTemplateName.APPLICATION_NEW_ORDER_ISSUED)
            .templateVars(templateVars)
            .hasEmailAttachment(true)
            .fileContents(fileContents)
            .build();

        notificationService.setNotificationRequest(request);
        try {
            notificationService.sendEmail();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> templateVars(final CaseData caseData, final Long caseId) {
        final Map<String, Object> templateVars = new HashMap<>();
        return templateVars;
    }

}
