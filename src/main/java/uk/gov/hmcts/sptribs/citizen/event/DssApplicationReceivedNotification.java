package uk.gov.hmcts.sptribs.citizen.event;

import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.DssNotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CICA_REF_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_REPRESENTATIVE_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_PARTY_INFO;
import static uk.gov.hmcts.sptribs.common.CommonConstants.HAS_CICA_NUMBER;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED_CY;

@Component
@Setter
public class DssApplicationReceivedNotification implements PartiesNotification {

    @Autowired
    private NotificationServiceCIC notificationService;

    @Autowired
    private DssNotificationHelper dssNotificationHelper;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        final DssCaseData dssCaseData = caseData.getDssCaseData();
        final Map<String, Object> templateVarsSubject = dssNotificationHelper.getSubjectCommonVars(caseNumber, caseData);
        templateVarsSubject.put(CIC_CASE_SUBJECT_NAME, dssCaseData.getSubjectFullName());
        templateVarsSubject.put(CONTACT_PARTY_INFO, dssCaseData.getNotifyPartyMessage());
        if (caseData.getEditCicaCaseDetails() != null && !StringUtils.isEmpty(caseData.getEditCicaCaseDetails().getCicaReferenceNumber())) {
            templateVarsSubject.put(HAS_CICA_NUMBER, true);
            templateVarsSubject.put(CICA_REF_NUMBER, caseData.getEditCicaCaseDetails().getCicaReferenceNumber());
        } else {
            templateVarsSubject.put(HAS_CICA_NUMBER, false);
            templateVarsSubject.put(CICA_REF_NUMBER, "");
        }
        NotificationResponse notificationResponse = sendEmailNotification(
            templateVarsSubject,
            dssCaseData.getSubjectEmailAddress(),
            dssCaseData.getLanguagePreference(),
            caseNumber
        );
        dssCaseData.setSubjectNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final DssCaseData dssCaseData = caseData.getDssCaseData();
        final Map<String, Object> templateVarsRep = dssNotificationHelper.getRepresentativeCommonVars(caseNumber, caseData);
        templateVarsRep.put(CIC_CASE_REPRESENTATIVE_NAME, dssCaseData.getRepresentativeFullName());
        templateVarsRep.put(CONTACT_PARTY_INFO, dssCaseData.getNotifyPartyMessage());
        if (caseData.getEditCicaCaseDetails() != null && !StringUtils.isEmpty(caseData.getEditCicaCaseDetails().getCicaReferenceNumber())) {
            templateVarsRep.put(HAS_CICA_NUMBER, true);
            templateVarsRep.put(CICA_REF_NUMBER, caseData.getEditCicaCaseDetails().getCicaReferenceNumber());
        } else {
            templateVarsRep.put(HAS_CICA_NUMBER, false);
            templateVarsRep.put(CICA_REF_NUMBER, "");
        }
        NotificationResponse notificationResponse = sendEmailNotification(
            templateVarsRep,
            dssCaseData.getRepresentativeEmailAddress(),
            ENGLISH,
            caseNumber
        );
        dssCaseData.setRepNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars,
                                                       String toEmail,
                                                       LanguagePreference languagePreference,
                                                       String caseReferenceNumber) {
        TemplateName templateName = ENGLISH.equals(languagePreference) ? APPLICATION_RECEIVED : APPLICATION_RECEIVED_CY;
        NotificationRequest request =
            dssNotificationHelper.buildEmailNotificationRequest(toEmail, templateVars, templateName);
        return notificationService.sendEmail(request, caseReferenceNumber);
    }

}
