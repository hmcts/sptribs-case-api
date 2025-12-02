package uk.gov.hmcts.sptribs.notification.dispatcher;

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

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    @Autowired
    public DecisionIssuedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        final Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, caseData);

        final NotificationResponse notificationResponse;
        final CicCase cicCase = caseData.getCicCase();
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            final Map<String, String> uploadedDocumentIds = getUploadedDocuments(caseData);

            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getEmail(),
                uploadedDocumentIds, templateVars, caseNumber);

        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars, caseNumber);
        }

        cicCase.setSubjectNotifyList(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final NotificationResponse notificationResponse;
        final Map<String, Object> templateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            final Map<String, String> uploadedDocumentIds = getUploadedDocuments(caseData);
            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getRepresentativeEmailAddress(),
                uploadedDocumentIds, templateVars, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars, caseNumber);
        }

        cicCase.setRepNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVars = notificationHelper.getRespondentCommonVars(caseNumber, caseData);
        final Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);

        final NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(cicCase.getRespondentEmail(),
            uploadedDocuments, templateVars, caseNumber);
        cicCase.setAppNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVars = notificationHelper.getApplicantCommonVars(caseNumber, caseData);

        final NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            final Map<String, String> uploadedDocumentIds = getUploadedDocuments(caseData);

            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getApplicantEmailAddress(),
                uploadedDocumentIds, templateVars, caseNumber);

        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars, caseNumber);
        }

        cicCase.setSubjectNotifyList(notificationResponse);
    }

    private NotificationResponse sendEmailNotificationWithAttachment(final String destinationAddress,
                                                                     Map<String, String> uploadedDocumentIds,
                                                                     final Map<String, Object> templateVars,
                                                                     String caseReferenceNumber) {
        final NotificationRequest emailRequest = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            true,
            uploadedDocumentIds,
            templateVars,
            TemplateName.DECISION_ISSUED_EMAIL);
        return notificationService.sendEmail(emailRequest, caseReferenceNumber);
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter, String caseReferenceNumber) {
        final NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.DECISION_ISSUED_POST);
        return notificationService.sendLetter(letterRequest, caseReferenceNumber);
    }

    private Map<String, String> getUploadedDocuments(CaseData caseData) {
        final CaseIssueDecision caseIssueDecision = caseData.getCaseIssueDecision();
        final Map<String, String> uploadedDocuments = new HashMap<>();

        int count = 0;
        if (caseIssueDecision.getDecisionNotice() == NoticeOption.UPLOAD_FROM_COMPUTER) {
            count++;

            final String uuid = StringUtils.substringAfterLast(caseIssueDecision.getDecisionDocument().getDocumentLink().getUrl(), "/");
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
