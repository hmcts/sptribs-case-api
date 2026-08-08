package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.EmailBundleCreatedResponses;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;

import java.util.Map;

@Component
@Slf4j
public class BundleCreatedNotification extends PartiesNotification {

    public BundleCreatedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    protected PartyNotification buildApplicantNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsApplicant = notificationHelper().getApplicantCommonVars(caseNumber, caseData);
        templateVarsApplicant.put(CommonConstants.CIC_CASE_APPLICANT_NAME, cicCase.getApplicantFullName());

        templateVarsApplicant.put(CommonConstants.BUNDLE_CREATED_EMAIL_TEXT,
            EmailBundleCreatedResponses.REPRESENTATIVE_APPLICANT_RESPONSE.getEmailResponse());

        return PartiesNotification.emailOnly(cicCase.getApplicantEmailAddress(), templateVarsApplicant, TemplateName.BUNDLE_CREATED_EMAIL,
            saveToCicCase(CicCase::setAppNotificationResponse));
    }

    @Override
    protected PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsRepresentative = notificationHelper().getRepresentativeCommonVars(caseNumber, caseData);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, cicCase.getRepresentativeFullName());

        templateVarsRepresentative.put(CommonConstants.BUNDLE_CREATED_EMAIL_TEXT,
            EmailBundleCreatedResponses.REPRESENTATIVE_APPLICANT_RESPONSE.getEmailResponse());

        return PartiesNotification.emailOnly(cicCase.getRepresentativeEmailAddress(), templateVarsRepresentative,
            TemplateName.BUNDLE_CREATED_EMAIL, saveToCicCase(CicCase::setRepNotificationResponse));
    }

    @Override
    protected PartyNotification buildRespondentNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsRespondent = notificationHelper().getRespondentCommonVars(caseNumber, caseData);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_RESPONDENT_NAME, cicCase.getRespondentName());

        templateVarsRespondent.put(CommonConstants.BUNDLE_CREATED_EMAIL_TEXT,
            EmailBundleCreatedResponses.RESPONDENT_RESPONSE.getEmailResponse());

        return PartiesNotification.emailOnly(cicCase.getRespondentEmail(), templateVarsRespondent, TemplateName.BUNDLE_CREATED_EMAIL,
            saveToCicCase(CicCase::setResNotificationResponse));
    }
}
