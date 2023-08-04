package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.NotifyProxyClient;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.notifyproxy.model.Notification;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CLOSURE_INFORMATION;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CLOSURE_REASON;
import static uk.gov.hmcts.sptribs.common.CommonConstants.NONE_PROVIDED;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.BEARER_PREFIX;

@Component
@Slf4j
public class CaseWithdrawnNotification implements PartiesNotification {
/*

    @Autowired
    private NotificationServiceCIC notificationService;
*/
    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private NotifyProxyClient notifyProxyClient;

    @Autowired
    private NotificationHelper notificationHelper;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);
        addCaseClosedTemplateVars(caseData, templateVars);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {

            String caseWithdrawnNotifyResponse = sendEmailNotification(cicCase.getEmail(), templateVars);
            //cicCase.setSubjectNotifyList(caseWithdrawnNotifyResponse);
            /*Notification notification = cicCase.getNotification();
            Notification.CaseWithdrawnNotification  caseWithdrawnNotification = notification.new CaseWithdrawnNotification();
            caseWithdrawnNotification.setSubNotificationSent(caseWithdrawnNotifyResponse);
            cicCase.setNotification(notification);*/
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            sendLetterNotification(templateVars);
        }
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> templateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);
        addCaseClosedTemplateVars(caseData, templateVars);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            /*NotificationResponse caseWithdrawnNotifyResponse =
                sendEmailNotification(cicCase.getRepresentativeEmailAddress(), templateVars);
            cicCase.setRepNotificationResponse(caseWithdrawnNotifyResponse);*/
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            sendLetterNotification(templateVars);
        }
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> respondentTemplateVars = notificationHelper.getRespondentCommonVars(caseNumber, cicCase);
        addCaseClosedTemplateVars(caseData, respondentTemplateVars);

        /*NotificationResponse caseWithdrawnNotifyResponse = sendEmailNotification(cicCase.getRespondentEmail(), respondentTemplateVars);
        cicCase.setResNotificationResponse(caseWithdrawnNotifyResponse);*/
    }

    private String sendEmailNotification(final String destinationAddress, final Map<String, Object> templateVars) {
        NotificationRequest notificationRequest = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            templateVars,
            TemplateName.CASE_WITHDRAWN_EMAIL);
        //return notificationService.sendEmail(notificationRequest);

        String serviceAuthToken = authTokenGenerator.generate();
        final User user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        final String authorisation = user.getAuthToken().startsWith(BEARER_PREFIX)
            ? user.getAuthToken() : BEARER_PREFIX + user.getAuthToken();
        Notification notification = new Notification();
        notification.setNotificationType("Email");
        ResponseEntity<?> responseEntity = notifyProxyClient.
            sendEmailNotification(authorisation, serviceAuthToken, 23123L, notification);
        String isNotificationSent = (String) responseEntity.getBody();
        return "isNotificationSent";
    }

    private void sendLetterNotification(Map<String, Object> templateVarsLetter) {
        NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.CASE_WITHDRAWN_POST);
        //notificationService.sendLetter(letterRequest);
    }

    private void addCaseClosedTemplateVars(CaseData caseData, Map<String, Object> templateVars) {
        CloseCase closeCase = caseData.getCloseCase();
        String additionalDetail = StringUtils.isNotEmpty(closeCase.getAdditionalDetail())
            ? closeCase.getAdditionalDetail() : NONE_PROVIDED;

        templateVars.put(CLOSURE_REASON, closeCase.getCloseCaseReason());
        templateVars.put(CLOSURE_INFORMATION, additionalDetail);
    }
}
