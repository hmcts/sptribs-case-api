package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.DECISION_NOTICE;

@Component
@Slf4j
public class DecisionIssuedNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    @Autowired
    private NotificationHelper notificationHelper;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);
        addDecisionsIssuedTemplateVars(caseData.getCaseIssueDecision(), templateVars);

        NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType().isEmail()) {
            notificationResponse = sendEmailNotification(cicCase.getEmail(), templateVars);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars);
        }

        cicCase.setSubjectNotifyList(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> templateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);
        addDecisionsIssuedTemplateVars(caseData.getCaseIssueDecision(), templateVars);

        NotificationResponse notificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference().isEmail()) {
            notificationResponse = sendEmailNotification(cicCase.getRepresentativeEmailAddress(), templateVars);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars);
        }

        cicCase.setRepNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> templateVars = notificationHelper.getRespondentCommonVars(caseNumber, cicCase);
        addDecisionsIssuedTemplateVars(caseData.getCaseIssueDecision(), templateVars);

        NotificationResponse notificationResponse = sendEmailNotification(cicCase.getRespondantEmail(), templateVars);
        cicCase.setAppNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotification(final String destinationAddress, final Map<String, Object> templateVars) {
        NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            templateVars,
            TemplateName.DECISION_ISSUED_EMAIL);
        notificationService.setNotificationRequest(request);
        return notificationService.sendEmail();
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter) {
        NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.DECISION_ISSUED_POST);
        notificationService.setNotificationRequest(letterRequest);
        return notificationService.sendLetter();
    }

    private void addDecisionsIssuedTemplateVars(CaseIssueDecision caseIssueDecision, Map<String, Object> templateVars) {
        //templateVars.put(DECISION_NOTICE, caseIssueDecision.getDecisionNotice());
    }
}
