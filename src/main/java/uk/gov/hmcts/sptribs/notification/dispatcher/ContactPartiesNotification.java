package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_EMAIL_VALUE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME_VALUE;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_EMAIL_NEW_CD;

@Component
@RequiredArgsConstructor
public class ContactPartiesNotification implements PartiesNotification {

    private static final int DOC_ATTACH_LIMIT = 10;

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    @Value("${sptribs-frontend.dashboard-url}")
    private String citizenDashboardUrl;

    @Value("${feature.citizen-dashboard.enabled}")
    private boolean citizenDashboardEnabled;

    @Override
    public String sendToSubject(final CaseData caseData, final String caseNumber, final Map<String, String> uploadedDocuments) {
        return sendToSubject(caseData, caseNumber, legacyUploadedDocuments(caseData), legacySelectedDocuments(caseData));
    }

    public String sendToSubject(final CaseData caseData,
                                final String caseNumber,
                                final Map<String, String> uploadedDocuments,
                                final List<CaseworkerCICDocument> selectedDocuments) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsSubject = notificationHelper.getSubjectCommonVars(caseNumber, caseData);
        templateVarsSubject.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsSubject.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        final NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            // Send Email
            addDashboardLink(templateVarsSubject);
            notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getEmail(),
                templateVarsSubject,
                getTemplateName(),
                caseNumber,
                Party.SUBJECT,
                uploadedDocuments,
                selectedDocuments);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVarsSubject);
            //SEND POST
            notificationResponse = sendLetterNotification(templateVarsSubject, TemplateName.CONTACT_PARTIES_POST, caseNumber);
        }
        cicCase.setSubjectLetterNotifyList(notificationResponse);
        return notificationResponse.getId();
    }

    @Override
    public String sendToApplicant(final CaseData caseData, final String caseNumber, final Map<String, String> uploadedDocuments) {
        return sendToApplicant(caseData, caseNumber, legacyUploadedDocuments(caseData), legacySelectedDocuments(caseData));
    }

    public String sendToApplicant(final CaseData caseData,
                                  final String caseNumber,
                                  final Map<String, String> uploadedDocuments,
                                  final List<CaseworkerCICDocument> selectedDocuments) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsApplicant = notificationHelper.getApplicantCommonVars(caseNumber, caseData);
        templateVarsApplicant.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsApplicant.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        final NotificationResponse notificationResponse;
        if (caseData.getCicCase().getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            // Send Email
            addDashboardLink(templateVarsApplicant);
            notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getApplicantEmailAddress(),
                templateVarsApplicant,
                getTemplateName(),
                caseNumber,
                Party.APPLICANT,
                uploadedDocuments,
                selectedDocuments);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVarsApplicant);
            notificationResponse = sendLetterNotification(templateVarsApplicant,
                TemplateName.CONTACT_PARTIES_POST, caseNumber);
        }

        cicCase.setAppNotificationResponse(notificationResponse);
        return notificationResponse.getId();
    }

    @Override
    public String sendToRepresentative(final CaseData caseData, final String caseNumber, final Map<String, String> uploadedDocuments) {
        return sendToRepresentative(caseData, caseNumber, legacyUploadedDocuments(caseData), legacySelectedDocuments(caseData));
    }

    public String sendToRepresentative(final CaseData caseData,
                                       final String caseNumber,
                                       final Map<String, String> uploadedDocuments,
                                       final List<CaseworkerCICDocument> selectedDocuments) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsRepresentative.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        final NotificationResponse notificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            // Send Email
            addDashboardLink(templateVarsRepresentative);
            notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getRepresentativeEmailAddress(),
                templateVarsRepresentative,
                getTemplateName(),
                caseNumber,
                Party.REPRESENTATIVE,
                uploadedDocuments,
                selectedDocuments);

        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVarsRepresentative);
            notificationResponse = sendLetterNotification(templateVarsRepresentative,
                TemplateName.CONTACT_PARTIES_POST, caseNumber);
        }

        cicCase.setRepNotificationResponse(notificationResponse);
        return notificationResponse.getId();
    }

    @Override
    public String sendToRespondent(final CaseData caseData, final String caseNumber, final Map<String, String> uploadedDocuments) {
        return sendToRespondent(caseData, caseNumber, legacyUploadedDocuments(caseData), legacySelectedDocuments(caseData));
    }

    public String sendToRespondent(final CaseData caseData,
                                   final String caseNumber,
                                   final Map<String, String> uploadedDocuments,
                                   final List<CaseworkerCICDocument> selectedDocuments) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRespondent = notificationHelper.getRespondentCommonVars(caseNumber, caseData);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsRespondent.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        final NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(
            cicCase.getRespondentEmail(),
            templateVarsRespondent,
            CONTACT_PARTIES_EMAIL,
            caseNumber,
            Party.RESPONDENT,
            uploadedDocuments,
            selectedDocuments
        );

        cicCase.setResNotificationResponse(notificationResponse);
        return notificationResponse.getId();
    }

    @Override
    public String sendToTribunal(final CaseData caseData, final String caseNumber, final Map<String, String> uploadedDocuments) {
        Map<String, String> legacyUploadedDocuments = legacyUploadedDocuments(caseData);
        List<CaseworkerCICDocument> legacySelectedDocuments = legacySelectedDocuments(caseData);
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsTribunal = notificationHelper.getTribunalCommonVars(caseNumber, caseData);
        templateVarsTribunal.put(CommonConstants.CIC_CASE_TRIBUNAL_NAME, TRIBUNAL_NAME_VALUE);
        templateVarsTribunal.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        // Send Email
        final NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(
            TRIBUNAL_EMAIL_VALUE,
            templateVarsTribunal,
            CONTACT_PARTIES_EMAIL,
            caseNumber,
            Party.TRIBUNAL,
            legacyUploadedDocuments,
            legacySelectedDocuments
        );

        cicCase.setTribunalNotificationResponse(notificationResponse);
        return notificationResponse.getId();
    }

    private NotificationResponse sendEmailNotificationWithAttachment(String toEmail, final Map<String, Object> templateVars,
                                                                     TemplateName emailTemplateName,
                                                                     String caseReferenceNumber,
                                                                     Party receivingParty,
                                                                     Map<String, String> uploadedDocuments,
                                                                     List<CaseworkerCICDocument> selectedDocuments) {
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(toEmail,
            true,
            uploadedDocuments,
            templateVars,
            emailTemplateName);

        List<CaseworkerCICDocument> documents = selectedDocuments != null ? selectedDocuments : Collections.emptyList();
        return notificationService.sendEmail(request, documents, caseReferenceNumber, receivingParty);
    }

    private Map<String, String> legacyUploadedDocuments(CaseData caseData) {
        return notificationHelper.buildDocumentList(caseData.getContactPartiesDocuments().getDocumentList(), DOC_ATTACH_LIMIT);
    }

    private List<CaseworkerCICDocument> legacySelectedDocuments(CaseData caseData) {
        return DocumentListUtil.getSelectedDocumentsFromDynamicList(
            caseData,
            caseData.getContactPartiesDocuments().getDocumentList()
        );
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter,
                                                        TemplateName emailTemplateName,
                                                        String caseReferenceNumber) {
        final NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(templateVarsLetter, emailTemplateName);
        return notificationService.sendLetter(letterRequest, caseReferenceNumber);
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
