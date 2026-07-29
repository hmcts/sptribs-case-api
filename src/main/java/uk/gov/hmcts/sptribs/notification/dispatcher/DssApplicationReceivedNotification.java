package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.notification.DssNotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;

import java.util.Map;

import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_REPRESENTATIVE_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_PARTY_INFO;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED_CY;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED_NEW_CD;

@Component
@Setter
public class DssApplicationReceivedNotification extends PartiesNotification {

    @Value("${sptribs-frontend.dashboard-url}")
    private String citizenDashboardUrl;

    @Value("${feature.citizen-dashboard.enabled}")
    private boolean citizenDashboardEnabled;

    public DssApplicationReceivedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    protected PartyNotification buildSubjectNotification(CaseData caseData, String caseNumber) {
        DssCaseData dssCaseData = caseData.getDssCaseData();
        Map<String, Object> templateVarsSubject = DssNotificationHelper.getSubjectCommonVars(caseNumber, caseData);
        templateVarsSubject.put(CIC_CASE_SUBJECT_NAME, dssCaseData.getSubjectFullName());
        templateVarsSubject.put(CONTACT_PARTY_INFO, dssCaseData.getNotifyPartyMessage());
        addDashboardLink(templateVarsSubject);

        notificationHelper().addCicaReferenceNumber(caseData, templateVarsSubject);
        LanguagePreference languagePreference = dssCaseData.getLanguagePreference();
        TemplateName templateName = languagePreference == ENGLISH ? getTemplateName() : APPLICATION_RECEIVED_CY;

        return emailOnly(dssCaseData.getSubjectEmailAddress(), templateVarsSubject, templateName, saveToDssData(DssCaseData::setSubjectNotificationResponse));
    }

    @Override
    protected PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        DssCaseData dssCaseData = caseData.getDssCaseData();
        Map<String, Object> templateVarsRep = DssNotificationHelper.getRepresentativeCommonVars(caseNumber, caseData);
        templateVarsRep.put(CIC_CASE_REPRESENTATIVE_NAME, dssCaseData.getRepresentativeFullName());
        templateVarsRep.put(CONTACT_PARTY_INFO, dssCaseData.getNotifyPartyMessage());
        addDashboardLink(templateVarsRep);

        notificationHelper().addCicaReferenceNumber(caseData, templateVarsRep);
        LanguagePreference languagePreference = dssCaseData.getLanguagePreference();
        TemplateName templateName = languagePreference == ENGLISH ? getTemplateName() : APPLICATION_RECEIVED_CY;

        return emailOnly(dssCaseData.getRepresentativeEmailAddress(), templateVarsRep, templateName, saveToDssData(DssCaseData::setRepNotificationResponse));
    }

    private TemplateName getTemplateName() {
        return citizenDashboardEnabled ? APPLICATION_RECEIVED_NEW_CD : APPLICATION_RECEIVED;
    }

    private void addDashboardLink(Map<String, Object> templateVars) {
        if (citizenDashboardEnabled) {
            templateVars.put(DASHBOARD_KEY, citizenDashboardUrl);
        }
    }
}
