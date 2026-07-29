package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_EMAIL_VALUE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME_VALUE;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_EMAIL_NEW_CD;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_POST;

@Component
public class ContactPartiesNotification extends PartiesNotification {

    private static final int DOC_ATTACH_LIMIT = 10;

    @Value("${sptribs-frontend.dashboard-url}")
    private String citizenDashboardUrl;

    @Value("${feature.citizen-dashboard.enabled}")
    private boolean citizenDashboardEnabled;

    public ContactPartiesNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    public PartyNotification buildSubjectNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsSubject = notificationHelper().getSubjectCommonVars(caseNumber, caseData);
        templateVarsSubject.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsSubject.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());
        addDashboardLink(templateVarsSubject);

        Map<String, String> uploadedDocuments = notificationHelper().buildDocumentList(caseData.getContactPartiesDocuments().getDocumentList(), DOC_ATTACH_LIMIT);
        List<CaseworkerCICDocument> selectedDocuments =
            DocumentListUtil.getSelectedDocumentsFromDynamicList(caseData, caseData.getContactPartiesDocuments().getDocumentList());

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            return new EmailNotification(cicCase.getEmail(), templateVarsSubject, getTemplateName(), uploadedDocuments, selectedDocuments, saveToCicCase(CicCase::setSubjectNotifyList));
        } else {
            return new LetterNotification(cicCase.getAddress(), templateVarsSubject, CONTACT_PARTIES_POST, saveToCicCase(CicCase::setSubjectLetterNotifyList));
        }
    }

    @Override
    public PartyNotification buildApplicantNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsApplicant = notificationHelper().getApplicantCommonVars(caseNumber, caseData);
        templateVarsApplicant.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsApplicant.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());
        addDashboardLink(templateVarsApplicant);

        Map<String, String> uploadedDocuments = notificationHelper().buildDocumentList(caseData.getContactPartiesDocuments().getDocumentList(), DOC_ATTACH_LIMIT);
        List<CaseworkerCICDocument> selectedDocuments =
            DocumentListUtil.getSelectedDocumentsFromDynamicList(caseData, caseData.getContactPartiesDocuments().getDocumentList());

        if (cicCase.getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            return new EmailNotification(cicCase.getApplicantEmailAddress(), templateVarsApplicant,getTemplateName(), uploadedDocuments, selectedDocuments, saveToCicCase(CicCase::setAppNotificationResponse));
        } else {
            return new LetterNotification(cicCase.getApplicantAddress(), templateVarsApplicant, CONTACT_PARTIES_POST, saveToCicCase(CicCase::setAppLetterNotificationResponse));
        }
    }

    @Override
    public PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsRepresentative = notificationHelper().getRepresentativeCommonVars(caseNumber, caseData);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsRepresentative.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());
        addDashboardLink(templateVarsRepresentative);

        Map<String, String> uploadedDocuments = notificationHelper().buildDocumentList(caseData.getContactPartiesDocuments().getDocumentList(), DOC_ATTACH_LIMIT);
        List<CaseworkerCICDocument> selectedDocuments =
            DocumentListUtil.getSelectedDocumentsFromDynamicList(caseData, caseData.getContactPartiesDocuments().getDocumentList());

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            return new EmailNotification(cicCase.getRepresentativeEmailAddress(), templateVarsRepresentative, getTemplateName(), uploadedDocuments, selectedDocuments, saveToCicCase(CicCase::setRepNotificationResponse));
        } else {
            return new LetterNotification(cicCase.getRepresentativeAddress(), templateVarsRepresentative, CONTACT_PARTIES_POST, saveToCicCase(CicCase::setRepLetterNotificationResponse));
        }
    }

    @Override
    public PartyNotification buildRespondentNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsRespondent = notificationHelper().getRespondentCommonVars(caseNumber, caseData);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsRespondent.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        Map<String, String> uploadedDocuments = notificationHelper().buildDocumentList(caseData.getContactPartiesDocuments().getDocumentList(), DOC_ATTACH_LIMIT);
        List<CaseworkerCICDocument> selectedDocuments =
            DocumentListUtil.getSelectedDocumentsFromDynamicList(caseData, caseData.getContactPartiesDocuments().getDocumentList());

        return new EmailNotification(cicCase.getRespondentEmail(), templateVarsRespondent, CONTACT_PARTIES_EMAIL, uploadedDocuments, selectedDocuments, saveToCicCase(CicCase::setResNotificationResponse));
    }

    @Override
    public PartyNotification buildTribunalNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsTribunal = notificationHelper().getTribunalCommonVars(caseNumber, caseData);
        templateVarsTribunal.put(CommonConstants.CIC_CASE_TRIBUNAL_NAME, TRIBUNAL_NAME_VALUE);
        templateVarsTribunal.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        Map<String, String> uploadedDocuments = notificationHelper().buildDocumentList(caseData.getContactPartiesDocuments().getDocumentList(), DOC_ATTACH_LIMIT);
        List<CaseworkerCICDocument> selectedDocuments =
            DocumentListUtil.getSelectedDocumentsFromDynamicList(caseData, caseData.getContactPartiesDocuments().getDocumentList());

        return new EmailNotification(TRIBUNAL_EMAIL_VALUE, templateVarsTribunal, CONTACT_PARTIES_EMAIL, uploadedDocuments, selectedDocuments, saveToCicCase(CicCase::setResNotificationResponse));
    }

    private TemplateName getTemplateName() {
        return citizenDashboardEnabled ? CONTACT_PARTIES_EMAIL_NEW_CD : CONTACT_PARTIES_EMAIL;
    }

    private void addDashboardLink(Map<String, Object> templateVars) {
        if (citizenDashboardEnabled) {
            templateVars.put(DASHBOARD_KEY, citizenDashboardUrl);
        }
    }
}
