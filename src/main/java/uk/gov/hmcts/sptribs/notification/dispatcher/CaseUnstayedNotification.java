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
public class CaseUnstayedNotification extends PartiesNotification {

    public CaseUnstayedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    protected PartyNotification buildSubjectNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getSubjectCommonVars(caseNumber, caseData);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            return emailOnly(
                    cicCase.getEmail(),
                    templateVars,
                    TemplateName.CASE_UNSTAYED_EMAIL,
                    saveToCicCase(CicCase::setSubjectNotifyList)
            );
        } else {
            notificationHelper().addAddressTemplateVars(cicCase.getAddress(), templateVars);
            return new LetterNotification(
                    cicCase.getAddress(),
                    templateVars,
                    TemplateName.CASE_UNSTAYED_POST,
                    saveToCicCase(CicCase::setSubjectLetterNotifyList)
            );
        }
    }

    @Override
    protected PartyNotification buildApplicantNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getApplicantCommonVars(caseNumber, caseData);

        if (cicCase.getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            return emailOnly(
                    cicCase.getApplicantEmailAddress(),
                    templateVars,
                    TemplateName.CASE_UNSTAYED_EMAIL,
                    saveToCicCase(CicCase::setAppNotificationResponse)
            );
        } else {
            notificationHelper().addAddressTemplateVars(cicCase.getApplicantAddress(), templateVars);
            return new LetterNotification(
                    cicCase.getApplicantAddress(),
                    templateVars,
                    TemplateName.CASE_UNSTAYED_POST,
                    saveToCicCase(CicCase::setAppLetterNotificationResponse)
            );
        }
    }

    @Override
    protected PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getRepresentativeCommonVars(caseNumber, caseData);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            return emailOnly(
                    cicCase.getRepresentativeEmailAddress(),
                    templateVars,
                    TemplateName.CASE_UNSTAYED_EMAIL,
                    saveToCicCase(CicCase::setRepNotificationResponse)
            );
        } else {
            notificationHelper().addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            return new LetterNotification(
                    cicCase.getRepresentativeAddress(),
                    templateVars,
                    TemplateName.CASE_UNSTAYED_POST,
                    saveToCicCase(CicCase::setRepLetterNotificationResponse)
            );
        }
    }
}
