package uk.gov.hmcts.sptribs.notification;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import static org.apache.commons.lang3.StringUtils.isBlank;

//TODO: Revisit if something similar is needed, Its part of CaseTask, think its not needed - Santoshini
@Service
public class NotificationDispatcher {

    public void send(final ApplicantNotification applicantNotification, final CaseData caseData, final Long caseId) {

        if (caseData.getApplicant1().isRepresented() && !caseData.getApplicant1().isOffline()) {
            applicantNotification.sendToApplicant1Solicitor(caseData, caseId);
        } else if (caseData.getApplicant1().isOffline()) {
            applicantNotification.sendToApplicant1Offline(caseData, caseId);
        } else {
            applicantNotification.sendToApplicant1(caseData, caseId);
        }

        if (caseData.getApplicant2().isRepresented() && !caseData.getApplicant2().isOffline()) {
            applicantNotification.sendToApplicant2Solicitor(caseData, caseId);
        } else if (isBlank(caseData.getApplicant2EmailAddress()) || caseData.getApplicant2().isOffline()) {
            applicantNotification.sendToApplicant2Offline(caseData, caseId);
        } else {
            applicantNotification.sendToApplicant2(caseData, caseId);
        }
    }
}
