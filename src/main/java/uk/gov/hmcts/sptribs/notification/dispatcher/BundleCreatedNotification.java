package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;

import java.util.Map;

@Component
@Slf4j
public class BundleCreatedNotification implements PartiesNotification {

    private static final String YES = "yes";
    private static final String NO = "no";

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

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

        //NEED TO ADD CORRECT DASHBOARD LINK
        templateVarsApplicant.put(CommonConstants.DASHBOARD_LINK, "Dashboard Link");

        final NotificationResponse notificationResponse;

        notificationResponse = sendEmailNotification(templateVarsApplicant,
        cicCase.getApplicantEmailAddress(), TemplateName.BUNDLE_CREATED_EMAIL, caseNumber);

        cicCase.setAppNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, cicCase.getRepresentativeFullName());

        //NEED TO ADD CORRECT DASHBOARD LINK
        templateVarsRepresentative.put(CommonConstants.DASHBOARD_LINK, "Dashboard Link");

        final NotificationResponse notificationResponse;

        notificationResponse = sendEmailNotification(templateVarsRepresentative,
        cicCase.getRepresentativeEmailAddress(), TemplateName.BUNDLE_CREATED_EMAIL, caseNumber);

        cicCase.setRepNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars, String toEmail,
                                                       TemplateName emailTemplateName, String caseReferenceNumber) {
        return notificationService.sendEmail(
            notificationHelper.buildEmailNotificationRequest(toEmail,
                templateVars,
                emailTemplateName),
            caseReferenceNumber);
    }
}
