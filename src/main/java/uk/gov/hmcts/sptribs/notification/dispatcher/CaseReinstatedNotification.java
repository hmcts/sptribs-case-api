package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;

import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.REINSTATE_REASON;

@Component
@Slf4j
public class CaseReinstatedNotification extends PartiesNotification {

    public CaseReinstatedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    protected PartyNotification buildSubjectNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getSubjectCommonVars(caseNumber, caseData);
        addCaseReInstateTemplateVars(cicCase, templateVars);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            return PartiesNotification.emailOnly(cicCase.getEmail(), templateVars, TemplateName.REINSTATED_EMAIL, saveToCicCase(CicCase::setSubjectNotifyList));
        } else {
            return new LetterNotification(cicCase.getAddress(), templateVars, TemplateName.REINSTATED_POST, saveToCicCase(CicCase::setSubjectLetterNotifyList));
        }
    }

    @Override
    protected PartyNotification buildApplicantNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getApplicantCommonVars(caseNumber, caseData);
        addCaseReInstateTemplateVars(cicCase, templateVars);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            return PartiesNotification.emailOnly(cicCase.getApplicantEmailAddress(), templateVars, TemplateName.REINSTATED_EMAIL, saveToCicCase(CicCase::setAppNotificationResponse));
        } else {
            return new LetterNotification(cicCase.getApplicantAddress(), templateVars, TemplateName.REINSTATED_POST, saveToCicCase(CicCase::setAppLetterNotificationResponse));
        }
    }

    @Override
    protected PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getRepresentativeCommonVars(caseNumber, caseData);
        addCaseReInstateTemplateVars(cicCase, templateVars);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            return PartiesNotification.emailOnly(cicCase.getRepresentativeEmailAddress(), templateVars, TemplateName.REINSTATED_EMAIL, saveToCicCase(CicCase::setRepNotificationResponse));
        } else {
            return new LetterNotification(cicCase.getRepresentativeAddress(), templateVars, TemplateName.REINSTATED_POST, saveToCicCase(CicCase::setRepLetterNotificationResponse));
        }
    }

    @Override
    protected PartyNotification buildRespondentNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getRespondentCommonVars(caseNumber, caseData);
        addCaseReInstateTemplateVars(cicCase, templateVars);

        return PartiesNotification.emailOnly(cicCase.getRespondentEmail(), templateVars, TemplateName.REINSTATED_EMAIL, saveToCicCase(CicCase::setResNotificationResponse));
    }

    private void addCaseReInstateTemplateVars(CicCase cicCase, Map<String, Object> templateVars) {
        templateVars.put(REINSTATE_REASON, cicCase.getReinstateReason().getLabel());
    }
}
