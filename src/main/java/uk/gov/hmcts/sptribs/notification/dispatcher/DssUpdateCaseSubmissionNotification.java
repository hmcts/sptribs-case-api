package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;

import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_EMAIL_VALUE;
import static uk.gov.hmcts.sptribs.notification.TemplateName.UPDATE_RECEIVED;
import static uk.gov.hmcts.sptribs.notification.TemplateName.UPDATE_RECEIVED_CIC;

@Component
@Slf4j
public class DssUpdateCaseSubmissionNotification extends PartiesNotification {

    public DssUpdateCaseSubmissionNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    protected PartyNotification buildApplicantNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getSubjectCommonVars(caseNumber, caseData);

        return emailOnly(cicCase.getEmail(), templateVars, UPDATE_RECEIVED, saveToCicCase(CicCase::setAppNotificationResponse));
    }

    @Override
    protected PartyNotification buildTribunalNotification(CaseData caseData, String caseNumber) {
        Map<String, Object> templateVars = notificationHelper().getTribunalCommonVars(caseNumber, caseData);

        return emailOnly(TRIBUNAL_EMAIL_VALUE, templateVars, UPDATE_RECEIVED_CIC, saveToCicCase(CicCase::setTribunalNotificationResponse));
    }
}
