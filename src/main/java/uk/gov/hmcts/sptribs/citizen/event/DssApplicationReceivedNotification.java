package uk.gov.hmcts.sptribs.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.Notification;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.DssNotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.NotifyProxyClient;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;


@Component
@Slf4j
public class DssApplicationReceivedNotification implements PartiesNotification {

    @Autowired
    private DssNotificationHelper dssNotificationHelper;

   /* @Autowired
    private NotifyProxyClient notifyProxyClient;
*/

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {

        CicCase cicCase = caseData.getCicCase();
        DssCaseData dssCaseData = caseData.getDssCaseData();

        final Map<String, Object> templateVarsSubject = dssNotificationHelper.getSubjectCommonVars(caseNumber, caseData);
        templateVarsSubject.put(CommonConstants.CIC_CASE_SUBJECT_NAME, dssCaseData.getSubjectFullName());
        templateVarsSubject.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        /*NotificationResponse notificationResponse = sendEmailNotification(
            templateVarsSubject,
            cicCase.getEmail(),
            TemplateName.APPLICATION_RECEIVED);
        cicCase.setSubjectNotifyList(notificationResponse);*/
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative = dssNotificationHelper.getRepresentativeCommonVars(caseNumber, caseData);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, cicCase.getRepresentativeFullName());
        templateVarsRepresentative.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        String notificationResponse = sendEmailNotification(
            templateVarsRepresentative,
            cicCase.getSelectedHearingToCancel(),
            TemplateName.APPLICATION_RECEIVED);
        Notification notification = cicCase.getNotification();
        Notification.DssApplicationReceived  dssApplicationReceived = notification.new DssApplicationReceived();
        dssApplicationReceived.setRepNotificationSent(notificationResponse);
        cicCase.setNotification(notification);
        //cicCase.setRepNotificationResponse(notificationResponse);
    }

    private String sendEmailNotification(final Map<String, Object> templateVars,
                                         String toEmail,
                                         TemplateName emailTemplateName) {
        NotificationRequest request = dssNotificationHelper.buildEmailNotificationRequest(
            toEmail, templateVars, emailTemplateName);

//        TODO
        //ResponseEntity<?> responseEntity = notifyProxyClient.sendEmailNotification();
        //String isNotificationSent = (String) responseEntity.getBody();
        //return notificationService.sendEmail(request); remove this
        return "isNotificationSent";
    }

}
