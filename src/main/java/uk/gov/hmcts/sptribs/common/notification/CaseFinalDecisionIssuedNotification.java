package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.caseworker.model.NoticeOption;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.FINAL_DECISION_GUIDANCE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.FINAL_DECISION_NOTICE;

@Component
@Slf4j
public class CaseFinalDecisionIssuedNotification implements PartiesNotification {

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    @Autowired
    public CaseFinalDecisionIssuedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsSubject = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);
        final NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            final Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            notificationResponse = sendEmailNotificationWithAttachment(templateVarsSubject,
                cicCase.getEmail(),
                uploadedDocuments);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVarsSubject);
            notificationResponse = sendLetterNotification(templateVarsSubject);
        }

        cicCase.setSubjectNotifyList(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);

        NotificationResponse notificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            final Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            notificationResponse = sendEmailNotificationWithAttachment(templateVarsRepresentative,
                cicCase.getRepresentativeEmailAddress(),
                uploadedDocuments);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVarsRepresentative);
            notificationResponse = sendLetterNotification(templateVarsRepresentative);
        }

        cicCase.setRepNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        final Map<String, Object> templateVarsRespondent = notificationHelper.getRespondentCommonVars(caseNumber, caseData.getCicCase());
        final CicCase cicCase = caseData.getCicCase();

        final Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);

        final NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(templateVarsRespondent,
            caseData.getCicCase().getRespondentEmail(),
            uploadedDocuments);
        cicCase.setResNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVars = notificationHelper.getApplicantCommonVars(caseNumber, cicCase);

        final NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            final Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            notificationResponse = sendEmailNotificationWithAttachment(templateVars,
                cicCase.getApplicantEmailAddress(),
                uploadedDocuments);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars);
        }

        cicCase.setSubjectNotifyList(notificationResponse);
    }

    private NotificationResponse sendEmailNotificationWithAttachment(final Map<String, Object> templateVars,
                                                                     String toEmail,
                                                                     Map<String, String> uploadedDocuments) {
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(toEmail,
            true,
            uploadedDocuments,
            templateVars,
            TemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
        return notificationService.sendEmail(request);
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter) {
        final NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.CASE_FINAL_DECISION_ISSUED_POST);
        return notificationService.sendLetter(letterRequest);
    }

    private Map<String, String> getUploadedDocuments(CaseData caseData) {
        final CaseIssueFinalDecision caseIssueFinalDecision = caseData.getCaseIssueFinalDecision();

        final Map<String, String> uploadedDocuments = new HashMap<>();

        final String finalDecisionNotice = getFinalDecisionNoticeDocument(caseIssueFinalDecision);
        uploadedDocuments.put(FINAL_DECISION_NOTICE, finalDecisionNotice);

        final String finalDecisionGuidance = StringUtils.substringAfterLast(caseIssueFinalDecision
            .getFinalDecisionGuidance().getUrl(), "/");
        uploadedDocuments.put(FINAL_DECISION_GUIDANCE, finalDecisionGuidance);

        return uploadedDocuments;
    }

    private String getFinalDecisionNoticeDocument(CaseIssueFinalDecision caseIssueFinalDecision) {
        String finalDecisionNotice = EMPTY_PLACEHOLDER;
        if (caseIssueFinalDecision.getFinalDecisionNotice() == NoticeOption.UPLOAD_FROM_COMPUTER
            && ObjectUtils.isNotEmpty(caseIssueFinalDecision.getDocument())) {
            finalDecisionNotice = StringUtils.substringAfterLast(caseIssueFinalDecision
                .getDocument().getDocumentLink().getUrl(), "/");
        } else if (caseIssueFinalDecision.getFinalDecisionNotice() == NoticeOption.CREATE_FROM_TEMPLATE
            && caseIssueFinalDecision.getFinalDecisionDraft() != null) {
            finalDecisionNotice = StringUtils.substringAfterLast(caseIssueFinalDecision.getFinalDecisionDraft().getUrl(), "/");
        }

        return finalDecisionNotice;
    }
}
