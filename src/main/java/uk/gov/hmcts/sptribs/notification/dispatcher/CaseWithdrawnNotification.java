package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;

import java.util.Map;

import static uk.gov.hmcts.sptribs.caseworker.model.CloseReason.DeathOfAppellant;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CLOSURE_INFORMATION;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CLOSURE_REASON;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DEATH_OF_APPELLANT_EMAIL_CONTENT;
import static uk.gov.hmcts.sptribs.common.CommonConstants.NONE_PROVIDED;

@Component
@Slf4j
public class CaseWithdrawnNotification extends PartiesNotification {

    public CaseWithdrawnNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    protected PartyNotification buildSubjectNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> templateVars = notificationHelper().getSubjectCommonVars(caseNumber, caseData);
        addCaseClosedTemplateVars(caseData, templateVars);

        if (ContactPreferenceType.EMAIL.equals(cicCase.getContactPreferenceType())) {
            return emailOnly(cicCase.getEmail(), templateVars, TemplateName.CASE_WITHDRAWN_EMAIL, saveToCicCase(CicCase::setSubjectNotifyList));
        } else {
            notificationHelper().addAddressTemplateVars(cicCase.getAddress(), templateVars);
            return new LetterNotification(cicCase.getAddress(), templateVars, TemplateName.CASE_WITHDRAWN_POST, saveToCicCase(CicCase::setSubjectLetterNotifyList));
        }
    }

    @Override
    protected PartyNotification buildApplicantNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getApplicantCommonVars(caseNumber, caseData);
        addCaseClosedTemplateVars(caseData, templateVars);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            return emailOnly(cicCase.getApplicantEmailAddress(), templateVars, TemplateName.CASE_WITHDRAWN_EMAIL, saveToCicCase(CicCase::setAppNotificationResponse));
        } else {
            notificationHelper().addAddressTemplateVars(cicCase.getApplicantAddress(), templateVars);
            return new LetterNotification(cicCase.getApplicantAddress(), templateVars, TemplateName.CASE_WITHDRAWN_POST, saveToCicCase(CicCase::setAppLetterNotificationResponse));
        }
    }

    @Override
    protected PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getRepresentativeCommonVars(caseNumber, caseData);
        addCaseClosedTemplateVars(caseData, templateVars);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            return emailOnly(cicCase.getRepresentativeEmailAddress(), templateVars, TemplateName.CASE_WITHDRAWN_EMAIL, saveToCicCase(CicCase::setRepNotificationResponse));
        } else {
            notificationHelper().addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            return new LetterNotification(cicCase.getRepresentativeAddress(), templateVars, TemplateName.CASE_WITHDRAWN_POST, saveToCicCase(CicCase::setRepLetterNotificationResponse));
        }
    }

    @Override
    protected PartyNotification buildRespondentNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> respondentTemplateVars = notificationHelper().getRespondentCommonVars(caseNumber, caseData);
        addCaseClosedTemplateVars(caseData, respondentTemplateVars);

        return emailOnly(cicCase.getRespondentEmail(), respondentTemplateVars, TemplateName.CASE_WITHDRAWN_EMAIL, saveToCicCase(CicCase::setResNotificationResponse));
    }

    private void addCaseClosedTemplateVars(CaseData caseData, Map<String, Object> templateVars) {
        final CloseCase closeCase = caseData.getCloseCase();
        final String additionalDetail = StringUtils.isNotEmpty(closeCase.getAdditionalDetail())
            ? closeCase.getAdditionalDetail() : NONE_PROVIDED;

        if (DeathOfAppellant.equals(closeCase.getCloseCaseReason())) {
            templateVars.put(CLOSURE_REASON, DEATH_OF_APPELLANT_EMAIL_CONTENT);
        } else {
            templateVars.put(CLOSURE_REASON, closeCase.getCloseCaseReason());
        }
        templateVars.put(CLOSURE_INFORMATION, additionalDetail);
    }
}
