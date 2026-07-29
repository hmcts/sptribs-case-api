package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.NoticeOption;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DECISION_NOTICE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DOC_AVAILABLE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.sptribs.notification.TemplateName.DECISION_ISSUED_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.DECISION_ISSUED_EMAIL_NEW_CD;
import static uk.gov.hmcts.sptribs.notification.TemplateName.DECISION_ISSUED_POST;

@Component
@Slf4j
public class DecisionIssuedNotification extends PartiesNotification {

    private static final int DOC_ATTACH_LIMIT = 5;
    private static final String YES = "yes";
    private static final String NO = "no";

    @Value("${sptribs-frontend.dashboard-url}")
    private String citizenDashboardUrl;

    @Value("${feature.citizen-dashboard.enabled}")
    private boolean citizenDashboardEnabled;

    public DecisionIssuedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    protected PartyNotification buildSubjectNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getSubjectCommonVars(caseNumber, caseData);
        addDashboardLink(templateVars);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocumentIds = getUploadedDocuments(caseData);
            return new EmailNotification(
                    cicCase.getEmail(),
                    templateVars,
                    getTemplateName(),
                    uploadedDocumentIds,
                    new ArrayList<>(),
                    saveToCicCase(CicCase::setSubjectNotifyList)
            );
        } else {
            notificationHelper().addAddressTemplateVars(cicCase.getAddress(), templateVars);
            return new LetterNotification(
                    cicCase.getAddress(),
                    templateVars,
                    DECISION_ISSUED_POST,
                    saveToCicCase(CicCase::setSubjectLetterNotifyList)
            );
        }
    }

    @Override
    protected PartyNotification buildApplicantNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getApplicantCommonVars(caseNumber, caseData);
        addDashboardLink(templateVars);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocumentIds = getUploadedDocuments(caseData);
            return new EmailNotification(
                    cicCase.getApplicantEmailAddress(),
                    templateVars,
                    getTemplateName(),
                    uploadedDocumentIds,
                    new ArrayList<>(),
                    saveToCicCase(CicCase::setAppNotificationResponse)
            );
        } else {
            notificationHelper().addAddressTemplateVars(cicCase.getApplicantAddress(), templateVars);
            return new LetterNotification(
                    cicCase.getApplicantAddress(),
                    templateVars,
                    DECISION_ISSUED_POST,
                    saveToCicCase(CicCase::setSubjectLetterNotifyList)
            );
        }
    }

    @Override
    protected PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getRepresentativeCommonVars(caseNumber, caseData);
        addDashboardLink(templateVars);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocumentIds = getUploadedDocuments(caseData);
            return new EmailNotification(
                    cicCase.getRepresentativeEmailAddress(),
                    templateVars,
                    getTemplateName(),
                    uploadedDocumentIds,
                    new ArrayList<>(),
                    saveToCicCase(CicCase::setRepNotificationResponse));
        } else {
            notificationHelper().addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            return new LetterNotification(
                    cicCase.getRepresentativeAddress(),
                    templateVars,
                    DECISION_ISSUED_POST,
                    saveToCicCase(CicCase::setRepLetterNotificationResponse)
            );
        }
    }

    @Override
    protected PartyNotification buildRespondentNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getRespondentCommonVars(caseNumber, caseData);
        Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);

        return new EmailNotification(
                cicCase.getRespondentEmail(),
                templateVars,
                DECISION_ISSUED_EMAIL,
                uploadedDocuments,
                new ArrayList<>(),
                saveToCicCase(CicCase::setResNotificationResponse)
        );
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

    private TemplateName getTemplateName() {
        return citizenDashboardEnabled ? DECISION_ISSUED_EMAIL_NEW_CD : DECISION_ISSUED_EMAIL;
    }

    private void addDashboardLink(Map<String, Object> templateVars) {
        if (citizenDashboardEnabled) {
            templateVars.put(DASHBOARD_KEY, citizenDashboardUrl);
        }
    }
}
