package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationContextRequest;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.APPLICANT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.SUBJECT;
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
                caseData,
                getTemplateName(),
                caseNumber,
                Party.SUBJECT);
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
                caseData,
                getTemplateName(),
                caseNumber,
                Party.APPLICANT);
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
                caseData,
                getTemplateName(),
                caseNumber,
                Party.REPRESENTATIVE);

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
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRespondent = notificationHelper.getRespondentCommonVars(caseNumber, caseData);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsRespondent.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        // Send Email
        final NotificationResponse notificationResponse;
        if (ObjectUtils.isNotEmpty(caseData.getContactPartiesDocuments().getDocumentList())) {

            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getRespondentEmail(),
                templateVarsRespondent,
                caseData,
                CONTACT_PARTIES_EMAIL,
                caseNumber,
                Party.RESPONDENT);
        } else {
            notificationResponse = sendEmailNotification(templateVarsRespondent,
                cicCase.getRespondentEmail(), CONTACT_PARTIES_EMAIL, caseNumber, Party.RESPONDENT);
        }

        cicCase.setResNotificationResponse(notificationResponse);
        return notificationResponse.getId();
    }

    @Override
    public String sendToTribunal(final CaseData caseData, final String caseNumber, final Map<String, String> uploadedDocuments) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsTribunal = notificationHelper.getTribunalCommonVars(caseNumber, caseData);
        templateVarsTribunal.put(CommonConstants.CIC_CASE_TRIBUNAL_NAME, TRIBUNAL_NAME_VALUE);
        templateVarsTribunal.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        // Send Email
        final NotificationResponse notificationResponse;
        if (ObjectUtils.isNotEmpty(caseData.getContactPartiesDocuments().getDocumentList())) {

            notificationResponse = sendEmailNotificationWithAttachment(TRIBUNAL_EMAIL_VALUE,
                templateVarsTribunal,
                caseData,
                CONTACT_PARTIES_EMAIL,
                caseNumber,
                Party.TRIBUNAL);
        } else {
            notificationResponse = sendEmailNotification(templateVarsTribunal,
                TRIBUNAL_EMAIL_VALUE, CONTACT_PARTIES_EMAIL, caseNumber, Party.TRIBUNAL);
        }

        cicCase.setTribunalNotificationResponse(notificationResponse);
        return notificationResponse.getId();
    }

    @Override
    public Set<NotificationParties> buildCorrespondenceParties(NotificationContextRequest request) {
        Set<NotificationParties> correspondenceParties = new HashSet<>();
        CicCase cicCase = request.getCaseData().getCicCase();

        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())) {
            correspondenceParties.add(SUBJECT);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())) {
            correspondenceParties.add(REPRESENTATIVE);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyApplicant())) {
            correspondenceParties.add(APPLICANT);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent())) {
            correspondenceParties.add(RESPONDENT);
        }
        return correspondenceParties;
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars,
                                                       String toEmail,
                                                       TemplateName emailTemplateName,
                                                       String caseReferenceNumber,
                                                       Party receivingParty) {
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(toEmail, templateVars, emailTemplateName);
        return notificationService.sendEmail(request, caseReferenceNumber, receivingParty);
    }

    private NotificationResponse sendEmailNotificationWithAttachment(String toEmail, final Map<String, Object> templateVars,
                                                                     CaseData caseData,
                                                                     TemplateName emailTemplateName,
                                                                     String caseReferenceNumber,
                                                                     Party receivingParty) {
        Map<String, String> uploadedDocuments = notificationHelper.buildDocumentList(caseData.getContactPartiesDocuments()
            .getDocumentList(), DOC_ATTACH_LIMIT);
        List<CaseworkerCICDocument> selectedDocuments = DocumentListUtil.getSelectedDocumentsFromDynamicList(caseData,
            caseData.getContactPartiesDocuments().getDocumentList());

        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(toEmail,
            true,
            uploadedDocuments,
            templateVars,
            emailTemplateName);

        return notificationService.sendEmail(request, selectedDocuments, caseReferenceNumber, receivingParty);
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
