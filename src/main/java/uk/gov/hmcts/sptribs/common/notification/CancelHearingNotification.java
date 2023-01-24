package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SPACE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.HEARING_DATE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.HEARING_TIME;

@Component
@Slf4j
public class CancelHearingNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    @Autowired
    private NotificationHelper notificationHelper;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> subjectTemplateVars = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);
        addCancelHearingTemplateVars(cicCase, subjectTemplateVars);

        NotificationResponse subjectNotificationResponse;
        if (cicCase.getContactPreferenceType().isEmail()) {
            subjectNotificationResponse = sendEmailNotification(cicCase.getEmail(), subjectTemplateVars);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), subjectTemplateVars);
            subjectNotificationResponse = sendLetterNotification(subjectTemplateVars);
        }

        cicCase.setSubjectNotifyList(subjectNotificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> reprTemplateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);
        addCancelHearingTemplateVars(cicCase, reprTemplateVars);

        NotificationResponse representativeNotificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference().isEmail()) {
            representativeNotificationResponse = sendEmailNotification(cicCase.getRepresentativeEmailAddress(), reprTemplateVars);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), reprTemplateVars);
            representativeNotificationResponse = sendLetterNotification(reprTemplateVars);
        }

        cicCase.setRepNotificationResponse(representativeNotificationResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> respondentTemplateVars = notificationHelper.getRespondentCommonVars(caseNumber, cicCase);
        addCancelHearingTemplateVars(cicCase, respondentTemplateVars);

        NotificationResponse respondentNotificationResponse = sendEmailNotification(cicCase.getRespondantEmail(), respondentTemplateVars);
        cicCase.setAppNotificationResponse(respondentNotificationResponse);
    }

    private NotificationResponse sendEmailNotification(final String destinationAddress, final Map<String, Object> templateVars) {
        NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            templateVars,
            TemplateName.CASE_CANCEL_HEARING_EMAIL);
        notificationService.setNotificationRequest(request);
        return notificationService.sendEmail();
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter) {
        NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.CASE_CANCEL_HEARING_POST);
        notificationService.setNotificationRequest(letterRequest);
        return notificationService.sendLetter();
    }

    private void addCancelHearingTemplateVars(CicCase cicCase, Map<String, Object> templateVars) {
        String selectedHearingDateTime = cicCase.getSelectedHearingToCancel();
        String[] hearingDateTimeArr = (null != selectedHearingDateTime) ? selectedHearingDateTime.split(SPACE + HYPHEN + SPACE) : null;
        String hearingDate = null != hearingDateTimeArr && ArrayUtils.isNotEmpty(hearingDateTimeArr)
            ? hearingDateTimeArr[1].substring(0, hearingDateTimeArr[1].lastIndexOf(SPACE))
            : null;
        String hearingTime = null != hearingDateTimeArr && ArrayUtils.isNotEmpty(hearingDateTimeArr)
            ? hearingDateTimeArr[1].substring(hearingDateTimeArr[1].lastIndexOf(SPACE) + 1)
            : null;

        templateVars.put(HEARING_DATE, hearingDate);
        templateVars.put(HEARING_TIME, hearingTime);
    }
}
