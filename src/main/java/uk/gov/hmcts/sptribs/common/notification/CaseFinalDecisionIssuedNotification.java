package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.document.CaseDocumentClient;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.common.CommonConstants.FINAL_DECISION_NOTICE;

@Component
@Slf4j
public class CaseFinalDecisionIssuedNotification implements PartiesNotification {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CaseDocumentClient caseDocumentClient;

    @Autowired
    private NotificationServiceCIC notificationService;

    @Autowired
    private NotificationHelper notificationHelper;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsSubject = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);

        NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType().isEmail()) {
            try {
                addDecisionsIssuedFileContents(caseData, templateVarsSubject);
            } catch (IOException e) {
                log.info("Unable to download Final Decision documents for Subject: {}", e.getMessage());
            }

            notificationResponse = sendEmailNotification(templateVarsSubject,
                cicCase.getEmail(),
                TemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVarsSubject);
            notificationResponse = sendLetterNotification(templateVarsSubject,
                TemplateName.CASE_FINAL_DECISION_ISSUED_POST);
        }

        cicCase.setSubjectNotifyList(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVarsRepresentative  = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);

        NotificationResponse notificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference().isEmail()) {
            try {
                addDecisionsIssuedFileContents(caseData, templateVarsRepresentative);
            } catch (IOException e) {
                log.info("Unable to download Final Decision documents for Representative: {}", e.getMessage());
            }

            notificationResponse = sendEmailNotification(templateVarsRepresentative,
                cicCase.getRepresentativeEmailAddress(), TemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVarsRepresentative);
            notificationResponse = sendLetterNotification(templateVarsRepresentative,
                TemplateName.CASE_FINAL_DECISION_ISSUED_POST);
        }

        cicCase.setRepNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        final Map<String, Object> templateVarsRespondent = notificationHelper.getRespondentCommonVars(caseNumber, caseData.getCicCase());
        CicCase cicCase = caseData.getCicCase();

        try {
            addDecisionsIssuedFileContents(caseData, templateVarsRespondent);
        } catch (IOException e) {
            log.info("Unable to download Final Decision documents for Respondent: {}", e.getMessage());
        }

        NotificationResponse notificationResponse = sendEmailNotification(templateVarsRespondent,
            caseData.getCicCase().getRespondantEmail(), TemplateName.CASE_FINAL_DECISION_ISSUED_EMAIL);
        cicCase.setResNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotification(final Map<String, Object> templateVars,
                                                       String toEmail,
                                                       TemplateName emailTemplateName) {
        NotificationRequest request = notificationHelper.buildEmailNotificationRequest(toEmail, templateVars, emailTemplateName);
        notificationService.setNotificationRequest(request);
        return notificationService.sendEmail();
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter, TemplateName letterTemplateName) {
        NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(templateVarsLetter, letterTemplateName);
        notificationService.setNotificationRequest(letterRequest);
        return notificationService.sendLetter();
    }

    private void addDecisionsIssuedFileContents(CaseData caseData, Map<String, Object> templateVars) throws IOException {
        final String authorisation = httpServletRequest.getHeader(AUTHORIZATION);
        String serviceAuthorization = authTokenGenerator.generate();

        CaseIssueFinalDecision caseIssueFinalDecision = caseData.getCaseIssueFinalDecision();

        List<String> uploadedDocumentsUrls = new ArrayList<>();

        String finalDecisionNotice = getFinalDecisionNoticeDocument(caseIssueFinalDecision);
        uploadedDocumentsUrls.add(finalDecisionNotice);

        String finalDecisionGuidance = StringUtils.substringAfterLast(caseIssueFinalDecision.getFinalDecisionGuidance().getUrl(), "/");
        uploadedDocumentsUrls.add(finalDecisionGuidance);

        for (String item : uploadedDocumentsUrls) {

            Resource uploadedDocument = caseDocumentClient.getDocumentBinary(authorisation,
                serviceAuthorization,
                UUID.fromString(item)).getBody();

            if (uploadedDocument != null) {
                log.info("Document found with uuid : {}", UUID.fromString(item));
                byte[] uploadedDocumentContents = uploadedDocument.getInputStream().readAllBytes();
                    templateVars.put(FINAL_DECISION_NOTICE, notificationService.getJsonFileAttachment(uploadedDocumentContents));
            } else {
                log.info("Document not found with uuid : {}", UUID.fromString(item));
            }
        }

    }

    private String getFinalDecisionNoticeDocument(CaseIssueFinalDecision caseIssueFinalDecision) {
        String finalDecisionNotice;
        if (null != caseIssueFinalDecision.getDocuments() && !caseIssueFinalDecision.getDocuments().isEmpty()) {
            List<String> uploadedDecisionNoticeDocs = caseIssueFinalDecision.getDocuments().stream().map(ListValue::getValue)
                .map(item -> StringUtils.substringAfterLast(item.getDocumentLink().getUrl(), "/"))
                .toList();
            finalDecisionNotice = uploadedDecisionNoticeDocs.get(0);

        } else {
            finalDecisionNotice = StringUtils.substringAfterLast(caseIssueFinalDecision.getFinalDecisionDraft().getUrl(), "/");
        }

        return finalDecisionNotice;
    }

}
