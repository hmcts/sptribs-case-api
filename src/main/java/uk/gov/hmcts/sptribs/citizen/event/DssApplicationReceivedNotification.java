package uk.gov.hmcts.sptribs.citizen.event;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.DssNotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_REPRESENTATIVE_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_PARTY_INFO;

@Component
@Setter
public class DssApplicationReceivedNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    @Autowired
    private DssNotificationHelper dssNotificationHelper;

    @Override
    public void sendToSubject(final DssCaseData dssCaseData, final String caseNumber) {
        final Map<String, Object> templateVarsSubject = dssNotificationHelper.getSubjectCommonVars(caseNumber, dssCaseData);
        templateVarsSubject.put(CIC_CASE_SUBJECT_NAME, dssCaseData.getSubjectFullName());
        templateVarsSubject.put(CONTACT_PARTY_INFO, dssCaseData.getNotifyPartyMessage());

        NotificationResponse notificationResponse = sendEmailNotification(templateVarsSubject, dssCaseData.getSubjectEmailAddress());
        dssCaseData.setSubjectNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final DssCaseData dssCaseData, final String caseNumber) {
        final Map<String, Object> templateVarsRep = dssNotificationHelper.getRepresentativeCommonVars(caseNumber, dssCaseData);
        templateVarsRep.put(CIC_CASE_REPRESENTATIVE_NAME, dssCaseData.getRepresentativeFullName());
        templateVarsRep.put(CONTACT_PARTY_INFO, dssCaseData.getNotifyPartyMessage());

        NotificationResponse notificationResponse = sendEmailNotification(templateVarsRep, dssCaseData.getRepresentativeEmailAddress());
        dssCaseData.setRepNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars, String toEmail) {
        NotificationRequest request = dssNotificationHelper.buildEmailNotificationRequest(
            toEmail, templateVars, TemplateName.APPLICATION_RECEIVED);
        return notificationService.sendEmail(request);
    }

}
