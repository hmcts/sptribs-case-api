package uk.gov.hmcts.sptribs.notification.dispatcher;

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

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    @Autowired
    public CaseStayedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final CaseStay caseStay = caseData.getCaseStay();

        final Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, caseData);
        addCaseStayTemplateVars(caseStay, templateVars);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            NotificationResponse response = sendEmailNotification(cicCase.getEmail(), templateVars, caseNumber);
            cicCase.setSubjectNotifyList(response);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            sendLetterNotification(templateVars, caseNumber);
        }
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final CaseStay caseStay = caseData.getCaseStay();

        final Map<String, Object> templateVars = notificationHelper.getApplicantCommonVars(caseNumber, caseData);
        addCaseStayTemplateVars(caseStay, templateVars);

        if (cicCase.getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            NotificationResponse response = sendEmailNotification(cicCase.getApplicantEmailAddress(), templateVars, caseNumber);
            cicCase.setAppNotificationResponse(response);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVars);
            sendLetterNotification(templateVars, caseNumber);
        }
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final CaseStay caseStay = caseData.getCaseStay();

        final Map<String, Object> templateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);
        addCaseStayTemplateVars(caseStay, templateVars);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            NotificationResponse response = sendEmailNotification(cicCase.getRepresentativeEmailAddress(), templateVars, caseNumber);
            cicCase.setRepNotificationResponse(response);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            sendLetterNotification(templateVars, caseNumber);
        }
    }

    private NotificationResponse sendEmailNotification(final String destinationAddress,
                                                       final Map<String, Object> templateVars,
                                                       String caseReferenceNumber) {
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            templateVars,
            TemplateName.CASE_STAYED_EMAIL);
        return notificationService.sendEmail(request, caseReferenceNumber);
    }

    private void sendLetterNotification(Map<String, Object> templateVarsLetter, String caseReferenceNumber) {
        final NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.CASE_STAYED_POST);
        notificationService.sendLetter(letterRequest, caseReferenceNumber);
    }

    private void addCaseStayTemplateVars(CaseStay caseStay, Map<String, Object> templateVars) {
        final String additionalDetail = StringUtils.isNotEmpty(caseStay.getAdditionalDetail())
            ? caseStay.getAdditionalDetail() : NONE_PROVIDED;

        templateVars.put(STAY_EXPIRATION_DATE, caseStay.getExpirationDate());
        templateVars.put(STAY_REASON, caseStay.getStayReason().getLabel());
        templateVars.put(STAY_ADDITIONAL_DETAIL, additionalDetail);
    }
}
