package uk.gov.hmcts.sptribs.citizen.event;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.DssNotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_REPRESENTATIVE_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_PARTY_INFO;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED_CY;

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

        NotificationResponse notificationResponse = sendEmailNotification(
            templateVarsSubject,
            dssCaseData.getSubjectEmailAddress(),
            dssCaseData.getLanguagePreference()
        );
        dssCaseData.setSubjectNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final DssCaseData dssCaseData, final String caseNumber) {
        final Map<String, Object> templateVarsRep = dssNotificationHelper.getRepresentativeCommonVars(caseNumber, dssCaseData);
        templateVarsRep.put(CIC_CASE_REPRESENTATIVE_NAME, dssCaseData.getRepresentativeFullName());
        templateVarsRep.put(CONTACT_PARTY_INFO, dssCaseData.getNotifyPartyMessage());

        NotificationResponse notificationResponse = sendEmailNotification(
            templateVarsRep,
            dssCaseData.getRepresentativeEmailAddress(),
            ENGLISH
        );
        dssCaseData.setRepNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars,
                                                       String toEmail,
                                                       LanguagePreference languagePreference) {
        TemplateName templateName = ENGLISH.equals(languagePreference) ? APPLICATION_RECEIVED : APPLICATION_RECEIVED_CY;
        NotificationRequest request =
            dssNotificationHelper.buildEmailNotificationRequest(toEmail, templateVars, templateName);
        return notificationService.sendEmail(request);
    }

}
