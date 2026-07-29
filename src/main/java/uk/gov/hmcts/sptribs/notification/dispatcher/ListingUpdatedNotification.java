package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;

import java.util.Map;

@Component
@Slf4j
public class ListingUpdatedNotification extends PartiesNotification {

    public ListingUpdatedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    protected PartyNotification buildSubjectNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsSubject = notificationHelper().getSubjectCommonVars(caseNumber, caseData);
        Listing listing = caseData.getListing();
        notificationHelper().setRecordingTemplateVars(templateVarsSubject, listing);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            return emailOnly(cicCase.getEmail(), templateVarsSubject, TemplateName.HEARING_UPDATED_EMAIL, saveToCicCase(CicCase::setSubjectNotifyList));
        } else {
            notificationHelper().addAddressTemplateVars(cicCase.getAddress(), templateVarsSubject);
            return new LetterNotification(cicCase.getAddress(), templateVarsSubject, TemplateName.HEARING_UPDATED_POST, saveToCicCase(CicCase::setSubjectLetterNotifyList));
        }
    }

    @Override
    protected PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsRepresentative = notificationHelper().getRepresentativeCommonVars(caseNumber, caseData);
        templateVarsRepresentative.put(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, cicCase.getRepresentativeFullName());
        Listing listing = caseData.getListing();
        notificationHelper().setRecordingTemplateVars(templateVarsRepresentative, listing);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            return emailOnly(cicCase.getRepresentativeEmailAddress(), templateVarsRepresentative, TemplateName.HEARING_UPDATED_EMAIL, saveToCicCase(CicCase::setRepNotificationResponse));
        } else {
            notificationHelper().addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVarsRepresentative);
            return new LetterNotification(cicCase.getRepresentativeAddress(), templateVarsRepresentative, TemplateName.HEARING_UPDATED_POST, saveToCicCase(CicCase::setRepLetterNotificationResponse));
        }
    }

    @Override
    protected PartyNotification buildRespondentNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsRespondent = notificationHelper().getRespondentCommonVars(caseNumber, caseData);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_RESPONDENT_NAME, caseData.getCicCase().getRespondentName());
        Listing listing = caseData.getListing();
        notificationHelper().setRecordingTemplateVars(templateVarsRespondent, listing);

        return emailOnly(cicCase.getRespondentEmail(), templateVarsRespondent, TemplateName.HEARING_UPDATED_EMAIL, saveToCicCase(CicCase::setResNotificationResponse));
    }
}
