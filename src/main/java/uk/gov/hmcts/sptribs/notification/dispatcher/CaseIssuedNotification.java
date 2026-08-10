package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.CicCaseFieldsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.notification.EmailRespondentResponses;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.sptribs.notification.EmailRespondentResponses.IN_TIME_RESPONSE;
import static uk.gov.hmcts.sptribs.notification.EmailRespondentResponses.OUT_OF_TIME_RESPONSE;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CASE_ISSUED_CITIZEN_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CASE_ISSUED_CITIZEN_POST;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CASE_ISSUED_RESPONDENT_EMAIL;

@Component
@Slf4j
public class CaseIssuedNotification extends PartiesNotification {

    private static final int DOC_ATTACH_LIMIT = 5;

    public CaseIssuedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    protected PartyNotification buildSubjectNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsSubject = notificationHelper().getSubjectCommonVars(caseNumber, caseData);
        templateVarsSubject.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            return emailOnly(
                cicCase.getEmail(),
                templateVarsSubject,
                CASE_ISSUED_CITIZEN_EMAIL,
                saveToCicCase(CicCase::setSubjectNotifyList)
            );
        } else {
            return new LetterNotification(
                cicCase.getAddress(),
                templateVarsSubject,
                CASE_ISSUED_CITIZEN_POST,
                saveToCicCase(CicCase::setSubjectLetterNotifyList)
            );
        }
    }

    @Override
    protected PartyNotification buildApplicantNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsApplicant = notificationHelper().getApplicantCommonVars(caseNumber, caseData);
        templateVarsApplicant.put(CommonConstants.CIC_CASE_APPLICANT_NAME, cicCase.getApplicantFullName());

        if (caseData
            .getCicCase()
            .getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            return emailOnly(
                cicCase.getApplicantEmailAddress(),
                templateVarsApplicant,
                CASE_ISSUED_CITIZEN_EMAIL,
                saveToCicCase(CicCase::setAppNotificationResponse)
            );
        } else {
            return new LetterNotification(
                cicCase.getApplicantAddress(),
                templateVarsApplicant,
                CASE_ISSUED_CITIZEN_POST,
                saveToCicCase(CicCase::setAppLetterNotificationResponse)
            );
        }
    }

    @Override
    protected PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsRepresentative = notificationHelper().getRepresentativeCommonVars(caseNumber, caseData);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, cicCase.getRepresentativeFullName());

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            return emailOnly(
                cicCase.getRepresentativeEmailAddress(),
                templateVarsRepresentative,
                CASE_ISSUED_CITIZEN_EMAIL,
                saveToCicCase(CicCase::setRepNotificationResponse)
            );
        } else {
            return new LetterNotification(
                cicCase.getRepresentativeAddress(),
                templateVarsRepresentative,
                CASE_ISSUED_CITIZEN_POST,
                saveToCicCase(CicCase::setRepLetterNotificationResponse));
        }
    }

    @Override
    protected PartyNotification buildRespondentNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsRespondent = notificationHelper().getRespondentCommonVars(caseNumber, caseData);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_RESPONDENT_NAME, caseData
            .getCicCase()
            .getRespondentName());

        LocalDate dueDate = cicCase.getRespondentBundleDueDate();
        if (cicCase.getIsCaseInTime() == null) {
            CicCaseFieldsUtil.calculateAndSetIsCaseInTime(caseData);
        }
        templateVarsRespondent.put(CommonConstants.CIC_BUNDLE_DUE_DATE_TEXT,
            cicCase
                .getIsCaseInTime()
                .toBoolean() ? buildTimeString(true, dueDate) : buildTimeString(false, dueDate));

        if (ObjectUtils.isNotEmpty(caseData.getCaseIssue().getDocumentList())) {
            DynamicMultiSelectList selectList = caseData.getCaseIssue().getDocumentList();
            Map<String, String> uploadedDocuments = getUploadedDocuments(caseData);
            List<CaseworkerCICDocument> selectedDocuments = DocumentListUtil.getSelectedDocumentsFromDynamicList(caseData, selectList);
            return new EmailNotification(
                cicCase.getAlternativeRespondentEmail(),
                templateVarsRespondent,
                CASE_ISSUED_RESPONDENT_EMAIL,
                uploadedDocuments,
                selectedDocuments,
                saveToCicCase(CicCase::setResNotificationResponse));
        } else {
            return emailOnly(cicCase.getAlternativeRespondentEmail(),
                templateVarsRespondent,
                CASE_ISSUED_RESPONDENT_EMAIL,
                saveToCicCase(CicCase::setResNotificationResponse));
        }
    }

    private String buildTimeString(boolean isInTime, LocalDate dueDate) {
        EmailRespondentResponses response = isInTime
            ? IN_TIME_RESPONSE
            : OUT_OF_TIME_RESPONSE;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = dueDate.format(formatter);

        return response.format(formattedDate);
    }

    private Map<String, String> getUploadedDocuments(CaseData caseData) {
        CaseIssue caseIssue = caseData.getCaseIssue();
        return notificationHelper().buildDocumentList(caseIssue.getDocumentList(), DOC_ATTACH_LIMIT);
    }
}
