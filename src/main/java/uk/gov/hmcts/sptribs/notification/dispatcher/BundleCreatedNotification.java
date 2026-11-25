package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationContext;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.notification.NotificationHelper.getNotificationPartiesBundle;
import static uk.gov.hmcts.sptribs.notification.TemplateName.BUNDLE_CREATED_EMAIL_CITIZEN;
import static uk.gov.hmcts.sptribs.notification.TemplateName.BUNDLE_CREATED_EMAIL_RESPONDENT;

@Component
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

    public record DispatchResult(Set<NotificationParties> sentParties, List<NotificationParties> failedParties) {
    }

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsSubject = notificationHelper.getSubjectCommonVars(caseNumber, caseData);
        templateVarsSubject.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        addDashboardLink(templateVarsSubject);

        final NotificationResponse notificationResponse = sendEmailNotification(
            templateVarsSubject,
            cicCase.getEmail(),
            BUNDLE_CREATED_EMAIL_CITIZEN,
            caseNumber,
            Party.SUBJECT
        );

        cicCase.setSubjectNotifyList(notificationResponse);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsApplicant = notificationHelper.getApplicantCommonVars(caseNumber, caseData);
        templateVarsApplicant.put(CommonConstants.CIC_CASE_APPLICANT_NAME, cicCase.getApplicantFullName());
        addDashboardLink(templateVarsApplicant);

        final NotificationResponse notificationResponse;

        notificationResponse = sendEmailNotification(templateVarsApplicant,
        cicCase.getApplicantEmailAddress(), BUNDLE_CREATED_EMAIL_CITIZEN, caseNumber, Party.APPLICANT);

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
        cicCase.getRepresentativeEmailAddress(), BUNDLE_CREATED_EMAIL_CITIZEN, caseNumber, Party.REPRESENTATIVE);

        cicCase.setRepNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRespondent = notificationHelper.getRespondentCommonVars(caseNumber, caseData);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_RESPONDENT_NAME, cicCase.getRespondentName());
        addDashboardLink(templateVarsRespondent);

        final NotificationResponse notificationResponse = sendEmailNotification(
            templateVarsRespondent,
            cicCase.getRespondentEmail(),
            BUNDLE_CREATED_EMAIL_RESPONDENT,
            caseNumber,
            Party.RESPONDENT
        );

        cicCase.setResNotificationResponse(notificationResponse);
    }

    @Override
    public Set<NotificationParties> buildCorrespondenceParties(NotificationContext context) {
        CicCase cicCase = context.getCaseData().getCicCase();

        return getNotificationPartiesBundle(cicCase);
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars, String toEmail, TemplateName templateName,
                                                       String caseReferenceNumber, Party receivingParty) {
        return notificationService.sendEmail(
            notificationHelper.buildEmailNotificationRequest(toEmail,
                templateVars,
                templateName),
            caseReferenceNumber, receivingParty);
    }

    private void addDashboardLink(Map<String, Object> templateVars) {
        if (citizenDashboardEnabled) {
            templateVars.put(DASHBOARD_KEY, citizenDashboardUrl);
        }
    }
}
