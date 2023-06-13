package uk.gov.hmcts.sptribs.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.DssNotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;


@Component
@Slf4j
public class DssApplicationReceivedNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    @Autowired
    private DssNotificationHelper dssNotificationHelper;


    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        DssCaseData dssCaseData = caseData.getDssCaseData();
        final Map<String, Object> templateVarsSubject = dssNotificationHelper.getSubjectCommonVars(caseNumber, dssCaseData);
        templateVarsSubject.put(CommonConstants.CIC_CASE_SUBJECT_NAME, dssCaseData.getSubjectFullName());
        templateVarsSubject.put(CommonConstants.CONTACT_PARTY_INFO, dssCaseData.getNotifyPartyMessage());

            NotificationResponse notificationResponse = sendEmailNotification(
                templateVarsSubject,
                dssCaseData.getSubjectEmailAddress(),
                            TemplateName.APPLICATION_RECEIVED);
            dssCaseData.setSubjectNotifyList(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        DssCaseData dssCaseData = caseData.getDssCaseData();
        final Map<String, Object> templateVarsRepresentative = dssNotificationHelper.getRepresentativeCommonVars(caseNumber, dssCaseData);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, dssCaseData.getRepresentativeFullName());
        templateVarsRepresentative.put(CommonConstants.CONTACT_PARTY_INFO, dssCaseData.getNotifyPartyMessage());

            NotificationResponse notificationResponse = sendEmailNotification(
                templateVarsRepresentative,
                dssCaseData.getRepresentativeEmailAddress(),
                            TemplateName.APPLICATION_RECEIVED);
            dssCaseData.setRepNotificationResponse(notificationResponse);
    }


    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars,
                                                       String toEmail,
                                                       TemplateName emailTemplateName) {
        NotificationRequest request = dssNotificationHelper.buildEmailNotificationRequest(
            toEmail, templateVars, emailTemplateName);
        return notificationService.sendEmail(request);
    }

}
