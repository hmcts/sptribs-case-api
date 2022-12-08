package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.CaseStay;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.EmailTemplateName;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
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

        if (cicCase.getContactPreferenceType().isEmail()) {
            Map<String, Object> templateVars = notificationHelper.commonTemplateVars(cicCase, caseNumber);
            templateVars.put(CONTACT_NAME, cicCase.getFullName());
            addCaseStayTemplateVars(caseStay, templateVars);

            // Send Email
            NotificationResponse notificationResponse = sendEmailNotification(cicCase.getEmail(), templateVars);
            cicCase.setSubjectNotifyList(notificationResponse);
        }
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        CaseStay caseStay = caseData.getCaseStay();

        if (cicCase.getApplicantContactDetailsPreference().isEmail()) {
            Map<String, Object> templateVars = notificationHelper.commonTemplateVars(cicCase, caseNumber);
            addCaseStayTemplateVars(caseStay, templateVars);
            templateVars.put(CONTACT_NAME, cicCase.getApplicantFullName());

            // Send Email
            NotificationResponse notificationResponse = sendEmailNotification(cicCase.getApplicantEmailAddress(), templateVars);
            cicCase.setAppNotificationResponse(notificationResponse);
        }
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        CaseStay caseStay = caseData.getCaseStay();

        if (cicCase.getRepresentativeContactDetailsPreference().isEmail()) {
            Map<String, Object> templateVars = notificationHelper.commonTemplateVars(cicCase, caseNumber);
            templateVars.put(CONTACT_NAME, cicCase.getRepresentativeFullName());
            addCaseStayTemplateVars(caseStay, templateVars);

            // Send Email
            NotificationResponse notificationResponse = sendEmailNotification(cicCase.getRepresentativeEmailAddress(), templateVars);
            cicCase.setRepNotificationResponse(notificationResponse);
        }
    }

    private NotificationResponse sendEmailNotification(final String destinationAddress, final Map<String, Object> templateVars) {
        NotificationRequest request = NotificationRequest.builder()
            .destinationAddress(destinationAddress)
            .template(EmailTemplateName.CASE_STAYED)
            .templateVars(templateVars)
            .build();

        notificationService.setNotificationRequest(request);
        return notificationService.sendEmail();
    }

    private void addCaseStayTemplateVars(CaseStay caseStay, Map<String, Object> templateVars) {
        templateVars.put(STAY_EXPIRATION_DATE, caseStay.getExpirationDate());
        templateVars.put(STAY_REASON, caseStay.getStayReason().getLabel());
        templateVars.put(STAY_ADDITIONAL_DETAIL, caseStay.getAdditionalDetail());
    }

}
