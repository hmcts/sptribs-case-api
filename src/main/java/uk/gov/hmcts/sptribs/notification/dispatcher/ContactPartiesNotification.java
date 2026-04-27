package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.document.persistence.DocumentsService;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.sptribs.common.CommonConstants.DOC_AVAILABLE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_EMAIL_VALUE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME_VALUE;

@Component
@Slf4j
public class ContactPartiesNotification implements PartiesNotification {

    @Autowired
    private final NotificationServiceCIC notificationService;

    @Autowired
    private final NotificationHelper notificationHelper;

    @Autowired
    private final DocumentsService documentsService;

    @Autowired
    private final CaseDocumentClientApi caseDocumentClientApi;

    @Autowired
    private final IdamService idamService;

    @Autowired
    private final AuthTokenGenerator authTokenGenerator;

    private static final int DOC_ATTACH_LIMIT = 10;

    public ContactPartiesNotification(NotificationServiceCIC notificationService,
                                      NotificationHelper notificationHelper,
                                      DocumentsService documentsService,
                                      CaseDocumentClientApi caseDocumentClientApi,
                                      IdamService idamService,
                                      AuthTokenGenerator authTokenGenerator) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
        this.documentsService = documentsService;
        this.caseDocumentClientApi = caseDocumentClientApi;
        this.idamService = idamService;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsSubject = notificationHelper.getSubjectCommonVars(caseNumber, caseData);
        templateVarsSubject.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsSubject.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        final NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            // Send Email
            final Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);

            notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getEmail(),
                templateVarsSubject,
                uploadedDocuments,
                TemplateName.CONTACT_PARTIES_EMAIL, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVarsSubject);
            //SEND POST
            notificationResponse = sendLetterNotification(templateVarsSubject, TemplateName.CONTACT_PARTIES_POST, caseNumber);
        }
        cicCase.setSubjectLetterNotifyList(notificationResponse);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsApplicant = notificationHelper.getApplicantCommonVars(caseNumber, caseData);
        templateVarsApplicant.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsApplicant.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        final NotificationResponse notificationResponse;
        if (caseData.getCicCase().getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            // Send Email
            final Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getApplicantEmailAddress(),
                templateVarsApplicant,
                uploadedDocuments,
                TemplateName.CONTACT_PARTIES_EMAIL, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVarsApplicant);
            notificationResponse = sendLetterNotification(templateVarsApplicant,
                TemplateName.CONTACT_PARTIES_POST, caseNumber);
        }

        cicCase.setAppNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsRepresentative.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        final NotificationResponse notificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            // Send Email
            final Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            notificationResponse = sendEmailNotificationWithAttachment(
                cicCase.getRepresentativeEmailAddress(),
                templateVarsRepresentative,
                uploadedDocuments,
                TemplateName.CONTACT_PARTIES_EMAIL, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVarsRepresentative);
            notificationResponse = sendLetterNotification(templateVarsRepresentative,
                TemplateName.CONTACT_PARTIES_POST, caseNumber);
        }

        cicCase.setRepNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRespondent = notificationHelper.getRespondentCommonVars(caseNumber, caseData);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsRespondent.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        // Send Email
        final NotificationResponse notificationResponse;
        if (ObjectUtils.isNotEmpty(caseData.getContactPartiesDocuments().getDocumentList())) {
            final Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getRespondentEmail(),
                templateVarsRespondent,
                uploadedDocuments,
                TemplateName.CONTACT_PARTIES_EMAIL, caseNumber);
        } else {
            notificationResponse = sendEmailNotification(templateVarsRespondent,
                cicCase.getRespondentEmail(), TemplateName.CONTACT_PARTIES_EMAIL, caseNumber);
        }

        cicCase.setResNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToTribunal(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsTribunal = notificationHelper.getTribunalCommonVars(caseNumber, caseData);
        templateVarsTribunal.put(CommonConstants.CIC_CASE_TRIBUNAL_NAME, TRIBUNAL_NAME_VALUE);
        templateVarsTribunal.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        // Send Email
        final NotificationResponse notificationResponse;
        if (ObjectUtils.isNotEmpty(caseData.getContactPartiesDocuments().getDocumentList())) {
            final Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            notificationResponse = sendEmailNotificationWithAttachment(TRIBUNAL_EMAIL_VALUE,
                templateVarsTribunal,
                uploadedDocuments,
                TemplateName.CONTACT_PARTIES_EMAIL, caseNumber);
        } else {
            notificationResponse = sendEmailNotification(templateVarsTribunal,
                TRIBUNAL_EMAIL_VALUE, TemplateName.CONTACT_PARTIES_EMAIL, caseNumber);
        }

        cicCase.setTribunalNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars,
                                                       String toEmail,
                                                       TemplateName emailTemplateName,
                                                       String caseReferenceNumber) {
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(toEmail, templateVars, emailTemplateName);
        return notificationService.sendEmail(request, caseReferenceNumber);
    }

    private NotificationResponse sendEmailNotificationWithAttachment(
        String toEmail, final Map<String, Object> templateVars,
        Map<String, String> uploadedDocuments,
        TemplateName emailTemplateName,
        String caseReferenceNumber) {
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(toEmail,
            true,
            uploadedDocuments,
            templateVars,
            emailTemplateName);
        for (Map.Entry<String, String> uploadedDocumentEntry : request.getUploadedDocuments().entrySet()) {
            log.error("UUID: {}", uploadedDocumentEntry.getValue());
            if (uploadedDocumentEntry.getKey().contains(DOC_AVAILABLE) || StringUtils.isEmpty(uploadedDocumentEntry.getValue())) {
                continue;
            }

            ResponseEntity<uk.gov.hmcts.sptribs.cdam.model.Document> documentToAttach = caseDocumentClientApi.getDocument(
                idamService.retrieveSystemUpdateUserDetails().getAuthToken(),
                authTokenGenerator.generate(),
                UUID.fromString(uploadedDocumentEntry.getValue())
            );

            if (documentToAttach.getBody() != null) {
                documentsService.setSentToApplicantViaContactPartiesToTrue(documentToAttach.getBody().links.binary.href);
            }
        }
        return notificationService.sendEmail(request, caseReferenceNumber);
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter,
                                                        TemplateName emailTemplateName,
                                                        String caseReferenceNumber) {
        final NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(templateVarsLetter, emailTemplateName);
        return notificationService.sendLetter(letterRequest, caseReferenceNumber);
    }

    private Map<String, String> getUploadedDocuments(CaseData caseData) {
        final ContactPartiesDocuments contactPartiesDocuments = caseData.getContactPartiesDocuments();
        return notificationHelper.buildDocumentList(contactPartiesDocuments.getDocumentList(), DOC_ATTACH_LIMIT);
    }
}
