package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;

import java.util.Map;

@Component
@Slf4j
public class CaseLinkedNotification extends PartiesNotification {

    public CaseLinkedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    protected PartyNotification buildSubjectNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getSubjectCommonVars(caseNumber, caseData);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            return PartiesNotification.emailOnly(cicCase.getEmail(), templateVars, TemplateName.CASE_LINKED_EMAIL, saveToCicCase(CicCase::setSubjectNotifyList));
        } else {
            return new LetterNotification(cicCase.getAddress(), templateVars, TemplateName.CASE_LINKED_POST, saveToCicCase(CicCase::setSubjectLetterNotifyList));
        }
    }

    @Override
    protected PartyNotification buildApplicantNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> applicantCommonVars = notificationHelper().getApplicantCommonVars(caseNumber, caseData);

        if (cicCase.getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            return PartiesNotification.emailOnly(cicCase.getApplicantEmailAddress(), applicantCommonVars, TemplateName.CASE_LINKED_EMAIL, saveToCicCase(CicCase::setAppNotificationResponse));
        } else {
            return new LetterNotification(cicCase.getApplicantAddress(), applicantCommonVars, TemplateName.CASE_LINKED_POST, saveToCicCase(CicCase::setAppLetterNotificationResponse));
        }
    }

    @Override
    protected PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> representativeCommonVars = notificationHelper().getRepresentativeCommonVars(caseNumber, caseData);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            return PartiesNotification.emailOnly(cicCase.getRepresentativeEmailAddress(), representativeCommonVars, TemplateName.CASE_LINKED_EMAIL, saveToCicCase(CicCase::setRepNotificationResponse));
        } else {
            return new LetterNotification(cicCase.getRepresentativeAddress(), representativeCommonVars, TemplateName.CASE_LINKED_POST, saveToCicCase(CicCase::setRepLetterNotificationResponse));
        }
    }
}
