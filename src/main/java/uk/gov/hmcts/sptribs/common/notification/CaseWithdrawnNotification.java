package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
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

import static uk.gov.hmcts.sptribs.common.CommonConstants.CLOSURE_INFORMATION;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CLOSURE_REASON;
import static uk.gov.hmcts.sptribs.common.CommonConstants.NONE_PROVIDED;

@Component
@Slf4j
public class CaseWithdrawnNotification implements PartiesNotification {

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    @Autowired
    public CaseWithdrawnNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();

        final Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);
        addCaseClosedTemplateVars(caseData, templateVars);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            final NotificationResponse caseWithdrawnNotifyResponse = sendEmailNotification(cicCase.getEmail(), templateVars);
            cicCase.setSubjectNotifyList(caseWithdrawnNotifyResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            sendLetterNotification(templateVars);
        }
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();

        final Map<String, Object> templateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);
        addCaseClosedTemplateVars(caseData, templateVars);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            final NotificationResponse caseWithdrawnNotifyResponse =
                sendEmailNotification(cicCase.getRepresentativeEmailAddress(), templateVars);
            cicCase.setRepNotificationResponse(caseWithdrawnNotifyResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            sendLetterNotification(templateVars);
        }
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();

        final Map<String, Object> respondentTemplateVars = notificationHelper.getRespondentCommonVars(caseNumber, cicCase);
        addCaseClosedTemplateVars(caseData, respondentTemplateVars);

        final NotificationResponse caseWithdrawnNotifyResponse =
            sendEmailNotification(cicCase.getRespondentEmail(), respondentTemplateVars);
        cicCase.setResNotificationResponse(caseWithdrawnNotifyResponse);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();

        final Map<String, Object> templateVars = notificationHelper.getApplicantCommonVars(caseNumber, cicCase);
        addCaseClosedTemplateVars(caseData, templateVars);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            final NotificationResponse caseWithdrawnNotifyResponse =
                sendEmailNotification(cicCase.getApplicantEmailAddress(), templateVars);
            cicCase.setAppNotificationResponse(caseWithdrawnNotifyResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVars);
            sendLetterNotification(templateVars);
        }
    }

    private NotificationResponse sendEmailNotification(final String destinationAddress, final Map<String, Object> templateVars) {
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            templateVars,
            TemplateName.CASE_WITHDRAWN_EMAIL);
        return notificationService.sendEmail(request);
    }

    private void sendLetterNotification(Map<String, Object> templateVarsLetter) {
        final NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.CASE_WITHDRAWN_POST);
        notificationService.sendLetter(letterRequest);
    }

    private void addCaseClosedTemplateVars(CaseData caseData, Map<String, Object> templateVars) {
        final CloseCase closeCase = caseData.getCloseCase();
        final String additionalDetail = StringUtils.isNotEmpty(closeCase.getAdditionalDetail())
            ? closeCase.getAdditionalDetail() : NONE_PROVIDED;

        templateVars.put(CLOSURE_REASON, closeCase.getCloseCaseReason());
        templateVars.put(CLOSURE_INFORMATION, additionalDetail);
    }
}
