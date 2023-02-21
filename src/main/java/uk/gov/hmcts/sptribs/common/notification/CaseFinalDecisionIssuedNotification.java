package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
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
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.FINAL_DECISION_GUIDANCE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.FINAL_DECISION_NOTICE;

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
            Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            notificationResponse = sendEmailNotificationWithAttachment(templateVarsSubject,
                cicCase.getEmail(),
                uploadedDocuments,
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
        final Map<String, Object> templateVarsRepresentative = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);

        NotificationResponse notificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            notificationResponse = sendEmailNotificationWithAttachment(templateVarsRepresentative,
                cicCase.getRepresentativeEmailAddress(),
                uploadedDocuments,
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

        Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);

        NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(templateVarsRespondent,
            caseData.getCicCase().getRespondentEmail(),
            uploadedDocuments,
            TemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
        cicCase.setResNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotificationWithAttachment(final Map<String, Object> templateVars,
                                                                     String toEmail,
                                                                     Map<String, String> uploadedDocuments,
                                                                     TemplateName emailTemplateName) {
        NotificationRequest request = notificationHelper.buildEmailNotificationRequest(toEmail,
            true,
            uploadedDocuments,
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

    private Map<String, String> getUploadedDocuments(CaseData caseData) {
        CaseIssueFinalDecision caseIssueFinalDecision = caseData.getCaseIssueFinalDecision();

        Map<String, String> uploadedDocuments = new HashMap<>();

        String finalDecisionNotice = getFinalDecisionNoticeDocument(caseIssueFinalDecision);
        uploadedDocuments.put(FINAL_DECISION_NOTICE, finalDecisionNotice);

        String finalDecisionGuidance = StringUtils.substringAfterLast(caseIssueFinalDecision.getFinalDecisionGuidance().getUrl(), "/");
        uploadedDocuments.put(FINAL_DECISION_GUIDANCE, finalDecisionGuidance);

        return uploadedDocuments;
    }

    private String getFinalDecisionNoticeDocument(CaseIssueFinalDecision caseIssueFinalDecision) {
        String finalDecisionNotice = null;
        if (caseIssueFinalDecision.getFinalDecisionNotice().equals(NoticeOption.UPLOAD_FROM_COMPUTER)
            && !CollectionUtils.isEmpty(caseIssueFinalDecision.getDocuments())) {
            List<String> uploadedDecisionNoticeDocs = caseIssueFinalDecision.getDocuments().stream().map(ListValue::getValue)
                .map(item -> StringUtils.substringAfterLast(item.getDocumentLink().getUrl(), "/"))
                .toList();
            finalDecisionNotice = uploadedDecisionNoticeDocs.get(0);
        } else if (caseIssueFinalDecision.getFinalDecisionNotice().equals(NoticeOption.CREATE_FROM_TEMPLATE)
            && null != caseIssueFinalDecision.getFinalDecisionDraft()) {
            finalDecisionNotice = StringUtils.substringAfterLast(caseIssueFinalDecision.getFinalDecisionDraft().getUrl(), "/");
        }

        return finalDecisionNotice;
    }

}
