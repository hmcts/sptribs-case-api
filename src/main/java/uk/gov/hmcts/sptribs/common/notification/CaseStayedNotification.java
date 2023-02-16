package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.CaseStay;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.NONE_PROVIDED;
import static uk.gov.hmcts.sptribs.common.CommonConstants.STAY_ADDITIONAL_DETAIL;
import static uk.gov.hmcts.sptribs.common.CommonConstants.STAY_EXPIRATION_DATE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.STAY_REASON;

@Component
@Slf4j
public class CaseStayedNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    @Autowired
    private NotificationHelper notificationHelper;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        CaseStay caseStay = caseData.getCaseStay();

        Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);
        addCaseStayTemplateVars(caseStay, templateVars);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            NotificationResponse response = sendEmailNotification(cicCase.getEmail(), templateVars);
            cicCase.setSubjectNotifyList(response);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            sendLetterNotification(templateVars);
        }
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        CaseStay caseStay = caseData.getCaseStay();

        Map<String, Object> templateVars = notificationHelper.getApplicantCommonVars(caseNumber, cicCase);
        addCaseStayTemplateVars(caseStay, templateVars);

        if (cicCase.getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            NotificationResponse response = sendEmailNotification(cicCase.getApplicantEmailAddress(), templateVars);
            cicCase.setAppNotificationResponse(response);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVars);
            sendLetterNotification(templateVars);
        }
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        CaseStay caseStay = caseData.getCaseStay();

        Map<String, Object> templateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);
        addCaseStayTemplateVars(caseStay, templateVars);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            NotificationResponse response = sendEmailNotification(cicCase.getRepresentativeEmailAddress(), templateVars);
            cicCase.setRepNotificationResponse(response);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            sendLetterNotification(templateVars);
        }
    }

    private NotificationResponse sendEmailNotification(final String destinationAddress, final Map<String, Object> templateVars) {
        NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            templateVars,
            TemplateName.CASE_STAYED_EMAIL);
        notificationService.setNotificationRequest(request);
        return notificationService.sendEmail();
    }

    private void sendLetterNotification(Map<String, Object> templateVarsLetter) {
        NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.CASE_STAYED_POST);
        notificationService.setNotificationRequest(letterRequest);
        notificationService.sendLetter();
    }

    private void addCaseStayTemplateVars(CaseStay caseStay, Map<String, Object> templateVars) {
        String additionalDetail = StringUtils.isNotEmpty(caseStay.getAdditionalDetail())
            ? caseStay.getAdditionalDetail() : NONE_PROVIDED;

        templateVars.put(STAY_EXPIRATION_DATE, caseStay.getExpirationDate());
        templateVars.put(STAY_REASON, caseStay.getStayReason().getLabel());
        templateVars.put(STAY_ADDITIONAL_DETAIL, additionalDetail);
    }

}
