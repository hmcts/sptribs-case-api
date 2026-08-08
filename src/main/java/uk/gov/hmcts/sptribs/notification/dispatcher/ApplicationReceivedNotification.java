package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;

import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED_NEW_CD;

@Component
@Slf4j
public class ApplicationReceivedNotification extends PartiesNotification {
    @Value("${sptribs-frontend.dashboard-url}")
    private String citizenDashboardUrl;

    @Value("${feature.citizen-dashboard.enabled}")
    private boolean citizenDashboardEnabled;

    public ApplicationReceivedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    protected PartyNotification buildSubjectNotification(CaseData caseData, String caseNumber) {
        Map<String, Object> templateVars = notificationHelper().getSubjectCommonVars(caseNumber, caseData);
        addDashboardLink(templateVars);
        CicCase cicCase = caseData.getCicCase();

        return emailOnly(
                cicCase.getEmail(),
                templateVars,
                getTemplateName(),
                saveToCicCase(CicCase::setSubjectNotifyList)
        );
    }

    @Override
    protected PartyNotification buildApplicantNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getApplicantCommonVars(caseNumber, caseData);
        addDashboardLink(templateVars);

        return emailOnly(
                cicCase.getApplicantEmailAddress(),
                templateVars,
                getTemplateName(),
                saveToCicCase(CicCase::setAppNotificationResponse)
        );
    }

    @Override
    protected PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getRepresentativeCommonVars(caseNumber, caseData);
        addDashboardLink(templateVars);

        return emailOnly(
                cicCase.getRepresentativeEmailAddress(),
                templateVars,
                getTemplateName(),
                saveToCicCase(CicCase::setRepNotificationResponse)
        );
    }

    private TemplateName getTemplateName() {
        return citizenDashboardEnabled ?  APPLICATION_RECEIVED_NEW_CD : APPLICATION_RECEIVED;
    }

    private void addDashboardLink(Map<String, Object> templateVars) {
        if (citizenDashboardEnabled) {
            templateVars.put(DASHBOARD_KEY, citizenDashboardUrl);
        }
    }
}
