package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.caseworker.model.NoticeOption;
import uk.gov.hmcts.sptribs.caseworker.util.DecisionDocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.document.DocumentUtil;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.common.CommonConstants.EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.FINAL_DECISION_GUIDANCE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.FINAL_DECISION_NOTICE;
import static uk.gov.hmcts.sptribs.notification.TemplateName.FINAL_DECISION_ISSUED_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.FINAL_DECISION_ISSUED_EMAIL_NEW_CD;
import static uk.gov.hmcts.sptribs.notification.TemplateName.FINAL_DECISION_ISSUED_POST;

@Component
@Slf4j
public class CaseFinalDecisionIssuedNotification extends PartiesNotification {

    @Value("${sptribs-frontend.dashboard-url}")
    private String citizenDashboardUrl;

    @Value("${feature.citizen-dashboard.enabled}")
    private boolean citizenDashboardEnabled;

    public CaseFinalDecisionIssuedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    protected PartyNotification buildSubjectNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsSubject = notificationHelper().getSubjectCommonVars(caseNumber, caseData);
        addDashboardLink(templateVarsSubject);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            return new EmailNotification(
                    cicCase.getEmail(),
                    templateVarsSubject,
                    getTemplateName(),
                    uploadedDocuments,
                    getFinalDecisionDocumentsAttachments(caseData),
                    saveToCicCase(CicCase::setSubjectNotifyList)
            );
        } else {
            return new LetterNotification(
                    cicCase.getAddress(),
                    templateVarsSubject,
                    FINAL_DECISION_ISSUED_POST,
                    saveToCicCase(CicCase::setSubjectLetterNotifyList)
            );
        }
    }

    @Override
    protected PartyNotification buildApplicantNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getApplicantCommonVars(caseNumber, caseData);
        addDashboardLink(templateVars);

        if (cicCase.getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            return new EmailNotification(
                    cicCase.getApplicantEmailAddress(),
                    templateVars,
                    getTemplateName(),
                    uploadedDocuments,
                    getFinalDecisionDocumentsAttachments(caseData),
                    saveToCicCase(CicCase::setAppNotificationResponse)
            );
        } else {
            return new LetterNotification(
                    cicCase.getApplicantAddress(),
                    templateVars,
                    FINAL_DECISION_ISSUED_POST,
                    saveToCicCase(CicCase::setAppLetterNotificationResponse)
            );
        }
    }

    @Override
    protected PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsRepresentative = notificationHelper().getRepresentativeCommonVars(caseNumber, caseData);
        addDashboardLink(templateVarsRepresentative);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            return new EmailNotification(cicCase.getRepresentativeEmailAddress(),
                templateVarsRepresentative,
                getTemplateName(),
                uploadedDocuments,
                getFinalDecisionDocumentsAttachments(caseData),
                saveToCicCase(CicCase::setRepNotificationResponse));
        } else {
            return new LetterNotification(cicCase.getRepresentativeAddress(),
                templateVarsRepresentative,
                FINAL_DECISION_ISSUED_POST,
                saveToCicCase(CicCase::setRepLetterNotificationResponse));
        }
    }

    @Override
    protected PartyNotification buildRespondentNotification(CaseData caseData, String caseNumber) {
        Map<String, Object> templateVarsRespondent = notificationHelper().getRespondentCommonVars(caseNumber, caseData);
        CicCase cicCase = caseData.getCicCase();
        Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);

        return new EmailNotification(cicCase.getRespondentEmail(),
            templateVarsRespondent,
            FINAL_DECISION_ISSUED_EMAIL,
            uploadedDocuments,
            getFinalDecisionDocumentsAttachments(caseData),
            saveToCicCase(CicCase::setResNotificationResponse));
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

    private List<CaseworkerCICDocument> getFinalDecisionDocumentsAttachments(CaseData caseData) {
        CaseIssueFinalDecision caseIssueFinalDecision = caseData.getCaseIssueFinalDecision();
        List<CaseworkerCICDocument> finalDecisionDocuments = DecisionDocumentListUtil.getFinalDecisionDocs(caseData);
        List<String> uuids = new ArrayList<>();
        if (NoticeOption.UPLOAD_FROM_COMPUTER.equals(caseIssueFinalDecision.getFinalDecisionNotice())) {
            uuids.add(DocumentUtil.getDocumentUuidFromCICDocument(caseIssueFinalDecision.getDocument()));
        } else if (NoticeOption.CREATE_FROM_TEMPLATE.equals(caseIssueFinalDecision.getFinalDecisionNotice())) {
            uuids.add(DocumentUtil.getUuid(caseIssueFinalDecision.getFinalDecisionDraft()));
        }
        List<CaseworkerCICDocument> filteredList = new ArrayList<>(finalDecisionDocuments.stream()
            .filter(doc -> uuids.stream().anyMatch(uuid -> DocumentUtil.getDocumentUuidFromCaseworkerCICDocument(doc).contains(uuid)))
            .toList());
        CaseworkerCICDocument guidanceDocument = CaseworkerCICDocument.builder()
            .documentLink(caseIssueFinalDecision.getFinalDecisionGuidance())
            .build();
        filteredList.add(guidanceDocument);
        return filteredList;
    }

    private TemplateName getTemplateName() {
        return citizenDashboardEnabled ? FINAL_DECISION_ISSUED_EMAIL_NEW_CD : FINAL_DECISION_ISSUED_EMAIL;
    }

    private void addDashboardLink(Map<String, Object> templateVars) {
        if (citizenDashboardEnabled) {
            templateVars.put(DASHBOARD_KEY, citizenDashboardUrl);
        }
    }
}
