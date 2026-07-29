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
public class HearingPostponedNotification extends PartiesNotification {

    public HearingPostponedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    protected PartyNotification buildSubjectNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getSubjectCommonVars(caseNumber, caseData);
        notificationHelper().addHearingPostponedTemplateVars(cicCase, templateVars);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            return emailOnly(cicCase.getEmail(), templateVars, TemplateName.HEARING_POSTPONED_EMAIL, saveToCicCase(CicCase::setSubjectNotifyList));
        } else {
            notificationHelper().addAddressTemplateVars(cicCase.getAddress(), templateVars);
            return new LetterNotification(cicCase.getAddress(), templateVars, TemplateName.HEARING_POSTPONED_POST, saveToCicCase(CicCase::setSubjectLetterNotifyList));
        }
    }

    @Override
    protected PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getRepresentativeCommonVars(caseNumber, caseData);
        notificationHelper().addHearingPostponedTemplateVars(cicCase, templateVars);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            return emailOnly(cicCase.getRepresentativeEmailAddress(), templateVars, TemplateName.HEARING_POSTPONED_EMAIL, saveToCicCase(CicCase::setRepNotificationResponse));
        } else {
            notificationHelper().addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            return new LetterNotification(cicCase.getRepresentativeAddress(), templateVars, TemplateName.HEARING_POSTPONED_POST, saveToCicCase(CicCase::setRepLetterNotificationResponse));
        }
    }

    @Override
    protected PartyNotification buildRespondentNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> respondentTemplateVars = notificationHelper().getRespondentCommonVars(caseNumber, caseData);
        notificationHelper().addHearingPostponedTemplateVars(cicCase, respondentTemplateVars);

        return emailOnly(cicCase.getRespondentEmail(), respondentTemplateVars, TemplateName.HEARING_POSTPONED_EMAIL, saveToCicCase(CicCase::setResNotificationResponse));
    }
}
