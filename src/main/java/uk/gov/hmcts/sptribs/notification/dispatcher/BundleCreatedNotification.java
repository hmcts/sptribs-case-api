package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseSubcategory;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.APPLICANT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.SUBJECT;
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

    @Value("${uk.gov.notify.email.templateVars.respondentEmail}")
    private String configuredRespondentEmail;

    @Autowired
    public BundleCreatedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    public DispatchResult dispatch(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Set<PartiesCIC> parties = cicCase.getPartiesCIC();
        Set<NotificationParties> sentParties = new HashSet<>();
        List<NotificationParties> failedParties = new ArrayList<>();

        if (cicCase.getCaseSubcategory() != CaseSubcategory.FATAL
            && cicCase.getCaseSubcategory() != CaseSubcategory.MINOR
            && StringUtils.hasText(cicCase.getEmail())) {
            attemptSend(() -> sendToSubject(caseData, caseNumber), SUBJECT, caseNumber, sentParties, failedParties);
        }

        attemptSend(() -> sendToRespondent(caseData, caseNumber), RESPONDENT, caseNumber, sentParties, failedParties);

        if (parties != null && parties.contains(PartiesCIC.REPRESENTATIVE)
            && StringUtils.hasText(cicCase.getRepresentativeEmailAddress())) {
            attemptSend(() -> sendToRepresentative(caseData, caseNumber), REPRESENTATIVE, caseNumber, sentParties, failedParties);
        }

        if (parties != null && parties.contains(PartiesCIC.APPLICANT)
            && StringUtils.hasText(cicCase.getApplicantEmailAddress())) {
            attemptSend(() -> sendToApplicant(caseData, caseNumber), APPLICANT, caseNumber, sentParties, failedParties);
        }

        return new DispatchResult(Set.copyOf(sentParties), List.copyOf(failedParties));
    }

    private void attemptSend(Runnable send, NotificationParties party, String caseNumber,
                             Set<NotificationParties> sentParties, List<NotificationParties> failedParties) {
        try {
            send.run();
            sentParties.add(party);
        } catch (Exception notificationException) {
            log.error("Failed to send bundle created notification to {} for case {}",
                party.getLabel(), caseNumber, notificationException);
            failedParties.add(party);
        }
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
            caseNumber
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

        final NotificationResponse notificationResponse = sendEmailNotification(
            templateVarsRespondent,
            configuredRespondentEmail,
            BUNDLE_CREATED_EMAIL_RESPONDENT,
            caseNumber
        );

        cicCase.setResNotificationResponse(notificationResponse);
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
