package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.document.CaseDocumentClient;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.sptribs.common.CommonConstants.DECISION_NOTICE;

@Component
@Slf4j
public class DecisionIssuedNotification implements PartiesNotification {

    @Autowired
    private IdamService idamService;

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

        Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);

        NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType().isEmail()) {
            try {
                addDecisionsIssuedFileContents(caseData.getCaseIssueDecision(), templateVars);
            } catch (IOException e) {
                log.info("Unable to download Decision Notice document: {}", e.getMessage());
            }

            notificationResponse = sendEmailNotification(cicCase.getEmail(), templateVars);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars);
        }

        cicCase.setSubjectNotifyList(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> templateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);

        NotificationResponse notificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference().isEmail()) {
            notificationResponse = sendEmailNotification(cicCase.getRepresentativeEmailAddress(), templateVars);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars);
        }

        cicCase.setRepNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> templateVars = notificationHelper.getRespondentCommonVars(caseNumber, cicCase);

        NotificationResponse notificationResponse = sendEmailNotification(cicCase.getRespondantEmail(), templateVars);
        cicCase.setAppNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotification(final String destinationAddress, final Map<String, Object> templateVars) {
        NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            templateVars,
            TemplateName.DECISION_ISSUED_EMAIL);
        notificationService.setNotificationRequest(request);
        return notificationService.sendEmail();
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter) {
        NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.DECISION_ISSUED_POST);
        notificationService.setNotificationRequest(letterRequest);
        return notificationService.sendLetter();
    }

    private void addDecisionsIssuedFileContents(CaseIssueDecision caseIssueDecision, Map<String, Object> templateVars) throws IOException {
        int count = 0;

        final String authorisation = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        String serviceAuthorization = authTokenGenerator.generate();

        if (caseIssueDecision.getDecisionDocument() != null) {
            List<String> uploadedDocumentsUrls = caseIssueDecision.getDecisionDocument().stream().map(item -> item.getValue())
                .map(item -> StringUtils.substringAfterLast(item.getDocumentLink().getUrl(), "/"))
                .collect(Collectors.toList());

            count = 1;
            for (String item : uploadedDocumentsUrls) {

                Resource uploadedDocument = caseDocumentClient.getDocumentBinary(authorisation,
                    serviceAuthorization,
                    UUID.fromString(item)).getBody();

                if (uploadedDocument != null) {
                    log.info("Document found with uuid : {}", UUID.fromString(item));
                    byte[] uploadedDocumentContents = uploadedDocument.getInputStream().readAllBytes();
                    templateVars.put("Document1", true);
                    templateVars.put(DECISION_NOTICE + count, notificationService.getJsonFileAttachment(uploadedDocumentContents));
                } else {
                    log.info("Document not found with uuid : {}", UUID.fromString(item));
                }
            }

        }
    }



}
