package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.notification.TemplateName.HEARING_CANCELLED_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.HEARING_CANCELLED_POST;

@Component
@Slf4j
public class CancelHearingNotification extends PartiesNotification {

    public CancelHearingNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    protected PartyNotification buildSubjectNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> subjectTemplateVars = notificationHelper().getSubjectCommonVars(caseNumber, caseData);
        notificationHelper().addHearingPostponedTemplateVars(cicCase, subjectTemplateVars);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            return new EmailNotification(
                    cicCase.getEmail(),
                    subjectTemplateVars,
                    HEARING_CANCELLED_EMAIL,
                    new HashMap<>(),
                    new ArrayList<>(),
                    saveToCicCase(CicCase::setSubjectNotifyList)
            );
        } else {
            return new LetterNotification(
                    cicCase.getAddress(),
                    subjectTemplateVars,
                    HEARING_CANCELLED_POST,
                    saveToCicCase(CicCase::setSubjectLetterNotifyList)
            );
        }
    }

    @Override
    protected PartyNotification buildApplicantNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> applicantCommonVars = notificationHelper().getApplicantCommonVars(caseNumber, caseData);
        notificationHelper().addHearingPostponedTemplateVars(cicCase, applicantCommonVars);

        if (cicCase.getApplicantContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            return new EmailNotification(
                    cicCase.getApplicantEmailAddress(),
                    applicantCommonVars,
                    HEARING_CANCELLED_EMAIL,
                    new HashMap<>(),
                    new ArrayList<>(),
                    saveToCicCase(CicCase::setAppNotificationResponse)
            );
        } else {
            return new LetterNotification(
                    cicCase.getApplicantAddress(),
                    applicantCommonVars,
                    HEARING_CANCELLED_POST,
                    saveToCicCase(CicCase::setAppLetterNotificationResponse)
            );
        }
    }

    @Override
    protected PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> repTemplateVars = notificationHelper().getRepresentativeCommonVars(caseNumber, caseData);
        notificationHelper().addHearingPostponedTemplateVars(cicCase, repTemplateVars);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            return new EmailNotification(
                    cicCase.getRepresentativeEmailAddress(),
                    repTemplateVars,
                    HEARING_CANCELLED_EMAIL,
                    new HashMap<>(),
                    new ArrayList<>(),
                    saveToCicCase(CicCase::setRepNotificationResponse)
            );
        } else {
            return new LetterNotification(
                    cicCase.getRepresentativeAddress(),
                    repTemplateVars,
                    HEARING_CANCELLED_POST,
                    saveToCicCase(CicCase::setRepLetterNotificationResponse)
            );
        }
    }

    @Override
    protected PartyNotification buildRespondentNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> respondentTemplateVars =
            notificationHelper().getRespondentCommonVars(caseNumber, caseData);
        notificationHelper().addHearingPostponedTemplateVars(cicCase, respondentTemplateVars);

        return PartiesNotification.emailOnly(cicCase.getRespondentEmail(), respondentTemplateVars, HEARING_CANCELLED_EMAIL, saveToCicCase(CicCase::setResNotificationResponse));
    }
}
