package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
public class CaseIssuedNotification implements PartiesNotification {

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    private static final int DOC_ATTACH_LIMIT = 5;
    private static final int NUMBER_OF_DAYS_IN_WINDOW = 42;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsSubject = notificationHelper.getSubjectCommonVars(caseNumber, caseData);
        templateVarsSubject.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());

        final NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            // Send Email
            notificationResponse = sendEmailNotification(templateVarsSubject,
                cicCase.getEmail(),
                TemplateName.CASE_ISSUED_CITIZEN_EMAIL, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVarsSubject);
            //SEND POST
            notificationResponse = sendLetterNotification(templateVarsSubject, caseNumber);
        }

        cicCase.setSubjectLetterNotifyList(notificationResponse);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsApplicant = notificationHelper.getApplicantCommonVars(caseNumber, caseData);
        templateVarsApplicant.put(CommonConstants.CIC_CASE_APPLICANT_NAME, cicCase.getApplicantFullName());

        final NotificationResponse notificationResponse;
        if (caseData.getCicCase().getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            // Send Email
            notificationResponse = sendEmailNotification(templateVarsApplicant,
                cicCase.getApplicantEmailAddress(), TemplateName.CASE_ISSUED_CITIZEN_EMAIL, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVarsApplicant);
            notificationResponse = sendLetterNotification(templateVarsApplicant, caseNumber);
        }

        cicCase.setAppNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, cicCase.getRepresentativeFullName());

        final NotificationResponse notificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            // Send Email
            notificationResponse = sendEmailNotification(templateVarsRepresentative,
                cicCase.getRepresentativeEmailAddress(), TemplateName.CASE_ISSUED_CITIZEN_EMAIL, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVarsRepresentative);
            notificationResponse = sendLetterNotification(templateVarsRepresentative, caseNumber);
        }

        cicCase.setRepNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRespondent = notificationHelper.getRespondentCommonVars(caseNumber, caseData);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_RESPONDENT_NAME, caseData.getCicCase().getRespondentName());

        //TODO update date condition
        LocalDate today = LocalDate.now();
        //dummy date for now
        LocalDate dueDate = LocalDate.of(2026, 1, 1).plusDays(NUMBER_OF_DAYS_IN_WINDOW);
        templateVarsRespondent.put(CommonConstants.CIC_BUNDLE_DUE_DATE_TEXT,
            today.isAfter(dueDate)
                ? buildTimeString(true, dueDate) : buildTimeString(false, dueDate));

        final NotificationResponse notificationResponse;
        if (ObjectUtils.isNotEmpty(caseData.getCaseIssue().getDocumentList())) {
            final Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            final List<CaseworkerCICDocument> selectedDocuments = getSelectedDocuments(caseData);
            // Send Email
            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getRespondentEmail(),
                templateVarsRespondent,
                uploadedDocuments,
                selectedDocuments,
                caseNumber);
            cicCase.setSubjectLetterNotifyList(notificationResponse);
        } else {
            notificationResponse = sendEmailNotification(templateVarsRespondent,
                cicCase.getRespondentEmail(), TemplateName.CASE_ISSUED_RESPONDENT_EMAIL_UPDATED, caseNumber);
            cicCase.setResNotificationResponse(notificationResponse);
        }
    }

    private String buildTimeString(boolean isOutOfTimeRange, LocalDate dueDate) {

        //probs need to format date better
        if (isOutOfTimeRange) {
            return String.format("Out of time appeal - You should provide the tribunal with a case bundle by %s. " +
                "Do not issue to the Subject/Applicant/Representative until we notify you the appeal has been admitted.", dueDate);
        } else {
            return String.format("You should provide the tribunal and the " +
                "Subject/Applicant/Representative with a case bundle by %s", dueDate);
        }
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars, String toEmail,
                                                       TemplateName emailTemplateName, String caseReferenceNumber) {
        return notificationService.sendEmail(
            notificationHelper.buildEmailNotificationRequest(toEmail,
                templateVars,
                emailTemplateName),
            caseReferenceNumber);
    }

    private NotificationResponse sendEmailNotificationWithAttachment(String toEmail,
                                                                     final Map<String, Object> templateVars,
                                                                     Map<String, String> uploadedDocuments,
                                                                     List<CaseworkerCICDocument> selectedDocuments,
                                                                     String caseReferenceNumber) {
        return notificationService.sendEmail(
            notificationHelper.buildEmailNotificationRequest(toEmail,
                true,
                uploadedDocuments,
                templateVars,
                TemplateName.CASE_ISSUED_RESPONDENT_EMAIL_UPDATED),
            selectedDocuments,
            caseReferenceNumber);
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter, String caseReferenceNumber) {
        final NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(templateVarsLetter,
            TemplateName.CASE_ISSUED_CITIZEN_POST);
        return notificationService.sendLetter(letterRequest, caseReferenceNumber);
    }

    private Map<String, String> getUploadedDocuments(CaseData caseData) {
        final CaseIssue caseIssue = caseData.getCaseIssue();
        return notificationHelper.buildDocumentList(caseIssue.getDocumentList(), DOC_ATTACH_LIMIT);
    }

    private List<CaseworkerCICDocument> getSelectedDocuments(CaseData caseData) {
        var selectedDocIds = DocumentListUtil.extractDocumentIds(caseData.getCaseIssue().getDocumentList().getValue());
        return selectedDocIds.stream().map(id -> DocumentListUtil.getCaseDocumentById(id, caseData))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
