package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationContextRequest;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.sptribs.caseworker.model.CloseReason.DeathOfAppellant;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.APPLICANT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.SUBJECT;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CLOSURE_INFORMATION;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CLOSURE_REASON;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DEATH_OF_APPELLANT_EMAIL_CONTENT;
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

        final Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, caseData);
        addCaseClosedTemplateVars(caseData, templateVars);

        if (ContactPreferenceType.EMAIL.equals(cicCase.getContactPreferenceType())) {
            final NotificationResponse caseWithdrawnNotifyResponse = sendEmailNotification(cicCase.getEmail(), templateVars, caseNumber);
            cicCase.setSubjectNotifyList(caseWithdrawnNotifyResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            sendLetterNotification(templateVars, caseNumber);
        }
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();

        final Map<String, Object> templateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);
        addCaseClosedTemplateVars(caseData, templateVars);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            final NotificationResponse caseWithdrawnNotifyResponse =
                sendEmailNotification(cicCase.getRepresentativeEmailAddress(), templateVars, caseNumber);
            cicCase.setRepNotificationResponse(caseWithdrawnNotifyResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            sendLetterNotification(templateVars, caseNumber);
        }
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();

        final Map<String, Object> respondentTemplateVars = notificationHelper.getRespondentCommonVars(caseNumber, caseData);
        addCaseClosedTemplateVars(caseData, respondentTemplateVars);

        final NotificationResponse caseWithdrawnNotifyResponse =
            sendEmailNotification(cicCase.getRespondentEmail(), respondentTemplateVars, caseNumber);
        cicCase.setResNotificationResponse(caseWithdrawnNotifyResponse);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();

        final Map<String, Object> templateVars = notificationHelper.getApplicantCommonVars(caseNumber, caseData);
        addCaseClosedTemplateVars(caseData, templateVars);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            final NotificationResponse caseWithdrawnNotifyResponse =
                sendEmailNotification(cicCase.getApplicantEmailAddress(), templateVars, caseNumber);
            cicCase.setAppNotificationResponse(caseWithdrawnNotifyResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVars);
            sendLetterNotification(templateVars, caseNumber);
        }
    }

    private NotificationResponse sendEmailNotification(final String destinationAddress,
                                                       final Map<String, Object> templateVars,
                                                       String caseReferenceNumber) {
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            templateVars,
            TemplateName.CASE_WITHDRAWN_EMAIL);
        return notificationService.sendEmail(request, caseReferenceNumber, null);
    }

    private void sendLetterNotification(Map<String, Object> templateVarsLetter, String caseReferenceNumber) {
        final NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.CASE_WITHDRAWN_POST);
        notificationService.sendLetter(letterRequest, caseReferenceNumber);
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

    @Override
    public Set<NotificationParties> buildCorrespondenceParties(NotificationContextRequest request) {
        Set<NotificationParties> correspondenceParties = new HashSet<>();
        CicCase cicCase = request.getCaseData().getCicCase();

        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())) {
            correspondenceParties.add(SUBJECT);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent())) {
            correspondenceParties.add(RESPONDENT);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())) {
            correspondenceParties.add(REPRESENTATIVE);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyApplicant())) {
            correspondenceParties.add(APPLICANT);
        }
        return correspondenceParties;
    }
}
