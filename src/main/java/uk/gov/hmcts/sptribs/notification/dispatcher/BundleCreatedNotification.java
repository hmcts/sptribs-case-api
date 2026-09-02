package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationContextRequest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.APPLICANT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.RESPONDENT;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.notification.TemplateName.BUNDLE_CREATED_EMAIL_CITIZEN;
import static uk.gov.hmcts.sptribs.notification.TemplateName.BUNDLE_CREATED_EMAIL_RESPONDENT;

@Component
@Slf4j
public class BundleCreatedNotification implements PartiesNotification {

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    @Value("${sptribs-frontend.dashboard-url}")
    private String citizenDashboardUrl;

    @Value("${feature.citizen-dashboard.enabled}")
    private boolean citizenDashboardEnabled;

    @Autowired
    public BundleCreatedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsApplicant = notificationHelper.getApplicantCommonVars(caseNumber, caseData);
        templateVarsApplicant.put(CommonConstants.CIC_CASE_APPLICANT_NAME, cicCase.getApplicantFullName());
        addDashboardLink(templateVarsApplicant);

        final NotificationResponse notificationResponse;

        notificationResponse = sendEmailNotification(templateVarsApplicant,
        cicCase.getApplicantEmailAddress(), BUNDLE_CREATED_EMAIL_CITIZEN, caseNumber);

        cicCase.setAppNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, cicCase.getRepresentativeFullName());
        addDashboardLink(templateVarsRepresentative);

        final NotificationResponse notificationResponse;

        notificationResponse = sendEmailNotification(templateVarsRepresentative,
        cicCase.getRepresentativeEmailAddress(), BUNDLE_CREATED_EMAIL_CITIZEN, caseNumber);

        cicCase.setRepNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRespondent = notificationHelper.getRespondentCommonVars(caseNumber, caseData);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_RESPONDENT_NAME, cicCase.getRespondentName());
        addDashboardLink(templateVarsRespondent);

        final NotificationResponse notificationResponse;

        notificationResponse = sendEmailNotification(templateVarsRespondent,
            cicCase.getRespondentEmail(), BUNDLE_CREATED_EMAIL_RESPONDENT, caseNumber);

        cicCase.setResNotificationResponse(notificationResponse);
    }

    @Override
    public Set<NotificationParties> buildCorrespondenceParties(NotificationContextRequest request) {
        Set<NotificationParties> correspondenceParties = new HashSet<>();
        CicCase cicCase = request.getCaseData().getCicCase();

        if (cicCase.getRespondentEmail() != null) {
            correspondenceParties.add(RESPONDENT);
        }
        if (!CollectionUtils.isEmpty(cicCase.getRepresentativeCIC())) {
            correspondenceParties.add(REPRESENTATIVE);
        }
        if (CollectionUtils.isEmpty(cicCase.getRepresentativeCIC())
            && !CollectionUtils.isEmpty(cicCase.getApplicantCIC())) {
            correspondenceParties.add(APPLICANT);
        }

        return correspondenceParties;
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars, String toEmail, TemplateName templateName,
                                                       String caseReferenceNumber) {
        return notificationService.sendEmail(
            notificationHelper.buildEmailNotificationRequest(toEmail,
                templateVars,
                templateName),
            caseReferenceNumber, null);
    }

    private void addDashboardLink(Map<String, Object> templateVars) {
        if (citizenDashboardEnabled) {
            templateVars.put(DASHBOARD_KEY, citizenDashboardUrl);
        }
    }
}
