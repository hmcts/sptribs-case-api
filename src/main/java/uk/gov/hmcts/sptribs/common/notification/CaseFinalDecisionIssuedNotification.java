package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CaseFinalDecisionIssuedNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    @Autowired
    private NotificationHelper notificationHelper;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsSubject = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);

        NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            List<String> uploadedDocumentIds = getUploadedDocumentIds(caseData);
            notificationResponse = sendEmailNotificationWithAttachment(templateVarsSubject,
                cicCase.getEmail(),
                uploadedDocumentIds,
                TemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVarsSubject);
            notificationResponse = sendLetterNotification(templateVarsSubject,
                TemplateName.CASE_FINAL_DECISION_ISSUED_POST);
        }

        cicCase.setSubjectNotifyList(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative  = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);

        NotificationResponse notificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            List<String> uploadedDocumentIds = getUploadedDocumentIds(caseData);
            notificationResponse = sendEmailNotificationWithAttachment(templateVarsRepresentative,
                cicCase.getRepresentativeEmailAddress(),
                uploadedDocumentIds,
                TemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVarsRepresentative);
            notificationResponse = sendLetterNotification(templateVarsRepresentative,
                TemplateName.CASE_FINAL_DECISION_ISSUED_POST);
        }

        cicCase.setRepNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        final Map<String, Object> templateVarsRespondent = notificationHelper.getRespondentCommonVars(caseNumber, caseData.getCicCase());
        CicCase cicCase = caseData.getCicCase();

        List<String> uploadedDocumentIds = getUploadedDocumentIds(caseData);

        NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(templateVarsRespondent,
            caseData.getCicCase().getRespondentEmail(),
            uploadedDocumentIds,
            TemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
        cicCase.setResNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotificationWithAttachment(final Map<String, Object> templateVars,
                                                                     String toEmail,
                                                                     List<String> uploadedDocumentIds,
                                                                     TemplateName emailTemplateName) {
        NotificationRequest request = notificationHelper.buildEmailNotificationRequest(toEmail,
            true,
            uploadedDocumentIds,
            templateVars,
            emailTemplateName);
        notificationService.setNotificationRequest(request);
        return notificationService.sendEmail();
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter, TemplateName letterTemplateName) {
        NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(templateVarsLetter, letterTemplateName);
        notificationService.setNotificationRequest(letterRequest);
        return notificationService.sendLetter();
    }

    private List<String> getUploadedDocumentIds(CaseData caseData) {
        CaseIssueFinalDecision caseIssueFinalDecision = caseData.getCaseIssueFinalDecision();

        List<String> uploadedDocumentIds = new ArrayList<>();

        String finalDecisionNotice = getFinalDecisionNoticeDocument(caseIssueFinalDecision);
        uploadedDocumentIds.add(finalDecisionNotice);

        String finalDecisionGuidance = StringUtils.substringAfterLast(caseIssueFinalDecision.getFinalDecisionGuidance().getUrl(), "/");
        uploadedDocumentIds.add(finalDecisionGuidance);

        return uploadedDocumentIds;
    }

    private String getFinalDecisionNoticeDocument(CaseIssueFinalDecision caseIssueFinalDecision) {
        String finalDecisionNotice;
        if (null != caseIssueFinalDecision.getDocuments() && !caseIssueFinalDecision.getDocuments().isEmpty()) {
            List<String> uploadedDecisionNoticeDocs = caseIssueFinalDecision.getDocuments().stream().map(ListValue::getValue)
                .map(item -> StringUtils.substringAfterLast(item.getDocumentLink().getUrl(), "/"))
                .toList();
            finalDecisionNotice = uploadedDecisionNoticeDocs.get(0);

        } else {
            finalDecisionNotice = StringUtils.substringAfterLast(caseIssueFinalDecision.getFinalDecisionDraft().getUrl(), "/");
        }

        return finalDecisionNotice;
    }

}
