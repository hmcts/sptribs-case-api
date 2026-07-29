package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.CaseStay;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;

import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.NONE_PROVIDED;
import static uk.gov.hmcts.sptribs.common.CommonConstants.STAY_ADDITIONAL_DETAIL;
import static uk.gov.hmcts.sptribs.common.CommonConstants.STAY_EXPIRATION_DATE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.STAY_REASON;

@Component
@Slf4j
public class CaseStayedNotification extends PartiesNotification {

    public CaseStayedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    protected PartyNotification buildSubjectNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        CaseStay caseStay = caseData.getCaseStay();
        Map<String, Object> templateVars = notificationHelper().getSubjectCommonVars(caseNumber, caseData);
        addCaseStayTemplateVars(caseStay, templateVars);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            return PartiesNotification.emailOnly(cicCase.getEmail(), templateVars, TemplateName.CASE_STAYED_EMAIL, saveToCicCase(CicCase::setSubjectNotifyList));
        } else {
            return new LetterNotification(cicCase.getAddress(), templateVars, TemplateName.CASE_STAYED_POST, saveToCicCase(CicCase::setSubjectLetterNotifyList));
        }
    }

    @Override
    protected PartyNotification buildApplicantNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        CaseStay caseStay = caseData.getCaseStay();
        Map<String, Object> templateVars = notificationHelper().getApplicantCommonVars(caseNumber, caseData);
        addCaseStayTemplateVars(caseStay, templateVars);

        if (cicCase.getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            return PartiesNotification.emailOnly(cicCase.getApplicantEmailAddress(), templateVars, TemplateName.CASE_STAYED_EMAIL, saveToCicCase(CicCase::setAppNotificationResponse));
        } else {
            return new LetterNotification(cicCase.getApplicantAddress(), templateVars, TemplateName.CASE_STAYED_POST, saveToCicCase(CicCase::setAppLetterNotificationResponse));
        }
    }

    @Override
    protected PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        CaseStay caseStay = caseData.getCaseStay();
        Map<String, Object> templateVars = notificationHelper().getRepresentativeCommonVars(caseNumber, caseData);
        addCaseStayTemplateVars(caseStay, templateVars);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            return PartiesNotification.emailOnly(cicCase.getRepresentativeEmailAddress(), templateVars, TemplateName.CASE_STAYED_EMAIL, saveToCicCase(CicCase::setRepNotificationResponse));
        } else {
            return new LetterNotification(cicCase.getRepresentativeAddress(), templateVars, TemplateName.CASE_STAYED_POST, saveToCicCase(CicCase::setRepLetterNotificationResponse));
        }
    }

    private void addCaseStayTemplateVars(CaseStay caseStay, Map<String, Object> templateVars) {
        final String additionalDetail = StringUtils.isNotEmpty(caseStay.getAdditionalDetail())
            ? caseStay.getAdditionalDetail() : NONE_PROVIDED;

        templateVars.put(STAY_EXPIRATION_DATE, caseStay.getExpirationDate());
        templateVars.put(STAY_REASON, caseStay.getStayReason().getLabel());
        templateVars.put(STAY_ADDITIONAL_DETAIL, additionalDetail);
    }
}
