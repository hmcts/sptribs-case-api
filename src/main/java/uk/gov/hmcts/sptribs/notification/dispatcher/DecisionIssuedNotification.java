package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.NoticeOption;
import uk.gov.hmcts.sptribs.caseworker.util.DecisionDocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.document.DocumentUtil;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationContextRequest;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.APPLICANT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.SUBJECT;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DECISION_NOTICE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DOC_AVAILABLE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.sptribs.notification.TemplateName.DECISION_ISSUED_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.DECISION_ISSUED_EMAIL_NEW_CD;

@Component
@Slf4j
public class DecisionIssuedNotification implements PartiesNotification {

    private static final int DOC_ATTACH_LIMIT = 5;
    private static final String YES = "yes";
    private static final String NO = "no";

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    @Value("${sptribs-frontend.dashboard-url}")
    private String citizenDashboardUrl;

    @Value("${feature.citizen-dashboard.enabled}")
    private boolean citizenDashboardEnabled;

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
            addDashboardLink(templateVars);

            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getEmail(),
                caseData, templateVars, getTemplateName(), caseNumber);

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
            addDashboardLink(templateVars);
            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getRepresentativeEmailAddress(),
                caseData, templateVars, getTemplateName(), caseNumber);
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

        final NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(cicCase.getRespondentEmail(),
            caseData, templateVars, DECISION_ISSUED_EMAIL, caseNumber);
        cicCase.setAppNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVars = notificationHelper.getApplicantCommonVars(caseNumber, caseData);

        final NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            addDashboardLink(templateVars);

            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getApplicantEmailAddress(),
                caseData, templateVars, getTemplateName(), caseNumber);

        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars, caseNumber);
        }

        cicCase.setSubjectNotifyList(notificationResponse);
    }

    private NotificationResponse sendEmailNotificationWithAttachment(final String destinationAddress,
                                                                     CaseData caseData,
                                                                     final Map<String, Object> templateVars,
                                                                     TemplateName templateName,
                                                                     String caseReferenceNumber) {

        final Map<String, String> uploadedDocumentIds = getUploadedDocuments(caseData);
        final NotificationRequest emailRequest = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            true,
            uploadedDocumentIds,
            templateVars,
            templateName);
        return notificationService.sendEmail(emailRequest, getDecisionDocuments(caseData), caseReferenceNumber, null);
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

    private List<CaseworkerCICDocument> getDecisionDocuments(CaseData caseData) {
        CaseIssueDecision caseIssueDecision = caseData.getCaseIssueDecision();
        List<CaseworkerCICDocument> decisionDocuments = DecisionDocumentListUtil.getDecisionDocs(caseData);
        String uuid;
        if (NoticeOption.UPLOAD_FROM_COMPUTER.equals(caseIssueDecision.getDecisionNotice())) {
            uuid = DocumentUtil.getUuid(caseIssueDecision.getDecisionDocument().getDocumentLink());
        } else if (NoticeOption.CREATE_FROM_TEMPLATE.equals(caseIssueDecision.getDecisionNotice())) {
            uuid = DocumentUtil.getUuid(caseIssueDecision.getIssueDecisionDraft());
        } else {
            uuid = "";
        }
        return decisionDocuments.stream().filter(doc -> doc.getDocumentLink().getBinaryUrl().contains(uuid)).toList();
    }

    private TemplateName getTemplateName() {
        return citizenDashboardEnabled ? DECISION_ISSUED_EMAIL_NEW_CD : DECISION_ISSUED_EMAIL;
    }

    private void addDashboardLink(Map<String, Object> templateVars) {
        if (citizenDashboardEnabled) {
            templateVars.put(DASHBOARD_KEY, citizenDashboardUrl);
        }
    }

    @Override
    public Set<NotificationParties> buildCorrespondenceParties(NotificationContextRequest request) {
        Set<NotificationParties> correspondenceParties = new HashSet<>();
        CicCase cicCase = request.getCaseData().getCicCase();

        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())) {
            correspondenceParties.add(SUBJECT);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyApplicant())) {
            correspondenceParties.add(APPLICANT);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())) {
            correspondenceParties.add(REPRESENTATIVE);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent())) {
            correspondenceParties.add(RESPONDENT);
        }

        return correspondenceParties;
    }
}
