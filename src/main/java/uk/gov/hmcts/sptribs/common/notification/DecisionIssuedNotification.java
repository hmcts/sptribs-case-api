package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
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

import static uk.gov.hmcts.sptribs.common.CommonConstants.DECISION_NOTICE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DOC_AVAILABLE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.EMPTY_PLACEHOLDER;

@Component
@Slf4j
public class DecisionIssuedNotification implements PartiesNotification {

    private static final int DOC_ATTACH_LIMIT = 5;
    private static final String YES = "yes";
    private static final String NO = "no";

    @Autowired
    private NotificationServiceCIC notificationService;

    @Autowired
    private NotificationHelper notificationHelper;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);


        NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocumentIds = getUploadedDocuments(caseData);

            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getEmail(),
                uploadedDocumentIds, templateVars);

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

        NotificationResponse notificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocumentIds = getUploadedDocuments(caseData);
            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getRepresentativeEmailAddress(),
                uploadedDocumentIds, templateVars);
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
        Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);

        NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(cicCase.getRespondentEmail(),
            uploadedDocuments, templateVars);
        cicCase.setAppNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper.getApplicantCommonVars(caseNumber, cicCase);


        NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocumentIds = getUploadedDocuments(caseData);

            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getApplicantEmailAddress(),
                uploadedDocumentIds, templateVars);

        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars);
        }

        cicCase.setSubjectNotifyList(notificationResponse);
    }

    private NotificationResponse sendEmailNotificationWithAttachment(final String destinationAddress,
                                                                     Map<String, String> uploadedDocumentIds,
                                                                     final Map<String, Object> templateVars) {
        NotificationRequest emailRequest = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            true,
            uploadedDocumentIds,
            templateVars,
            TemplateName.DECISION_ISSUED_EMAIL);
        return notificationService.sendEmail(emailRequest);
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter) {
        NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.DECISION_ISSUED_POST);
        return notificationService.sendLetter(letterRequest);
    }

    private Map<String, String> getUploadedDocuments(CaseData caseData) {
        CaseIssueDecision caseIssueDecision = caseData.getCaseIssueDecision();
        Map<String, String> uploadedDocuments = new HashMap<>();

        int count = 0;
        if (caseIssueDecision.getDecisionNotice() == NoticeOption.UPLOAD_FROM_COMPUTER) {

            String uuid = StringUtils.substringAfterLast(caseIssueDecision.getDecisionDocument().getDocumentLink().getUrl(), "/");
            uploadedDocuments.put(DOC_AVAILABLE + count, YES);
            uploadedDocuments.put(DECISION_NOTICE + count, uuid);

        } else if (caseIssueDecision.getDecisionNotice() == NoticeOption.CREATE_FROM_TEMPLATE) {
            count++;

            uploadedDocuments.put(DOC_AVAILABLE + count, YES);
            uploadedDocuments.put(DECISION_NOTICE + count,
                StringUtils.substringAfterLast(caseIssueDecision.getIssueDecisionDraft().getUrl(),
                    "/"));
        }

        while (count < DOC_ATTACH_LIMIT) {
            count++;
            uploadedDocuments.put(DOC_AVAILABLE + count, NO);
            uploadedDocuments.put(DECISION_NOTICE + count, EMPTY_PLACEHOLDER);
        }

        return uploadedDocuments;
    }

}
